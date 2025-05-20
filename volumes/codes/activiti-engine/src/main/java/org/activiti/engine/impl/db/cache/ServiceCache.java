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

/**
 * @apiNote workflow.yaml文件中SERVICE_RESULT_PERSISTENCE为true的时候起作用
 * 用于保存所有服务任务的执行结果(服务返回结果)包括用户最开始的输入，可以直接让服务任务使用已执行过的数据作为输入
 * 当然了这里只是用的内存，因为当时考虑到性能，没有真正的使用持久化，但是大体实现思路是这样
 */
public class ServiceCache {
    /*
     * 结构如下
     * <oid,<TaskName,(服务任务执行结果，是否已上链)>>
     */
    private ConcurrentHashMap<String,Map<String,Pair<Object,Boolean>>> cache=new ConcurrentHashMap<>();
    private final Logger logger=LoggerFactory.getLogger(ServiceCache.class);

    /**
     * @apiNote 暂存(模拟执行期间就会调用服务，需要等到flush的时候才真保存)
     * @param cachedOutput 服务的返回结果
     * @param oid 实例的唯一id
     */
    @SuppressWarnings("unchecked")
    public void cache(Map<String,Object> cachedOutput,String oid) {
        try {
            //入参为空，报错
            if (cachedOutput==null||oid==null) throw new RuntimeException("Class ServiceCache, method persistence params is null");
            //如果cache中没有对应实例的HashMap,创建一个,并返回(oidMap)
            Map<String,Pair<Object,Boolean>> oidMap= cache.computeIfAbsent(oid, key -> {return new HashMap<>();});
            //遍历暂存，false表示数据是暂存
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

    /**
     * @apiNote flush时候真保存，最开始设计的时候其实是isVaild为true是真保存，false时是删除对应实例暂存的数据
     * @param oid 实例唯一id
     */
    public void flush(String oid,boolean isVaild) {
        try {
            //cache里没有直接返回
            if (!cache.containsKey(oid)) return;
            if (isVaild) {
                //true则将对之前暂存的false置为true
                cache.get(oid).entrySet().stream().forEach(entry -> {
                    entry.getValue().setValue(true);
                });
            } else {
                //false则直接删除
                cache.get(oid).entrySet().removeIf(entry -> !entry.getValue().getValue());
            } 
        } catch (Exception e) {
            throw new RuntimeException("serviceCache flush error:"+e.getMessage());
        }
    }

    /**
     * @apiNote 这里没有存储每一次的用户输入，只存储了最新的一次用户输入
     * 因为暂时没想好每一个的输入应该怎么表示，这是一个坑，记录下2023-06-21
     */
    public void storeBusinessData(String oid,Object businessData) {
        cache.computeIfAbsent(oid, key -> {return new HashMap<>();})
                .put("init",new MutablePair<Object,Boolean>(businessData, true));
    } 

    /**
     * @apiNote 获得对应实例的数据
     * @param oid
     * @return
     */
    public Map<String,Object> getCache(String oid) {
        try {
            Map<String,Object> dataCache=new HashMap<String,Object>();
            Map<String,Pair<Object,Boolean>> oidMap=cache.getOrDefault(oid,new HashMap<String,Pair<Object,Boolean>>());
            //遍历将true的数据加入dataCache
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

    /**
     * @apiNote 删除对应实例的数据，当实例结束的时候调用
     * @param oid
     */
    public void removeCache(String oid) {
        cache.remove(oid);
    }
}
