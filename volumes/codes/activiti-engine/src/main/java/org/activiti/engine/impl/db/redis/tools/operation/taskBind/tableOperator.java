package org.activiti.engine.impl.db.redis.tools.operation.taskBind;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.impl.db.redis.redisUtil;
import org.activiti.engine.impl.db.redis.tools.jackJson.jsonTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @apiNote 用户任务的用户信息和服务任务的服务信息的管理
 */
public class tableOperator {
    /*
     * 缓存的用户任务的用户分配表和服务任务的服务绑定信息，结构如下
     * <Oid,<TaskName,UserName或者ServiceBindInfo>>
     */
    private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> allocationTable=new ConcurrentHashMap<String,ConcurrentHashMap<String,String>>();
    /*
     * 用户分配表的最近使用时间记录，为了长时间不使用时，清空缓存
     */
    private static ConcurrentHashMap<String,timeRecord> lastUsedTime=new ConcurrentHashMap<String,timeRecord>();
    //redis客户端
    private final static StringRedisTemplate redisClient=redisUtil.getTableRedisClient();
    private final static Logger logger=LoggerFactory.getLogger(tableOperator.class);
    static class timeRecord {
        //最近一次使用时间
        long lastUseTime;
        public timeRecord(long time) {
            this.lastUseTime=time;
        }

        public void setLastUseTime(long time) {
            this.lastUseTime=time;
        }

        public long getLastUseTime() {
            return lastUseTime;
        }

        /**
         * @apiNote 当前时间now-最近使用时间lastUseTime,大于等于设置的最大时间maxTime,就认为可以删除
         */
        public boolean canDeleted(long now,long maxTime) {
            return now-lastUseTime>=maxTime;
        }
    }

    /**
     * 
     * @param maxTime 现在时间-最后使用时间>=maxTime时把cache清除掉
     * @apiNote 这个只是用来清除内存中的数据并不是清除redis中的数据
     */
    public static void cleanCache(long maxTime) {
        Iterator<String> keyIterator= lastUsedTime.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key=keyIterator.next();
            //符合时间条件就删除
            if (lastUsedTime.get(key).canDeleted(System.currentTimeMillis(), maxTime)) {
                allocationTable.remove(key);
                keyIterator.remove();
            }
        }
    }

    /**
     * @apiNote 缓存中是否有对应信息(用户任务的用户分配表或者服务任务的服务绑定信息)
     * @param oid 工作流实例的oid
     * @return 如果缓存里有返回true,否则返回false
     */
    public static boolean isExist(String oid) {
        return allocationTable.containsKey(oid);
    }

    /**
     * @apiNote 获得用户分配表中对应UserTask的用户
     * @param oid 实例唯一id
     * @param taskName 用户任务名
     * @return 用户名UserName
     */
    public static String getUser(String oid,String taskName) {
        ConcurrentHashMap<String,String> table=getTable(oid);
        return table.get(taskName);
    }

    /**
     * @apiNote 获得服务任务的服务绑定信息
     * @param oid 实例唯一id
     * @param serviceTask 服务任务名
     * @return 绑定的服务信息
     */
    public static String getServiceBindInfo(String oid,String serviceTask) {
        ConcurrentHashMap<String,String> table=getTable(oid);
        return table.get(serviceTask);
    }

    /**
     * @apiNote 删除缓存和Redis中的数据
     * @param oid 已结束的工作流实例oid
     */
    public static void cleanTable(String oid) {
        redisClient.delete(oid);
        allocationTable.remove(oid);
    }

    /**
     * @apiNote 根据oid获得用户分配表，或者各个服务任务的服务绑定信息
     * @param oid 实例唯一id
     * @return <TaskName,UserName或者ServiceBindInfo>
     */
    private static ConcurrentHashMap<String, String> getTable(String oid) {
        if (!allocationTable.containsKey(oid)) {
            /*
             * 如果缓存中没有，先从Redis读
             * 然后存入缓存
             * 同时lastUsedTime放入创建的时间记录
             */
            Map<Object, Object> table=redisClient.opsForHash().entries(oid);
            ConcurrentHashMap<String,String> cMap=new ConcurrentHashMap<>();
            for (Entry<Object,Object> entry:table.entrySet()) {
                cMap.put(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
            }
            allocationTable.put(oid,cMap);
            lastUsedTime.put(oid,new timeRecord(0L));
        }
        //设置最近一次使用时间
        lastUsedTime.get(oid).setLastUseTime(System.currentTimeMillis());
        return allocationTable.get(oid);
    }


    /**
     * @apiNote 绑定(写入数据)
     * @param oid 实例的oid,不为null
     * @param key 对应的taskName,不为null
     * @param value 绑定的值,不为null
     * @return 动态绑定成功返回true,绑定失败返回false
     */
    public static boolean bind(String oid, String key,String value) {
        try {
            redisClient.opsForHash().put(oid, key, value);
            if (allocationTable.containsKey(oid)) {
                /*更新redis后，再删除缓存中的脏数据，但是有一个问题，如果在还没有删除脏数据的时候,
                 *如果这时候有人读取数据就会出现脏读
                 */
                allocationTable.remove(oid);
                lastUsedTime.remove(oid);
            }
            return true; 
        } catch (Exception e) {
            logger.warn(String.format("oid:%s,taskName:%s,value:%s,bind error", oid,key,value));
            return false;
        }
    }

    /**
     * @apiNote 动态绑定模拟执行的返回结果
     * @param oid 不为null
     * @param key 不为null
     * @param value 不为null
     * @return json字符串 {oid=,taskName=,oldValue=,newValue=,opType=taskBind}
     */
    public static String simulateBind(String oid,String key,String value) {
        //返回的内容有需要可以修改，但是要确保在比较结果的时候没问题
        Object oldValue= redisClient.opsForHash().get(oid, key);
        Map<String,Object> SimulateBindSet=new HashMap<String,Object>() {{
            put("oid",oid);put("taskName",key);put("oldValue",oldValue==null?"default":oldValue);
            put("newValue",value);put("opType","taskBind");
        }};
        return jsonTransfer.mapToJsonString(SimulateBindSet);
    }

    /**
     * @apiNote 实例化的时候静态分配表的写入
     */
    public static void allocate(String oid,Map<String,Object> staticAllocation) {
        if (staticAllocation==null) return;
        redisClient.opsForHash().putAll(oid, staticAllocation);
    }
}
