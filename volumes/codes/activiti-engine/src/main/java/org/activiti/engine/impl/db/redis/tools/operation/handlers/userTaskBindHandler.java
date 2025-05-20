package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.redis.entityFieldMap;
import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operationSelector;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.binderImpl;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.tableOperator;
import org.activiti.engine.impl.db.redis.tools.operation.verify.verifyOperator;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;

/**
 * @apiNote 用户任务动态绑定
 */
public class userTaskBindHandler extends binderImpl implements handler{

    /**
     * @apiNote 获得用户任务分配的用户名，用于创建TaskEntity实体时设置UserId
     * @param oid 表示唯一的工作流实例
     * @param taskName 任务名
     * @return 如果有分配返回对应值，没有绑定则返回null
     */
    public String getUser (String oid,String taskName) {
        return tableOperator.getUser(oid, taskName);
    }

    @Override
    public String simulate(Map<String,Object> params) {
        String oid=String.valueOf(params.get("oid"));
        String key=String.valueOf(params.get("taskName"));
        String value=String.valueOf(params.get("value"));
        return simulateBind(oid, key, value);
    }

    @Override
    public boolean flush(Map<String,Object> params) {
        String oid=String.valueOf(params.get("oid"));
        String key=String.valueOf(params.get("taskName"));
        String value=String.valueOf(params.get("newValue"));
        if (params.containsKey("taskId")) {
            /*
             * 如果有taskId,说明已经存在这个TaskEntity,也就是同时需要将Redis中存储的实体进行修改
             */
            String taskId=String.valueOf(params.get("taskId"));
            TaskEntityImpl task= (TaskEntityImpl)useRedis.findByIdInRedis(useRedis.TaskClass, taskId);
            task.setUserId(value);
            useRedis.entityToRedis(task);
        }
        return bind(oid, key, value);
    }
}
