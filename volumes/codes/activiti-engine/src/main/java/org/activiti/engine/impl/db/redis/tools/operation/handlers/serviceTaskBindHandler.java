package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.Map;

import org.activiti.engine.impl.db.redis.tools.operation.taskBind.binderImpl;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.tableOperator;

public class serviceTaskBindHandler extends binderImpl implements handler{

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
     * @return 如果有分配返回对应值是一个json字符串，没有绑定则返回null
     */
    public String getServiceInfo(String oid,String taskName) {
        return tableOperator.getServiceBindInfo(oid, taskName);
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
        return bind(oid, key, value);
    }
}
