package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.Map;

import org.activiti.engine.impl.db.redis.entityFieldMap;
import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.binderImpl;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.tableOperator;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;

public class userTaskBindHandler extends binderImpl implements handler{

    /**
     * @apiNote 这个函数预留给校验用的
     * @return 
     */
    public boolean verify() {
        return false;
    }
    /**
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
            String taskId=String.valueOf(params.get("taskId"));
            TaskEntityImpl task= (TaskEntityImpl)useRedis.findByIdInRedis(useRedis.TaskClass, taskId);
            task.setUserId(value);
            useRedis.entityToRedis(task);
        }
        return bind(oid, key, value);
    }
}
