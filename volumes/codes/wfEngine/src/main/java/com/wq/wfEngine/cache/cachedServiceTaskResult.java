package com.wq.wfEngine.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.impl.db.workflowClass.serviceTaskRes;

/**
 * @apiNote 这是用于缓存服务任务执行结果，只有一个节点调用服务，然后会先把执行结果缓存，最后将结果返回，清空缓存
 */
public class cachedServiceTaskResult {
    private static ConcurrentHashMap<String,Map<String,serviceTaskRes>> cachedServiceTaskRes=new ConcurrentHashMap<>();

    public static void addServiceTaskRes(Map<String,serviceTaskRes> serviceTaskResult,String oid) {
        cachedServiceTaskRes.put(oid,serviceTaskResult);
    }

    public static Map<String,serviceTaskRes> removeServiceTaskRes(String oid) {
        return cachedServiceTaskRes.remove(oid);
    }

    public static Map<String,serviceTaskRes> getServiceTaskRes(String oid) {
        return cachedServiceTaskRes.get(oid);
    }

    public static boolean isCreateMap(String oid) {
        return cachedServiceTaskRes.containsKey(oid);
    }
}
