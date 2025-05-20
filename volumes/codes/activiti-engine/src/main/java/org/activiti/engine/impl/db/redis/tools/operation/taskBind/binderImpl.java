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

/**
 * @apiNote 动态绑定(用户任务和服务任务的都有)
 */
public abstract class binderImpl implements binder{
    /**
     * @apiNote 校验签名
     * @param name 用户名
     * @param data 数据
     * @param signature 签名
     * @return true就是校验通过，失败就是false
     */
    public boolean verify(String name,String data,String signature) {
        return verifyOperator.verify(name, data, signature);
    }

    /**
     * @apiNote 用于服务任务动态绑定时校验，应该是需要所有组织的签名
     * @param data 数据
     * @param bpmnName BPMN的部署名
     * @param list 各组织签名的list
     * @return true就是校验通过，失败就是false
     */
    public boolean verifies(String data,String bpmnName,List<String[]> list) {
        return verifies(data, list,bpmnName,false);
    }

    /**
     * @apiNote 对一组签名进行验证，用于部署时和服务任务动态绑定时
     * @param data 数据
     * @param list 各组织签名的list
     * @param bpmnName BPMN的部署名
     * @param isDeploy true表示是部署，false就说明是在服务任务动态绑定
     * @return
     */
    public boolean verifies(String data,List<String[]> list,String bpmnName,boolean isDeploy) {
        StringRedisTemplate redis=redisUtil.getTableRedisClient();
        if (isDeploy) {
            /*
             * 如果是在部署，依次校验各组织签名
             * 然后将各组织名存在Redis中，结构如下
             * <deploymentName,Set<组织名>>
             */
            String[] orgs=new String[list.size()];
            for (int i=0;i<list.size();i++) {
                if (!verify(list.get(i)[0], data, list.get(i)[1])) return false;
                orgs[i]=list.get(i)[0];
            }
            redis.delete(bpmnName);
            redis.opsForSet().add(bpmnName,orgs);
        } else {
            /*
             * 如果是服务任务动态绑定
             * 则根据部署是写入Redis中的组织名，依次进行签名校验
             */
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
     * @apiNote 实例化的时候静态分配表的写入
     * @param oid 静态分配对应的工作流实例的oid
     * @param staticAllocation key为taskName,value为userId的map,如果staticAllocation=null,会直接返回
     */
    public void staticAllocate(String oid,Map<String,Object> staticAllocation) {
        tableOperator.allocate(oid, staticAllocation);
    }

    /**
     * @apiNote 动态绑定的模拟执行
     * @param oid 不为null
     * @param key 不为null
     * @param value 如果是userTask绑定为user,如果是serviceTask绑定则为group
     * @return json字符串 {oid=,taskName=,oldValue=,newValue=,opType=taskBind}
     */
    public String simulateBind(String oid,String key,String value) {
        return tableOperator.simulateBind(oid, key, value);
    }

    /**
     * @apiNote 动态绑定的写入
     * @param oid
     * @param key
     * @param value
     * @param oldValue 用来做对比，如果oldValue与查询出来的值不匹配，则认为动态绑定失败,类似cas的想法
     */
    public boolean bind(String oid,String key,String value) {
        return tableOperator.bind(oid, key, value);
    }
}
