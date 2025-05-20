package com.wq.wfEngine.tool.serviceComposition.simpleJson;

/**
 * @apiNote 当初的简单json解析用的，现在应该已经没用了
 */
public class stringToNum {
    public static Integer getInteger(String str) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getLong(String str) {
        try {
            return Long.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Double getDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return null;
        }
    }
}
