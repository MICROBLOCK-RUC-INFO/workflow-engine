package org.activiti.engine.impl.db.redis.tools.rwSet.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.db.redis.tools.jackJson.jsonTransfer;
import org.activiti.engine.impl.db.workflowClass.proto.entity.simpleEntity.opType;
import org.activiti.engine.impl.db.workflowClass.proto.rwSet.rwSetList.readSet;
import org.activiti.engine.impl.db.workflowClass.rwSetClass.simpleEntity;
import org.activiti.engine.impl.persistence.entity.Entity;


public class readSetExtractor extends extractor{
    public static String extract() {
        Map<String,String> readEntityMap=useRedis.getReadEntities();
        if (readEntityMap==null) return null;
        List<simpleEntity> readSet=new ArrayList<simpleEntity>();
        Iterator<String> values= readEntityMap.values().iterator();
        while (values.hasNext()) {
            String value=values.next();
            if (value==null) continue;
            simpleEntity readEntity=handleEntity((Entity)useRedis.streamToEntity(value));
            readEntity.setOpType(simpleEntity.READ);
            readSet.add(readEntity);
        }
        return jsonTransfer.rwSetToJsonString(readSet);
    }



}
