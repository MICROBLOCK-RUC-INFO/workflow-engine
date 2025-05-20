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

/**
 * @apiNote 读集提取
 */
public class readSetExtractor extends extractor{
    public static String extract() {
        //从UseRedis的缓存中拿到所有读取的数据
        Map<String,String> readEntityMap=useRedis.getReadEntities();
        if (readEntityMap==null) return null;
        List<simpleEntity> readSet=new ArrayList<simpleEntity>();
        Iterator<String> values= readEntityMap.values().iterator();
        while (values.hasNext()) {
            //遍历生成简化版实体
            String value=values.next();
            if (value==null) continue;
            simpleEntity readEntity=handleEntity((Entity)useRedis.streamToEntity(value));
            readEntity.setOpType(simpleEntity.READ);
            readSet.add(readEntity);
        }
        //返回Json字符串
        return jsonTransfer.rwSetToJsonString(readSet);
    }
}
