package org.activiti.engine.impl.db.redis.tools.operation.taskBind;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.db.redis.redisUtil;
import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.db.redis.workflowContext;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.deployHandler;
import org.activiti.engine.impl.db.redis.tools.operation.verify.verifyOperator;
import org.springframework.data.redis.core.StringRedisTemplate;

public abstract class binderImpl implements binder{
        /**
     * @apiNote 这个函数预留给校验用的
     * @return 
     */
    public boolean verify(String name,String data,String signature) {
        return verifyOperator.verify(name, data, signature);
    }

    public boolean verifies(String data,String bpmnName,List<String[]> list) {
        return verifies(data, list,bpmnName,false);
    }

    public boolean verifies(String data,List<String[]> list,String bpmnName,boolean isDeploy) {
        StringRedisTemplate redis=redisUtil.getTableRedisClient();
        if (isDeploy) {
            String[] orgs=new String[list.size()];
            for (int i=0;i<list.size();i++) {
                if (!verify(list.get(i)[0], data, list.get(i)[1])) return false;
                orgs[i]=list.get(i)[0];
            }
            redis.delete(bpmnName);
            redis.opsForSet().add(bpmnName,orgs);
        } else {
            Set<String> orgs=redis.opsForSet().members(bpmnName);
            if (list.isEmpty()||list.size()!=deployHandler.getSize(bpmnName)) return false;
            for (int i=0;i<list.size();i++) {
                if (!orgs.contains(list.get(i)[0])) return false;
                if (!verify(list.get(i)[0], data, list.get(i)[1])) return false;
            }
        }
        return true;
    }
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
