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

public class tableOperator {
    private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> allocationTable=new ConcurrentHashMap<String,ConcurrentHashMap<String,String>>();
    private static ConcurrentHashMap<String,timeRecord> lastUsedTime=new ConcurrentHashMap<String,timeRecord>();
    private final static StringRedisTemplate redisClient=new redisUtil().getTableRedisClient();
    private final static Logger logger=LoggerFactory.getLogger(tableOperator.class);
    static class timeRecord {
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
            if (lastUsedTime.get(key).canDeleted(System.currentTimeMillis(), maxTime)) {
                allocationTable.remove(key);
                keyIterator.remove();
            }
        }
    }

    /**
     * 
     * @param oid 工作流实例的oid
     * @return 如果缓存里有返回true,否则返回false
     */
    public static boolean isExist(String oid) {
        return allocationTable.containsKey(oid);
    }

    public static String getUser(String oid,String taskName) {
        ConcurrentHashMap<String,String> table=getTable(oid);
        return table.get(taskName);
    }

    public static String getServiceBindInfo(String oid,String serviceTask) {
        ConcurrentHashMap<String,String> table=getTable(oid);
        return table.get(serviceTask);
    }

    /**
     * 
     * @param oid 已结束的工作流实例oid
     */
    public static void cleanTable(String oid) {
        redisClient.delete(oid);
        allocationTable.remove(oid);
    }

    private static ConcurrentHashMap<String, String> getTable(String oid) {
        if (!allocationTable.containsKey(oid)) {
            Map<Object, Object> table=redisClient.opsForHash().entries(oid);
            ConcurrentHashMap<String,String> cMap=new ConcurrentHashMap<>();
            for (Entry<Object,Object> entry:table.entrySet()) {
                cMap.put(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
            }
            allocationTable.put(oid,cMap);
            lastUsedTime.put(oid,new timeRecord(0L));
        }
        lastUsedTime.get(oid).setLastUseTime(System.currentTimeMillis());
        return allocationTable.get(oid);
    }


    /**
     * 
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
                 *如果这时候有人读取数据就会出现脏读，使用缓存双删也无法解决这个问题
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
        Object oldValue= redisClient.opsForHash().get(oid, key);
        Map<String,Object> SimulateBindSet=new HashMap<String,Object>() {{
            put("oid",oid);put("taskName",key);put("oldValue",oldValue==null?"default":oldValue);
            put("newValue",value);put("opType","taskBind");
        }};
        return jsonTransfer.mapToJsonString(SimulateBindSet);
    }

    public static void allocate(String oid,Map<String,Object> staticAllocation) {
        if (staticAllocation==null) return;
        redisClient.opsForHash().putAll(oid, staticAllocation);
    }
}
