package org.activiti.engine.impl.db.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * 用来缓存oid对应的messageEvent的name与execution
 * 方便通过oid来查找
 */
public class oidEvents {
    private static Logger logger=LoggerFactory.getLogger(oidEvents.class);
    private volatile static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> oidEventMap=new ConcurrentHashMap<>();
    
    public static void addOidEventMap(String oid,String eventName,String executionId) {
        if (!oidEventMap.containsKey(oid)) {
            oidEventMap.put(oid,new ConcurrentHashMap<String,String>());
        }
        oidEventMap.get(oid).put(eventName,executionId);
    }

    public static void deleteOidEventMap(String oid,String eventName,String executionId) {
        if (oidEventMap.containsKey(oid)&&oidEventMap.get(oid).containsKey(eventName)&&oidEventMap.get(oid).get(eventName).equals(executionId)) {
            oidEventMap.get(oid).remove(eventName);
            if (oidEventMap.get(oid).isEmpty()) {
                oidEventMap.remove(oid);
            }
        } else {
            System.out.printf("this event oid:%s ,eventName:%s ,executionId:%s is not exist",oid,eventName,executionId);
            //logger.error("this event oid:%s ,eventName:%s ,executionId:%s is not exist",oid,eventName,executionId);
        }
    }

    public static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> getAllEventMap() {
        return oidEventMap;
    }

    // public static Map<String,String> getEventMapByOid(String oid) {
    //     if (oidEventMap.containsKey(oid)) {
    //         return oidEventMap.get(oid);
    //     } else {
    //         return new HashMap<>();
    //     }
    // }

    public static String getEventExecutionIdByOidAndName(String oid,String eventName) {
        return oidEventMap.get(oid).get(eventName);
    }

    public static Map<String,String> removeEventMapByOid(String oid) {
        return oidEventMap.remove(oid);
    }


}
