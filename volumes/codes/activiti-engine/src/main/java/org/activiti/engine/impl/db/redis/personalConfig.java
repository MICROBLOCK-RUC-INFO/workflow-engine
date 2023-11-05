package org.activiti.engine.impl.db.redis;

import org.springframework.beans.factory.annotation.Value;

public class personalConfig {
    private static long cacheCleanTime;
    private static boolean serviceResultPersistence;


    public static void initConfig() {
        cacheCleanTime=Long.valueOf(System.getenv("cacheCleanTime")).longValue();
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
