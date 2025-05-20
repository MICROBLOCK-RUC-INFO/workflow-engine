package org.activiti.engine.impl.db.redis;

import org.springframework.beans.factory.annotation.Value;

public class personalConfig {
    private static long cacheCleanTime;
    private static boolean serviceResultPersistence;


    public static void initConfig() {
        //用户任务用户分配表缓存的清理时间
        cacheCleanTime=Long.valueOf(System.getenv("cacheCleanTime")).longValue();
        //true的时候，服务任务的执行结果会持久化，直到实例执行结束
        serviceResultPersistence=Boolean.valueOf(System.getenv("SERVICE_RESULT_PERSISTENCE")).booleanValue();
    }

    public static long getCacheCleanTime() {
        return cacheCleanTime;
    }

    public static void setCacheCleanTime(long cacheCleanTime) {
        personalConfig.cacheCleanTime = cacheCleanTime;
    }

    public static boolean isServiceResultPersistence() {
        return serviceResultPersistence;
    }

    public static void setServiceResultPersistence(boolean serviceResultPersistence) {
        personalConfig.serviceResultPersistence = serviceResultPersistence;
    }
    
    
}
