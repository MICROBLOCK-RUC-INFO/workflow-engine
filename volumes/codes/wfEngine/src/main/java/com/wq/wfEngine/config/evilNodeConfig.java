package com.wq.wfEngine.config;

/**
 * @apiNote 恶意节点设置
 */
public class evilNodeConfig {
    private static boolean isEvil;
    public static void init() {
        isEvil=Boolean.valueOf(System.getenv("isEvil")).booleanValue();
    }
    public static boolean isEvil() {
        return isEvil;
    }
}
