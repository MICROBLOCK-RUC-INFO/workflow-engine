package org.activiti.engine.impl.db.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceCache {
    private ConcurrentHashMap<String,Map<String,Pair<Object,Boolean>>> cache=new ConcurrentHashMap<>();
    private final Logger logger=LoggerFactory.getLogger(ServiceCache.class);

    @SuppressWarnings("unchecked")
    public void cache(Map<String,Object> cachedOutput,String oid) {
        try {
            if (cachedOutput==null||oid==null) throw new RuntimeException("Class ServiceCache, method persistence params is null");
            Map<String,Pair<Object,Boolean>> oidMap= cache.computeIfAbsent(oid, key -> {return new HashMap<>();});
            cachedOutput.entrySet().stream().forEach(entry -> {
                if (!entry.getKey().equals("init")) {
                    oidMap.put(entry.getKey(),new MutablePair<Object,Boolean>(entry.getValue(), false));
                }
            });
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    public void flush(String oid,boolean isVaild) {
        try {
            if (!cache.containsKey(oid)) return;
            if (isVaild) {
                cache.get(oid).entrySet().stream().forEach(entry -> {
                    entry.getValue().setValue(true);
                });
            } else {
                cache.get(oid).entrySet().removeIf(entry -> !entry.getValue().getValue());
            } 
        } catch (Exception e) {
            throw new RuntimeException("serviceCache flush error:"+e.getMessage());
        }
    }

    /**
     * @apiNote 这里没有存储每一次的输入，只存储了每一个serviceTask的返回值
     * 因为暂时没想好每一个的输入应该怎么表达，这是一个坑，记录下2023-06-21
     */
    public void storeBusinessData(String oid,Object businessData) {
        cache.computeIfAbsent(oid, key -> {return new HashMap<>();})
                .put("init",new MutablePair<Object,Boolean>(businessData, true));
    } 

    public Map<String,Object> getCache(String oid) {
        try {
            Map<String,Object> dataCache=new HashMap<String,Object>();
            Map<String,Pair<Object,Boolean>> oidMap=cache.getOrDefault(oid,new HashMap<String,Pair<Object,Boolean>>());
            oidMap.entrySet().stream().forEach(entry -> {
                if (entry.getValue().getRight()) {
                    dataCache.put(entry.getKey(),entry.getValue().getKey());
                }
            });
            return dataCache;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw new RuntimeException("serviceCache error:"+e.getMessage());
        }
    }

    public void removeCache(String oid) {
        cache.remove(oid);
    }
}
