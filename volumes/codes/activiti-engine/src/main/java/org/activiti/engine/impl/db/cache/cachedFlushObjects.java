package org.activiti.engine.impl.db.cache;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.db.redis.tools.jackJson.jsonTransfer;
import org.activiti.engine.impl.db.redis.tools.rwSet.extractor.readSetExtractor;
import org.activiti.engine.impl.db.redis.tools.rwSet.extractor.writeSetExtractor;
import org.activiti.engine.impl.db.workflowClass.*;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @apiNote 缓存的insert,delete,update的entity对象(deploy,instance,complete)
 */
public class cachedFlushObjects {
    /*
     * 结构如下
     * <oid,<EntityType,<EntityId,EntityObject>>>
     */
    private static volatile ConcurrentHashMap<String,Map<Class<? extends Entity>, Map<String, Entity>>> cachedInsert=new ConcurrentHashMap<>(20000);
    /*
     * 结构如下
     *  <oid,<EntityType,<EntityId,EntityObject>>>
     */
    private static volatile ConcurrentHashMap<String,Map<Class<? extends Entity>, Map<String, Entity>>> cachedDelete=new ConcurrentHashMap<>(20000);
    /*
     * 结构如下
     * <oid,[updatedEntities]>
     */
    private static volatile ConcurrentHashMap<String,List<Entity>> cachedUpdate=new ConcurrentHashMap<>(20000);
    //private static volatile Map<String,HashMap<String,String>> currentTaskId=new HashMap<>();//这个也准备改到activiti上一层
    private static volatile ConcurrentHashMap<String,cachedResponse> cachedResponse=new ConcurrentHashMap<>(20000);
    private static Logger logger=LoggerFactory.getLogger(cachedFlushObjects.class);

    /**
     * @apiNote 根据oids将缓存的数据写入redis
     * @param Oids 所有需要写入redis的实例的唯一id
     * @return
     */
    public static String flushCachedObjectsToRedis(CommandContext commandContext,String[] Oids) {
        List<Map<Class<? extends Entity>, Map<String, Entity>>> insertedList=new LinkedList<>();
        List<Map<Class<? extends Entity>, Map<String, Entity>>> deletedList=new LinkedList<>();
        List<Entity> updatedObjects=new ArrayList<>();
        for (String Oid:Oids) {
            if (Oid.endsWith(".bpmn")) {
                /*
                 * 部署的话，需要同时写数据库，因为在实例化有些复杂的查询涉及到部署的BPMN，字段映射有点麻烦(已经加了缓存，所以性能没问题)
                 */
                Map<Class<? extends Entity>, Map<String, Entity>> inserts=cachedInsert.remove(Oid);
                Map<Class<? extends Entity>, Map<String, Entity>> deletes=cachedDelete.remove(Oid);
                List<Entity> updates=cachedUpdate.remove(Oid);
                for (Map<String,Entity> entities:inserts.values()) {
                    for (Entity entity:entities.values()) {
                        commandContext.getDbSqlSession().flushInsert(entity);
                    }
                }
                for (Map<String,Entity> entities:deletes.values()) {
                    for (Entity entity:entities.values()) {
                        commandContext.getDbSqlSession().flushDelete(entity);
                    }
                }
                for (Entity entity:updates) {
                    commandContext.getDbSqlSession().flushUpdate(entity);
                }
                insertedList.add(inserts);
                updatedObjects.addAll(updates);
                deletedList.add(deletes);
            } else {
                //加入
                //需要价格判定，不判定，Oid不存在会加入空的hashMap
                insertedList.add(cachedInsert.remove(Oid));
                List<Entity> uList=cachedUpdate.remove(Oid);
                updatedObjects.addAll(uList);
                deletedList.add(cachedDelete.remove(Oid));
            }
        }
        useRedis.insertListToRedis(insertedList);
        //System.out.println("1");
        useRedis.updateToRedis(updatedObjects);
        //System.out.println("2");
        useRedis.deleteListToRedis(deletedList);
        //System.out.println("3");
        //这个如果是deploy，就会有写数据库，不是的话，这个操作不会有写库
        commandContext.getDbSqlSession().justFlush();
        insertedList.clear();
        updatedObjects.clear();
        deletedList.clear();
        //useRedis.removeJedis();
        return "success";
    }

    /**
     * @apiNote 缓存需要写入的数据
     */
    public static void storeInsertToCache(Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects,String Oid) {
        // if (!cachedInsert.containsKey(Oid)) {
        //     cachedInsert.put(Oid,new HashMap<Class<? extends Entity>, Map<String, Entity>>());
        //     cachedInsert.get(Oid).putAll(insertedObjects);
        // } else {
        //     for (Class<? extends Entity> clazz:insertedObjects.keySet()) {
        //         if (!cachedInsert.get(Oid).containsKey(clazz)) {
        //             cachedInsert.get(Oid).put(clazz,new HashMap<String,Entity>());
        //         }
        //         cachedInsert.get(Oid).get(clazz).putAll(insertedObjects.get(clazz));
        //     }
        // }
        Map<Class<? extends Entity>, Map<String, Entity>> temp=new HashMap<Class<? extends Entity>, Map<String, Entity>>();
        temp.putAll(insertedObjects);
        cachedInsert.put(Oid,temp);
    }

    /**
     * @apiNote 缓存需要更新的数据
     */
    public static void storeUpdateToCache(List<Entity> updatedObjects,String Oid) {
        // if (!cachedUpdate.containsKey(Oid)) {
        //     cachedUpdate.put(Oid,new LinkedList<Entity>());
        // }
        // cachedUpdate.get(Oid).addAll(updatedObjects);
        List<Entity> temp=new ArrayList<Entity>();
        temp.addAll(updatedObjects);
        cachedUpdate.put(Oid,temp);
    }

    /**
     * @apiNote 缓存需要删除的数据
     */
    public static void storeDeleteToCache(Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects,String Oid) {
        // if (!cachedDelete.containsKey(Oid)) {
        //     cachedDelete.put(Oid,new HashMap<Class<? extends Entity>, Map<String, Entity>>());
        //     cachedDelete.get(Oid).putAll(deletedObjects);
        // } else {
        //     for (Class<? extends Entity> clazz:deletedObjects.keySet()) {
        //         if (!cachedDelete.get(Oid).containsKey(clazz)) {
        //             cachedDelete.get(Oid).put(clazz,new HashMap<String,Entity>());
        //         }
        //         cachedDelete.get(Oid).get(clazz).putAll(deletedObjects.get(clazz));
        //     }
        // }
        Map<Class<? extends Entity>, Map<String, Entity>> temp=new HashMap<Class<? extends Entity>, Map<String, Entity>>();
        temp.putAll(deletedObjects);
        cachedDelete.put(Oid,temp);
    }

    /**
     * @apiNote 缓存所有数据
     */
    public static void storeDataToCache(Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects,List<Entity> updatedObjects,
    Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects,String Oid,String businessData) {
        //处理数据获得cachedResponse结构体
        cachedResponse response=new cachedResponse();
        response.setServiceUrls(Context.getCommandContext().getServiceUrls());
        response.setOid(Oid);
        response.setBusinessData(businessData);
        response.setWriteSetJson(writeSetExtractor.extract(insertedObjects, updatedObjects, deletedObjects));
        response.setReadSetJson(readSetExtractor.extract());
        if (insertedObjects.isEmpty()&&updatedObjects.isEmpty()&&deletedObjects.isEmpty()) {
            logger.info("this operation has no data to change");
            System.out.println("no entity to update");
            //return;//均为空，没有需要插入或者删除的实体
        }

        if (insertedObjects.containsKey(DeploymentEntityImpl.class)) {
            response.setDeploy(true);
            //一次insert只有一个deployment
            for (Entity entity:insertedObjects.get(DeploymentEntityImpl.class).values()) {
                response.setDeploymentName(((DeploymentEntityImpl)entity).getName());
            }
        } 
        //设置fromTask与toTask
        Map<String,String> fromTasks=new HashMap<>();
        if (deletedObjects.containsKey(TaskEntityImpl.class)) {
            for (Entity entity:deletedObjects.get(TaskEntityImpl.class).values()) {
                TaskEntityImpl fromTask=(TaskEntityImpl)entity;
                fromTasks.put(fromTask.getName(),fromTask.getId());
            }
        }
        response.setFromTask(fromTasks);
        Map<String,String> toTasks=new HashMap<>();
        if (insertedObjects.containsKey(TaskEntityImpl.class)) {
            for (Entity entity:insertedObjects.get(TaskEntityImpl.class).values()) {
                TaskEntityImpl toTask=(TaskEntityImpl)entity;
                toTasks.put(toTask.getName(),toTask.getId());
            }
        }
        response.setToTasks(toTasks);


        //将数据存入cache
        storeInsertToCache(insertedObjects, Oid);
        storeUpdateToCache(updatedObjects, Oid);
        storeDeleteToCache(deletedObjects, Oid);
        cachedResponse.put(Oid,response);
    }

    //删除并返回
    public static cachedResponse getWorkflowResponse(String Oid) {
        cachedResponse response;
        // synchronized (cachedResponse) {
        //     response=cachedResponse.remove(Oid);
        // }
        response=cachedResponse.remove(Oid);
        return response;
    }


}
