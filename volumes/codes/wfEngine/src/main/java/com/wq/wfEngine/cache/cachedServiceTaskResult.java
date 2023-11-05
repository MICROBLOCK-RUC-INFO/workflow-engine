package com.wq.wfEngine.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.impl.db.workflowClass.serviceTaskRes;

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
