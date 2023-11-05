package com.wq.wfEngine.config;

public class evilNodeConfig {
    private static boolean isEvil;
    public static void init() {
        isEvil=Boolean.valueOf(System.getenv("isEvil")).booleanValue();
    }
    public static boolean isEvil() {
        return isEvil;
    }
}
