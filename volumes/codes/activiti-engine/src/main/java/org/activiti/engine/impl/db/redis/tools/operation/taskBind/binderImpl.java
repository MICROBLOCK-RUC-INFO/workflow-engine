package org.activiti.engine.impl.db.redis.tools.operation.taskBind;

import java.util.Map;

import org.activiti.engine.impl.db.redis.redisUtil;
import org.activiti.engine.impl.db.redis.useRedis;
import org.springframework.data.redis.core.StringRedisTemplate;

public abstract class binderImpl implements binder{
    /**
     * 
     * @param oid 静态分配对应的工作流实例的oid
     * @param staticAllocation key为taskName,value为userId的map,如果staticAllocation=null,会直接返回
     */
    public void staticAllocate(String oid,Map<String,Object> staticAllocation) {
        tableOperator.allocate(oid, staticAllocation);
    }

    /**
     * 
     * @param oid 不为null
     * @param key 不为null
     * @param value 如果是userTask绑定为user,如果是serviceTask绑定则为group
     * @return json字符串 {oid=,taskName=,oldValue=,newValue=,opType=taskBind}
     */
    public String simulateBind(String oid,String key,String value) {
        return tableOperator.simulateBind(oid, key, value);
    }

    /**
     * 
     * @param oid
     * @param key
     * @param value
     * @param oldValue 用来做对比，如果oldValue与查询出来的值不匹配，则认为动态绑定失败,类似cas的想法
     */
    public boolean bind(String oid,String key,String value) {
        return tableOperator.bind(oid, key, value);
    }
}
