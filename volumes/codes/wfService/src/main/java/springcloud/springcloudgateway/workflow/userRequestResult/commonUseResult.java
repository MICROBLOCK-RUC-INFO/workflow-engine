package springcloud.springcloudgateway.workflow.userRequestResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 2025/4/30
 * 执行结果缓存，还是跟之前一样不包括Deploy,instance,complete
 */
public class commonUseResult {
    private static ConcurrentHashMap<String,String> commonUseResponseCache=new ConcurrentHashMap<>();

    /**
     * @apiNote 这个接口暂时没用
     * @param simulateResults
     */
    @Deprecated
    public static void addSuccessRes(Map<String, String> simulateResults) {
        commonUseResponseCache.putAll(simulateResults);
    }
    
    public static void addResult(String key,String result) {
        commonUseResponseCache.put(key,result);
    }


    //判断是否完成
    public static boolean isCompleted(String key) {
        return commonUseResponseCache.containsKey(key);
    }


    //给用户返回结果后要清空对应的result
    //没有加锁的原因是因为按理论说每一个Oid一个时间段内只有一个需要返回的结果
    public static String getResult(String key) {
        if (commonUseResponseCache.containsKey(key)) return commonUseResponseCache.remove(key);
        else return "none";
        //return successExecuteRes.get(key).getResponseString();
        //return null;
    }
}
