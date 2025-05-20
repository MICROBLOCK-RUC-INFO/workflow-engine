package org.activiti.engine.impl.db.redis.tools.rwSet.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.EntityDependencyOrder;
import org.activiti.engine.impl.db.redis.tools.jackJson.jsonTransfer;
import org.activiti.engine.impl.db.workflowClass.proto.entity.simpleEntity.entityType;
import org.activiti.engine.impl.db.workflowClass.proto.entity.simpleEntity.opType;
import org.activiti.engine.impl.db.workflowClass.proto.rwSet.rwSetList.writeSet;
import org.activiti.engine.impl.db.workflowClass.rwSetClass.simpleEntity;
import org.activiti.engine.impl.persistence.entity.Entity;

/**
 * @apiNote 写集提取
 */
public class writeSetExtractor extends extractor{
    public static String extract(Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects,List<Entity> updatedObjects,
    Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects) {
        //无插入，更新，删除的实体，直接返回
        if (insertedObjects.isEmpty()&&deletedObjects.isEmpty()&&updatedObjects.isEmpty()) return null;
        List<simpleEntity> writeSet=new ArrayList<simpleEntity>();
        //遍历插入实体，顺序是Activiti规定的，这跟他数据库写入和依赖有关
        for (Class<? extends Entity> entityClass : EntityDependencyOrder.INSERT_ORDER) {
            //没有或者为空就继续
            if (!insertedObjects.containsKey(entityClass)) continue;
            Map<String,Entity> map=insertedObjects.get(entityClass);
            if (map.isEmpty()) continue;
            //生成简化版的实体并写入写集
            Entity typeEntity= map.entrySet().stream().findFirst().get().getValue();
            handleEntities(typeEntity, map.values().iterator(), writeSet, simpleEntity.WRITE_INSERT);
        }
        for (Class<? extends Entity> entityClass : EntityDependencyOrder.DELETE_ORDER) {
            //没有或者为空就继续
            if (!deletedObjects.containsKey(entityClass)) continue;
            Map<String,Entity> map=deletedObjects.get(entityClass);
            if (map.isEmpty()) continue;
             //生成简化版的实体并写入写集
            Entity typeEntity=map.entrySet().stream().findFirst().get().getValue();
            handleEntities(typeEntity, map.values().iterator(), writeSet, simpleEntity.WRITE_DELETE);
        }
        Iterator<Entity> updatedEntityIterator=updatedObjects.iterator();
        while (updatedEntityIterator.hasNext()) {
             //生成简化版的实体并写入写集
            simpleEntity updatedEntity=handleEntity(updatedEntityIterator.next());
            updatedEntity.setOpType(simpleEntity.WRITE_UPDATE);
            writeSet.add(updatedEntity);
        }
        return jsonTransfer.rwSetToJsonString(writeSet);
    }
}
