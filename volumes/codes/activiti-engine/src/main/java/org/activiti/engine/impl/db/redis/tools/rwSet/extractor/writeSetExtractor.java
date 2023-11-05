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

public class writeSetExtractor extends extractor{
    public static String extract(Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects,List<Entity> updatedObjects,
    Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects) {
        if (insertedObjects.isEmpty()&&deletedObjects.isEmpty()&&updatedObjects.isEmpty()) return null;
        List<simpleEntity> writeSet=new ArrayList<simpleEntity>();
        for (Class<? extends Entity> entityClass : EntityDependencyOrder.INSERT_ORDER) {
            if (!insertedObjects.containsKey(entityClass)) continue;
            Map<String,Entity> map=insertedObjects.get(entityClass);
            if (map.isEmpty()) continue;
            Entity typeEntity= map.entrySet().stream().findFirst().get().getValue();
            handleEntities(typeEntity, map.values().iterator(), writeSet, simpleEntity.WRITE_INSERT);
        }
        for (Class<? extends Entity> entityClass : EntityDependencyOrder.DELETE_ORDER) {
            if (!deletedObjects.containsKey(entityClass)) continue;
            Map<String,Entity> map=deletedObjects.get(entityClass);
            if (map.isEmpty()) continue;
            Entity typeEntity=map.entrySet().stream().findFirst().get().getValue();
            handleEntities(typeEntity, map.values().iterator(), writeSet, simpleEntity.WRITE_DELETE);
        }
        Iterator<Entity> updatedEntityIterator=updatedObjects.iterator();
        while (updatedEntityIterator.hasNext()) {
            simpleEntity updatedEntity=handleEntity(updatedEntityIterator.next());
            updatedEntity.setOpType(simpleEntity.WRITE_UPDATE);
            writeSet.add(updatedEntity);
        }
        return jsonTransfer.rwSetToJsonString(writeSet);
    }
}
