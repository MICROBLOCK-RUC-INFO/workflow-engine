package org.activiti.engine.impl.db.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteOperation;
import org.activiti.engine.impl.db.cache.oidEvents;
import org.activiti.engine.impl.db.workflowClass.typeTransfer;
import org.activiti.engine.impl.db.workflowClass.proto.entity.simpleEntity.entity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.impl.persistence.entity.PropertyEntityImpl;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.springframework.data.redis.core.StringRedisTemplate;



public  class useRedis {
    public final static String TaskClass="class org.activiti.engine.impl.persistence.entity.TaskEntityImpl";
    public final static String VariableClass="class org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl";
    public final static String ExecutionClass="class org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl";
    public final static String ProcessDefinitionClass="class org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl";
    public final static String DeploymentClass="class org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl";
    public final static String ResourceClass="class org.activiti.engine.impl.persistence.entity.ResourceEntityImpl";
    public final static String PropertyClass="class org.activiti.engine.impl.persistence.entity.PropertyEntityImpl";
    public final static String EventClass="class org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityImpl";
    public final static Set<String> listfieldMap=new HashSet<>(Arrays.asList("selectTasksByExecutionId",
                                                                        "selectTasksByParentTaskId",
                                                                        "selectVariablesByTaskId",
                                                                        "selectVariablesByExecutionId",
                                                                        "selectChildExecutionsByProcessInstanceId",
                                                                        "selectExecutionsByParentExecutionId",
                                                                        "selectResourcesByDeploymentId",
                                                                        "selectEventSubscriptionsByNameAndExecution",
                                                                        "selectEventSubscriptionsByExecution",
                                                                        "selectEventSubscriptionsByProcessInstanceTypeAndActivity",
                                                                        "selectInactiveExecutionsInActivityAndProcessInstance"));
    
    static public StringRedisTemplate stringRedisTemplate=new redisUtil().getstringRedisTemplate();
    private static final ThreadLocal<Map<String,String>> threadLocalCache=new ThreadLocal<Map<String,String>>();
    private static final ThreadLocal<List<String>> threadLocalReadList=new ThreadLocal<List<String>>();
    //private static final JedisPool jedisPool=redisUtil.getJedisPool();
    // private static final Jedis mutilSetJedis =redisUtil.getMutilSetJedis();
    // private static final Jedis singleSetJedis =redisUtil.getSingleSetJedis();
    //private static final Jedis getJedis =redisUtil.getGetJedis();
    // private static final Map<String,Jedis> getJedisMap=redisUtil.getGetJedisMap();
    // private static final Jedis deleteJedis =redisUtil.getDeleteJedis();
    // private static final Jedis entityIdJedis =redisUtil.getEntityIdJedis();
    // private static final Jedis otherJedis =redisUtil.getOtherJedis();
    private static long idLimit=20L;
    private static long nowId=20L;
    //用于取id加锁的对象
    private static Long idMutex=-1L;

    public static void test () {
        
    }

    public static void initRedisNextId() {
        if (!stringRedisTemplate.hasKey("ActivityEntityId")) {
            stringRedisTemplate.opsForValue().set("ActivityEntityId","20");
        } else {
            idLimit=Long.valueOf(stringRedisTemplate.opsForValue().get("ActivityEntityId")).longValue();
            nowId=idLimit;
        }
        // if (!otherJedis.exists("ActivityEntityId")) {
        //     synchronized (singleSetJedis) {
        //         singleSetJedis.set("ActivityEntityId","20");
        //     }
        // } else {
        //     idLimit=Long.valueOf(otherJedis.get("ActivityEntityId"));
        //     nowId=Long.valueOf(idLimit.longValue());
        // }
    }

    public static Map<String,String> getReadEntities() {
        return threadLocalCache.get();
    }

    public static void threadCacheRemove() {
        threadLocalCache.remove();
    }

    @SuppressWarnings("null")
    public static String getNextId() {
        String res=null;
        synchronized (idMutex) {
            if (nowId>=idLimit) {
                //这里没做null值判断
                idLimit=stringRedisTemplate.opsForValue().increment("ActivityEntityId", 10000).longValue();
                //idLimit=entityIdJedis.incrBy("ActivityEntityId", 10000);
            }
            res=String.valueOf(nowId);
            nowId+=1L;
        }
        return res;
    }

    public static void initRedisFieldMap() {
        //Set<String> keys=otherJedis.keys("*");
        Set<String> keys=stringRedisTemplate.keys("*");
        for (String key:keys) {
            if (!key.equals("ActivityEntityId")) {
                // String res=null;
                // synchronized (otherJedis) {
                //     res=otherJedis.get(key);
                // }
                handleEntityFieldMap((Entity)streamToEntity(stringRedisTemplate.opsForValue().get(key)));
            }
        }
    }

    public static void deleteListToRedis(List<Map<Class<? extends Entity>, Map<String, Entity>>> deletedList) {
        List<String> removeList=new LinkedList<>();
        for (Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects:deletedList) {
            if (deletedObjects.isEmpty()) {
                continue;
            }
            for (Class<? extends Entity> clazz:deletedObjects.keySet()) {
                StringBuilder stringBuilder=new StringBuilder();
                stringBuilder.append(typeTransfer.getSimpleType.get(clazz.toString())).append('-');
                int prefixLength=stringBuilder.length();
                //String prefix=typeTransfer.getSimpleType.get(clazz.toString())+"-";
                Map<String,Entity> entityMap=deletedObjects.get(clazz);
                removeDeleteObjectsFieldMap(clazz,entityMap.values());
                for (String entityId:entityMap.keySet()) {
                    stringBuilder.append(entityId);
                    //String key=prefix+entityId;
                    removeList.add(stringBuilder.toString());
                    stringBuilder.setLength(prefixLength);
                }
            }
        }
        // if (removeList.isEmpty()) return;
        // String[] deleteKeys=removeList.toArray(new String[removeList.size()]);
        // synchronized (deleteJedis) {
        //     deleteJedis.del(deleteKeys);
        // }
        stringRedisTemplate.delete(removeList);
        removeList.clear();
    }

    public static void deleteToRedis(Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects) {
        if (deletedObjects.isEmpty()) {
            return;
        }
        List<String> removeList=new LinkedList<>();
        for (Class<? extends Entity> clazz:deletedObjects.keySet()) {
            String prefix=typeTransfer.getSimpleType.get(clazz.toString())+"-";
            Map<String,Entity> entityMap=deletedObjects.get(clazz);
            removeDeleteObjectsFieldMap(clazz,entityMap.values());
            for (String entityId:entityMap.keySet()) {
                String key=prefix+entityId;
                removeList.add(key);
            }
        }
        // if (removeList.isEmpty()) return;
        // String[] deleteKeys=removeList.toArray(new String[removeList.size()]);
        // synchronized (deleteJedis) {
        //     deleteJedis.del(deleteKeys);
        // }
        stringRedisTemplate.delete(removeList);
        removeList.clear();
    }

    public static void insertListToRedis(List<Map<Class<? extends Entity>, Map<String, Entity>>> insertedList) {
        Map<String,String> setMap=new HashMap<>();
        //List<String> keyValueList=new ArrayList<>();
        for (Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects:insertedList) {
            if (insertedObjects.isEmpty()) {
                continue;
            }
            //这里可能要改用stringbuilder，防止频繁创建新对象，造成内存抖动
            for (Class<? extends Entity> clazz:insertedObjects.keySet()) {
                //System.out.println("4");
                StringBuilder stringBuilder=new StringBuilder();
                stringBuilder.append(typeTransfer.getSimpleType.get(clazz.toString())).append('-');
                int prefixLength=stringBuilder.length();
                //String prefix=typeTransfer.getSimpleType.get(clazz.toString())+"-";
                Map<String,Entity> entityMap=insertedObjects.get(clazz);
                //System.out.println("5");
                handleEntitiesFieldMap(entityMap.values());
                //System.out.println("6");
                for (String entityId:entityMap.keySet()) {
                    stringBuilder.append(entityId);
                    //String key=prefix+entityId;
                    //keyValueList.add(stringBuilder.toString());
                    //keyValueList.add(entityToStream(entityMap.get(entityId)));
                    setMap.put(stringBuilder.toString(),entityToStream(entityMap.get(entityId)));
                    stringBuilder.setLength(prefixLength);
                }
            }
        }
        // if (keyValueList.isEmpty()) return;
        // String[] keyValue=keyValueList.toArray(new String[keyValueList.size()]);
        // synchronized (mutilSetJedis) {
        //     mutilSetJedis.mset(keyValue);
        // }
        //keyValueList.clear();
        stringRedisTemplate.opsForValue().multiSet(setMap);
        setMap.clear();
    }

    public static void insertToRedis(Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects,boolean isDeploy) {

    }

    public static void updateToRedis(List<Entity> updatedObjects,boolean isDeploy) {
        
    }

    //必须先与update
    public static void insertToRedis(Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects) {
        if (insertedObjects.isEmpty()) {
            return;
        }
        Map<String,String> setMap=new HashMap<>();
        //List<String> keyValueList=new ArrayList<>();
        for (Class<? extends Entity> clazz:insertedObjects.keySet()) {
            String prefix=typeTransfer.getSimpleType.get(clazz.toString())+"-";
            Map<String,Entity> entityMap=insertedObjects.get(clazz);
            handleEntitiesFieldMap(entityMap.values());
            for (String entityId:entityMap.keySet()) {
                String key=prefix+entityId;
                //keyValueList.add(key);
                //keyValueList.add(entityToStream(entityMap.get(entityId)));
                setMap.put(key,entityToStream(entityMap.get(entityId)));
            }
        }
        // if (keyValueList.isEmpty()) return;
        // String[] keyValue=keyValueList.toArray(new String[keyValueList.size()]);
        // synchronized (mutilSetJedis) {
        //     mutilSetJedis.mset(keyValue);
        // }
        //keyValueList.clear();
        stringRedisTemplate.opsForValue().multiSet(setMap);
        setMap.clear();
    }

    public static void updateToRedis(List<Entity> updatedObjects) {
        if (updatedObjects.isEmpty()) {
            return;
        }
        Map<String,String> setMap=new HashMap<>();
        //List<String> keyValueList=new ArrayList<>();
        //这里也使用stringbuilder,防止频繁建立对象
        StringBuilder stringBuilder=new StringBuilder();
        for (Entity entity:updatedObjects) {
            Class<? extends Entity> clazz=entity.getClass();
            if (clazz.toString().equals(PropertyClass)) {
                updateRedisProperyData(entity);
            } else {
                //先处理字段映射
                handleEntityFieldMap(entity);
                stringBuilder.append(typeTransfer.getSimpleType.get(clazz.toString())).append('-').append(entity.getId());
                //keyValueList.add(stringBuilder.toString());
                //keyValueList.add(entityToStream(entity));
                setMap.put(stringBuilder.toString(),entityToStream(entity));
                stringBuilder.setLength(0); 
            }
        }
        // if (keyValueList.isEmpty()) return;
        // String[] keyValue=keyValueList.toArray(new String[keyValueList.size()]);
        // synchronized (mutilSetJedis) {
        //     mutilSetJedis.mset(keyValue);
        // }
        //keyValueList.clear();
        stringRedisTemplate.opsForValue().multiSet(setMap);
        setMap.clear();
    }

    //有这个东西，但是暂时没有做
    //public static void bulkDeleteOperationToRedis(){}



    //sunzhouxing:更新redis
    public static void updateRedisProperyData(Entity entity) {
        String key=typeTransfer.getSimpleType.get(PropertyClass)+"-"+((PropertyEntityImpl)entity).getName();
        // synchronized (singleSetJedis) {
        //     singleSetJedis.set(key, entityToStream(entity));
        // }
        stringRedisTemplate.opsForValue().set(key, entityToStream(entity));
    }
 




    public static void entityToRedis(Entity entity) {
        Class<? extends Entity> clazz=entity.getClass();
        String key;
        if (clazz.equals(PropertyEntityImpl.class)) 
            key=typeTransfer.getSimpleType.get(clazz.toString())+"-"+((PropertyEntityImpl)entity).getName();
        else
            key=typeTransfer.getSimpleType.get(clazz.toString())+"-"+entity.getId();
        stringRedisTemplate.opsForValue().set(key, entityToStream(entity));
    }


    //将list<entity>写入redis
    public static void entitiesToRedisCache(List<Entity> entities) {
        try {
            if(entities.isEmpty()) {
                return;
            }
            Map<String,String> key_value=new HashMap<>();
            //List<String> keyValueList=new ArrayList<>();
            Class<? extends Entity> clazz=entities.get(0).getClass();
            String prefix=typeTransfer.getSimpleType.get(clazz.toString())+"-";
            for (Entity entity:entities) {
                String key;
                if (clazz.equals(PropertyEntityImpl.class)) {
                    key=prefix+((PropertyEntityImpl)entity).getName();
                } else {
                    key=prefix+entity.getId();
                }
                //keyValueList.add(key);
                //keyValueList.add(entityToStream(entity));
                key_value.put(key,entityToStream(entity));
                if (key_value.size()>=5000) {
                    // String[] keyValue=keyValueList.toArray(new String[keyValueList.size()]);
                    // synchronized (mutilSetJedis) {
                    //     mutilSetJedis.mset(keyValue);
                    // }
                    // keyValueList.clear();
                    stringRedisTemplate.opsForValue().multiSet(key_value);
                    key_value.clear();
                }//切片插入防止过大。
            }
            if (!key_value.isEmpty()) {
                // String[] keyValue=keyValueList.toArray(new String[keyValueList.size()]);
                // synchronized (mutilSetJedis) {
                //     mutilSetJedis.mset(keyValue);
                // }
                // keyValueList.clear();
                stringRedisTemplate.opsForValue().multiSet(key_value);
                key_value.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }     
    }


    public static Object findByIdInRedis(String clazz,String entityId) {
        if (threadLocalCache.get()==null) {
            threadLocalCache.set(new LinkedHashMap<String,String>());
        }
        String key=typeTransfer.getSimpleType.get(clazz.toString())+"-"+entityId;
        //String value=stringRedisTemplate.opsForValue().get(key);
        //避免多次查询redis使用了threadLocal作为缓存
        if (!threadLocalCache.get().containsKey(key)) {
            threadLocalCache.get().put(key,stringRedisTemplate.opsForValue().get(key));
        }
        return streamToEntity(threadLocalCache.get().get(key));
    }


    // public void putEntitiesIntoRedis(String fcn,String key) {

    // }
    //将从redis内读取的数据转换成entity
    public static Object streamToEntity(String value) {
        try {
            if (value==null) return null;
            return streamToEntity(Base64.getDecoder().decode(value)); 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object streamToEntity(byte[] value) {
        try {
            if (value==null) {
                return null;
            }
            ByteArrayInputStream reader = new ByteArrayInputStream(value);
            ObjectInputStream in=new ObjectInputStream(reader);
            Object res=in.readObject();
            in.close();
            reader.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //将entity转换成string
    public static <T> String entityToStream(Map<String, List<T>> deletedList) {
        try {
            ByteArrayOutputStream value=new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(value);
            out.writeObject(deletedList);
            String res=Base64.getEncoder().encodeToString(value.toByteArray());
            out.flush();
            out.close();
            value.flush();
            value.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String entityToStream(Object object) {
        try {
            ByteArrayOutputStream value=new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(value);
            out.writeObject(object);
            String res=Base64.getEncoder().encodeToString(value.toByteArray());
            out.flush();
            out.close();
            value.flush();
            value.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String entityToStream(List<Entity> objects) {
        try {
            ByteArrayOutputStream value=new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(value);
            out.writeObject(objects);
            String res=Base64.getEncoder().encodeToString(value.toByteArray());
            out.flush();
            out.close();
            value.flush();
            value.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //处理deleteObjects删除时的关键字段映射，返回需要的删除的key的list
    public static void removeDeleteObjectsFieldMap(Class<? extends Entity> clazz,Collection<Entity> entities) {
        if (entities.isEmpty()) return;
        Entity entity=entities.iterator().next();
        if (entity instanceof TaskEntityImpl) {
            removeTaskEntitiesFieldMap(entities);
        } else if (entity instanceof VariableInstanceEntityImpl) {
            removeVariableInstanceEntitiesFieldMap(entities);
        } else if (entity instanceof ExecutionEntityImpl) {
            removeExecutionEntitiesFieldMap(entities);
        } else if (entity instanceof ProcessDefinitionEntityImpl) {
            removeProcessDefinitionEntitiesFieldMap(entities);
        } else if (entity instanceof DeploymentEntityImpl) {
            removeDeploymentEntitiesFieldMap(entities);
        } else if (entity instanceof ResourceEntityImpl) {
            removeResourceEntitiesFieldMap(entities);
        } else if (entity instanceof EventSubscriptionEntityImpl) {
            removeEventSubSrcEntitiesFieldMap(entities);
        }
        // switch (clazz.toString()) {
        //     case TaskClass:
        //         removeTaskEntitiesFieldMap(entities);
        //         break;
        //     case VariableClass:
        //         removeVariableInstanceEntitiesFieldMap(entities);
        //         break;
        //     case ExecutionClass:
        //         removeExecutionEntitiesFieldMap(entities);
        //         break;
        //     case ProcessDefinitionClass:
        //         removeProcessDefinitionEntitiesFieldMap(entities);
        //         break;
        //     case DeploymentClass:
        //         removeDeploymentEntitiesFieldMap(entities);
        //         break;
        //     case ResourceClass:
        //         removeResourceEntitiesFieldMap(entities);
        //         break;
        //     case EventClass:
        //         removeEventSubSrcEntitiesFieldMap(entities);
        //         break; 
        //     default:
        //         return;
        // }
    }
    //处理entity关键字段的映射，这样可以用deploymentId查找ResourceEntity
    public static void handleEntitiesFieldMap(Collection<Entity> entities) {
        if (entities.isEmpty()) {
            return;
        }
        //System.out.println("7");
        Entity entity=entities.iterator().next();
        if (entity instanceof TaskEntityImpl) {
            //System.out.println("8");
            handleTaskEntitiesFieldMap(entities);
        } else if (entity instanceof VariableInstanceEntityImpl) {
            //System.out.println("9");
            handleVariableInstanceEntitiesFieldMap(entities);
        } else if (entity instanceof ExecutionEntityImpl) {
            //System.out.println("10");
            handleExecutionEntitiesFieldMap(entities);
        } else if (entity instanceof ProcessDefinitionEntityImpl) {
            //System.out.println("11");
            handleProcessDefinitionEntitiesFieldMap(entities);
        } else if (entity instanceof DeploymentEntityImpl) {
            //System.out.println("12");
            handleDeploymentEntitiesFieldMap(entities);
        } else if (entity instanceof ResourceEntityImpl) {
            //System.out.println("13");
            handleResourceEntitiesFieldMap(entities);
        } else if (entity instanceof EventSubscriptionEntityImpl) {
            //System.out.println("14");
            handleEventSubSrcEntitiesFieldMap(entities);
        }
        // switch (clazz) {
        //     case TaskClass:
        //     handleTaskEntitiesFieldMap(entities);
        //     break;
        //     case VariableClass:
        //     handleVariableInstanceEntitiesFieldMap(entities);
        //     break;
        //     case ExecutionClass:
        //     handleExecutionEntitiesFieldMap(entities);
        //     break;
        //     case ProcessDefinitionClass:
        //     handleProcessDefinitionEntitiesFieldMap(entities);
        //     break;
        //     case DeploymentClass:
        //     handleDeploymentEntitiesFieldMap(entities);
        //     break;
        //     case ResourceClass:
        //     handleResourceEntitiesFieldMap(entities);
        //     break;
        //     case EventClass:
        //     handleEventSubSrcEntitiesFieldMap(entities);
        //     break;
        //     default:
        //     return;
        // }
    }

    public static void handleEntityFieldMap(Entity entity) {
        //Class<? extends Entity> clazz=entity.getClass();
        if (entity instanceof TaskEntityImpl) {
            handleTaskEntityFieldMap(entity);
        } else if (entity instanceof VariableInstanceEntityImpl) {
            handleVariableInstanceEntityFieldMap(entity);
        } else if (entity instanceof ExecutionEntityImpl) {
            handleExecutionEntityFieldMap(entity);
        } else if (entity instanceof ProcessDefinitionEntityImpl) {
            handleProcessDefinitionEntityFieldMap(entity);
        } else if (entity instanceof DeploymentEntityImpl) {
            handleDeploymentEntityFieldMap(entity);
        } else if (entity instanceof ResourceEntityImpl) {
            handleResourceEntityFieldMap(entity);
        } else if (entity instanceof EventSubscriptionEntityImpl) {
            handleEventSubSrcEntityFieldMap(entity);
        }
        // switch (clazz.toString()) {
        //     case TaskClass:
        //     handleTaskEntityFieldMap(entity);
        //     break;
        //     case VariableClass:
        //     handleVariableInstanceEntityFieldMap(entity);
        //     break;
        //     case ExecutionClass:
        //     handleExecutionEntityFieldMap(entity);
        //     break;
        //     case ProcessDefinitionClass:
        //     handleProcessDefinitionEntityFieldMap(entity);
        //     break;
        //     case DeploymentClass:
        //     handleDeploymentEntityFieldMap(entity);
        //     break;
        //     case ResourceClass:
        //     handleResourceEntityFieldMap(entity);
        //     break;
        //     case EventClass:
        //     handleEventSubSrcEntityFieldMap(entity);
        //     break;
        //     default:
        //     return;
        // }
    }

    public static void handleTaskEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleTaskEntityFieldMap(entity);
        }
    }

    public static void handleTaskEntityFieldMap(Entity entity) {
        TaskEntity taskEntity=(TaskEntity)entity;
        if (taskEntity.getExecutionId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.TASK,entityFieldMap.Field_ExecutionId,taskEntity.getExecutionId(),taskEntity.getId());
        }
        if (taskEntity.getProcessInstanceId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.TASK,entityFieldMap.Field_ProcessInstanceId,taskEntity.getProcessInstanceId(),taskEntity.getId());
        }
        if (taskEntity.getParentTaskId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.TASK,entityFieldMap.Field_ParentTaskId,taskEntity.getParentTaskId(),taskEntity.getId());
        }
    }

    public static void removeTaskEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            TaskEntity taskEntity=(TaskEntity)entity;
            if (taskEntity.getExecutionId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.TASK,entityFieldMap.Field_ExecutionId,taskEntity.getExecutionId(),taskEntity.getId());
            }
            if (taskEntity.getProcessInstanceId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.TASK,entityFieldMap.Field_ProcessInstanceId,taskEntity.getProcessInstanceId(),taskEntity.getId());
            }
            if (taskEntity.getParentTaskId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.TASK,entityFieldMap.Field_ParentTaskId,taskEntity.getParentTaskId(),taskEntity.getId());
            }
        }
    }
    

    public static void handleVariableInstanceEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleVariableInstanceEntityFieldMap(entity);     
        }
    }

    public static void handleVariableInstanceEntityFieldMap(Entity entity) {
        VariableInstanceEntity variableInstanceEntity=(VariableInstanceEntity)entity;
        if (variableInstanceEntity.getExecutionId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.VARIABLE,entityFieldMap.Field_ExecutionId,variableInstanceEntity.getExecutionId(),variableInstanceEntity.getId());
        }
        if (variableInstanceEntity.getTaskId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.VARIABLE,entityFieldMap.Field_TaskId,variableInstanceEntity.getTaskId(),variableInstanceEntity.getId());
        }  
    }

    public static void removeVariableInstanceEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            VariableInstanceEntity variableInstanceEntity=(VariableInstanceEntity)entity;
            if (variableInstanceEntity.getExecutionId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.VARIABLE,entityFieldMap.Field_ExecutionId,variableInstanceEntity.getExecutionId(),variableInstanceEntity.getId());
            }
            if (variableInstanceEntity.getTaskId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.VARIABLE,entityFieldMap.Field_TaskId,variableInstanceEntity.getTaskId(),variableInstanceEntity.getId());
            }            
        }
    }


    public static void handleExecutionEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleExecutionEntityFieldMap(entity);
        }
    }

    public static void handleExecutionEntityFieldMap(Entity entity) {
        ExecutionEntity executionEntity=(ExecutionEntity)entity;
        if (executionEntity.getProcessInstanceId()!=null) {
            //System.out.println("15");
            entityFieldMap.setEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_ProcessInstanceId,executionEntity.getProcessInstanceId(),executionEntity.getId());
            if (executionEntity.getActivityId()!=null&&!executionEntity.isActive()) {
                entityFieldMap.setEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.compositeKey_ActivityId_ProInstId_IsActive,
                entityFieldMap.compositeKey(executionEntity.getActivityId(),executionEntity.getProcessInstanceId(),String.valueOf(executionEntity.isActive())),
                executionEntity.getId());
            }
        }
        if (executionEntity.getName()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_Name,executionEntity.getName(),executionEntity.getId());
        }
        if (executionEntity.getSuperExecutionId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_SuperExec,executionEntity.getSuperExecutionId(),executionEntity.getId());
        }
        if (executionEntity.getParentId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_ParentExecutionId,executionEntity.getParentId(),executionEntity.getId());
        }
    }


    public static void removeExecutionEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            ExecutionEntity executionEntity=(ExecutionEntity)entity;
            if (executionEntity.getProcessInstanceId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_ProcessInstanceId,executionEntity.getProcessInstanceId(),executionEntity.getId());
                if (executionEntity.getActivityId()!=null&&!executionEntity.isActive()) {
                    entityFieldMap.removeEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.compositeKey_ActivityId_ProInstId_IsActive,
                    entityFieldMap.compositeKey(executionEntity.getActivityId(),executionEntity.getProcessInstanceId(),String.valueOf(executionEntity.isActive())),
                    executionEntity.getId());
                }
            }
            if (executionEntity.getName()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_Name,executionEntity.getName(),executionEntity.getId());
            }
            if (executionEntity.getSuperExecutionId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_SuperExec,executionEntity.getSuperExecutionId(),executionEntity.getId());
            }
            if (executionEntity.getParentId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.EXECUTION,entityFieldMap.Field_ParentExecutionId,executionEntity.getParentId(),executionEntity.getId());
            }
        }
    }

    public static void handleProcessDefinitionEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleProcessDefinitionEntityFieldMap(entity);
        }
    }

    public static void handleProcessDefinitionEntityFieldMap(Entity entity) {
        ProcessDefinitionEntity processDefinitionEntity=(ProcessDefinitionEntity)entity;
        if (processDefinitionEntity.getDeploymentId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.PROCDEF,entityFieldMap.Field_DeploymentId,processDefinitionEntity.getDeploymentId(),processDefinitionEntity.getId());
        }
        if (processDefinitionEntity.getKey()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.PROCDEF,entityFieldMap.Field_ProcessDefinitionKey,processDefinitionEntity.getKey(),processDefinitionEntity.getId());
        }
    }


    public static void removeProcessDefinitionEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            ProcessDefinitionEntity processDefinitionEntity=(ProcessDefinitionEntity)entity;
            if (processDefinitionEntity.getDeploymentId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.PROCDEF,entityFieldMap.Field_DeploymentId,processDefinitionEntity.getDeploymentId(),processDefinitionEntity.getId());
            }
            if (processDefinitionEntity.getKey()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.PROCDEF,entityFieldMap.Field_ProcessDefinitionKey,processDefinitionEntity.getKey(),processDefinitionEntity.getId());
            }
        }
    }

    public static void handleDeploymentEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleDeploymentEntityFieldMap(entity);
        }
    }

    public static void handleDeploymentEntityFieldMap(Entity entity) {
        DeploymentEntity deploymentEntity=(DeploymentEntity)entity;
        if (deploymentEntity.getName()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.DEPLOYMENT,entityFieldMap.Field_Name,deploymentEntity.getName(),deploymentEntity.getId());
        }
    }

    public static void removeDeploymentEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            DeploymentEntity deploymentEntity=(DeploymentEntity)entity;
            if (deploymentEntity.getName()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.DEPLOYMENT,entityFieldMap.Field_Name,deploymentEntity.getName(),deploymentEntity.getId());
            }
        }
    }

    public static void handleResourceEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleResourceEntityFieldMap(entity);
        }
    }

    public static void handleResourceEntityFieldMap(Entity entity) {
        ResourceEntity resourceEntity=(ResourceEntity)entity;
        if (resourceEntity.getDeploymentId()!=null) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.RESOURCE,entityFieldMap.Field_DeploymentId,resourceEntity.getDeploymentId(),resourceEntity.getId());
        }
    }

    public static void removeResourceEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            ResourceEntity resourceEntity=(ResourceEntity)entity;
            if (resourceEntity.getDeploymentId()!=null) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.RESOURCE,entityFieldMap.Field_DeploymentId,resourceEntity.getDeploymentId(),resourceEntity.getId());
            }
        }
    }

    public static void handleEventSubSrcEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            handleEventSubSrcEntityFieldMap(entity);            
        }
    }

    public static void handleEventSubSrcEntityFieldMap(Entity entity) {
        EventSubscriptionEntity eventSubscriptionEntity=(EventSubscriptionEntity)entity;
        // if (eventSubscriptionEntity.getEventType()!=null) {
        //     entityFieldMap.setEntityFieldMap(entityFieldMap.EVENT,entityFieldMap.Field_EventType,eventSubscriptionEntity.getEventType(),eventSubscriptionEntity.getId());
        // }
        // if (eventSubscriptionEntity.getEventName()!=null) {
        //     entityFieldMap.setEntityFieldMap(entityFieldMap.EVENT,entityFieldMap.Field_EventName,eventSubscriptionEntity.getEventName(),eventSubscriptionEntity.getId());
        // }
        if (eventSubscriptionEntity.getEventType().equals("message")) {
            //System.out.println("16");
            if (eventSubscriptionEntity.getExecutionId()!=null) {
                oidEvents.addOidEventMap(eventSubscriptionEntity.getOid(), eventSubscriptionEntity.getEventName(), eventSubscriptionEntity.getExecutionId());
                entityFieldMap.setEntityFieldMap(entityFieldMap.EVENT, entityFieldMap.compositeKey_EventType_EventName_ExecutionId,
                entityFieldMap.compositeKey(eventSubscriptionEntity.getEventType(),eventSubscriptionEntity.getEventName(),eventSubscriptionEntity.getExecutionId()),
                eventSubscriptionEntity.getId());
                entityFieldMap.setEntityFieldMap(entityFieldMap.EVENT,entityFieldMap.Field_ExecutionId,eventSubscriptionEntity.getExecutionId(),eventSubscriptionEntity.getId());
            }
        }
        if (eventSubscriptionEntity.getEventType().equals("compensate")) {
            entityFieldMap.setEntityFieldMap(entityFieldMap.EVENT,entityFieldMap.Field_ExecutionId,eventSubscriptionEntity.getExecutionId(),eventSubscriptionEntity.getId());
            entityFieldMap.setEntityFieldMap(entityFieldMap.EVENT, entityFieldMap.compositeKey_EventType_ProInstId_ActivityId, entityFieldMap.compositeKey(eventSubscriptionEntity.getEventType(),
            eventSubscriptionEntity.getProcessInstanceId(),eventSubscriptionEntity.getActivityId()), eventSubscriptionEntity.getId());
        }
    }

    public static void removeEventSubSrcEntitiesFieldMap(Collection<Entity> entities) {
        for (Entity entity:entities) {
            EventSubscriptionEntity eventSubscriptionEntity=(EventSubscriptionEntity)entity;
            if (eventSubscriptionEntity.getEventType().equals("message")) {
                if (eventSubscriptionEntity.getExecutionId()!=null) {
                    oidEvents.deleteOidEventMap(eventSubscriptionEntity.getOid(), eventSubscriptionEntity.getEventName(), eventSubscriptionEntity.getExecutionId());
                    entityFieldMap.removeEntityFieldMap(entityFieldMap.EVENT, entityFieldMap.compositeKey_EventType_EventName_ExecutionId,
                                    entityFieldMap.compositeKey(eventSubscriptionEntity.getEventType(),eventSubscriptionEntity.getEventName(),eventSubscriptionEntity.getExecutionId()),
                                    eventSubscriptionEntity.getId());
                    entityFieldMap.removeEntityFieldMap(entityFieldMap.EVENT,entityFieldMap.Field_ExecutionId,eventSubscriptionEntity.getExecutionId(),eventSubscriptionEntity.getId());
                }
            }
            if (eventSubscriptionEntity.getEventType().equals("compensate")) {
                entityFieldMap.removeEntityFieldMap(entityFieldMap.EVENT,entityFieldMap.Field_ExecutionId,eventSubscriptionEntity.getExecutionId(),eventSubscriptionEntity.getId());
                entityFieldMap.removeEntityFieldMap(entityFieldMap.EVENT, entityFieldMap.compositeKey_EventType_ProInstId_ActivityId, entityFieldMap.compositeKey(eventSubscriptionEntity.getEventType(),
                eventSubscriptionEntity.getProcessInstanceId(),eventSubscriptionEntity.getActivityId()), eventSubscriptionEntity.getId());
            }
        }
    }


    public static List<Object> findEventSubScriptionEntityByNameAndExecution(String eventType,String eventName,String executionId) {
        List<Object> eventSubScriptionList=new LinkedList<>();
        Set<String> res=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EVENT, entityFieldMap.compositeKey_EventType_EventName_ExecutionId, 
                                                    entityFieldMap.compositeKey(eventType,eventName,executionId));
        for (String id:res) {
            eventSubScriptionList.add(findByIdInRedis(EventClass,id));
        }
        return eventSubScriptionList;
    }

    public static List<Object> findEventSubScriptionEntityByProcessInstanceTypeAndActivity(String eventType,String processInstanceId,String activityId) {
        List<Object> eventSubScriptionList=new LinkedList<>();
        Set<String> res=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EVENT, entityFieldMap.compositeKey_EventType_ProInstId_ActivityId,
                                                    entityFieldMap.compositeKey(eventType,processInstanceId,activityId));
        for (String id:res) {
            eventSubScriptionList.add(findByIdInRedis(EventClass,id));
        }
        return eventSubScriptionList;
    }

    public static List<Object> findEventSubScriptionEntityByExecution(String executionId) {
        List<Object> eventSubScriptionList=new LinkedList<>();
        Set<String> resByExecutionId=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EVENT, entityFieldMap.Field_ExecutionId, executionId);
        for (String id:resByExecutionId) {
            eventSubScriptionList.add(findByIdInRedis(EventClass,id));
        }
        return eventSubScriptionList;
    }

    public static Object findExecutionEntityBySuperExecId(String superExecId) {
        Set<String> executionId=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EXECUTION,entityFieldMap.Field_SuperExec,superExecId);
        for (String id:executionId) {
            return findByIdInRedis(ExecutionClass, id);
        }
        return null;
    }


    public static List<Object> findInactiveExecutionsInActivityAndProcessInstance(String activityId,String processInstanceId) {
        String fieldValue=entityFieldMap.compositeKey(activityId,processInstanceId,String.valueOf(false));
        List<Object> executionList=new LinkedList<>();
        Set<String> executionId=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EXECUTION,entityFieldMap.compositeKey_ActivityId_ProInstId_IsActive,fieldValue);
        for (String id:executionId) {
            executionList.add(findByIdInRedis(ExecutionClass,id));
        }
        return executionList;
    }

    public static List<Object> findchildExecutionEntitiesByProcessInstanceId(String processInstanceId) {
        Set<String> executionIds=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EXECUTION,entityFieldMap.Field_ProcessInstanceId,processInstanceId);
        List<Object> executionList=new LinkedList<>();
        for (String id:executionIds) {
            executionList.add(findByIdInRedis(ExecutionClass, id));
        }
        return executionList;
    }

    public static List<Object> findExecutionEntitiesByParentId(String parentId) {
        Set<String> executionIds=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.EXECUTION,entityFieldMap.Field_ParentExecutionId,parentId);
        List<Object> executionList=new LinkedList<>();
        for (String id:executionIds) {
            executionList.add(findByIdInRedis(ExecutionClass, id));
        }
        return executionList;
    }

    public static List<Object> findResourceEntitiesByDeploymentId(String deploymentId) {
        Set<String> Resources=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.RESOURCE,entityFieldMap.Field_DeploymentId,deploymentId);
        List<Object> resourceEntities=new LinkedList<>();
        for (String resourceId:Resources) {
            resourceEntities.add(findByIdInRedis(ResourceClass, resourceId));
        }
        return resourceEntities;
    }

    public static List<Object> findTaskByParentTaskId(String parentTaskId) {
        Set<String> taskIds=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.TASK,entityFieldMap.Field_ParentTaskId,parentTaskId);
        List<Object> taskList=new LinkedList<>();
        for (String taskId:taskIds) {
            taskList.add(findByIdInRedis(TaskClass, taskId));
        }
        return taskList;
    }

    public static List<Object> findTasksByExecutionId(String executionId) {
        Set<String> taskIds=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.TASK,entityFieldMap.Field_ExecutionId,executionId);
        List<Object> taskList=new LinkedList<>();
        for (String taskId:taskIds) {
            taskList.add(findByIdInRedis(TaskClass, taskId));
        }
        return taskList;
    }

    public static List<Object> findVariableByTaskId(String taskId) {
        Set<String> variableIds=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.VARIABLE,entityFieldMap.Field_TaskId,taskId);
        List<Object> variableEntities=new LinkedList<>();
        for (String variableId:variableIds) {
            variableEntities.add(findByIdInRedis(VariableClass, variableId));
        }
        return variableEntities;
    }

    public static List<Object> findVariableByExecutionId(String executionId) {
        Set<String> variableIds=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.VARIABLE,entityFieldMap.Field_ExecutionId,executionId);

        List<Object> variableEntities=new LinkedList<>();
        for (String variableId:variableIds) {
            variableEntities.add(findByIdInRedis(VariableClass, variableId));
        }
        return variableEntities;
    }

    public static Entity findLatestProcessDefinitionByKey(String processDefinitionKey) {
        Set<String> sets=entityFieldMap.getEntityIdByFieldValue(entityFieldMap.PROCDEF,entityFieldMap.Field_ProcessDefinitionKey,processDefinitionKey);
        if (sets instanceof LinkedHashSet) {
            LinkedHashSet<String> linkedHashSet=(LinkedHashSet<String>)sets;
            String entityId=linkedHashSet.stream().skip(linkedHashSet.size()-1).findFirst().get();
            return (Entity)findByIdInRedis(ProcessDefinitionClass, entityId);
        } else {
            return null;
        }
    }
}
