package springcloud.springcloudgateway.workflow.userRequestResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.impl.db.workflowClass.workflowResponse;

import springcloud.springcloudgateway.workflow.tools.jsonTransfer;


public class resForUsers {
    public static volatile ConcurrentHashMap<String,workflowResponse> successExecuteRes=new ConcurrentHashMap<>();
    //测试用的
    public static volatile Set<String> oids=new HashSet<>();
    public static volatile Set<String> flushOidsStrings=new HashSet<>();

    public static void addSuccessRes(Map<String, workflowResponse> preDatas) {
        successExecuteRes.putAll(preDatas);
    }
    


    //判断是否完成
    public static boolean isCompleted(String key) {
        return successExecuteRes.containsKey(key);
    }


    //给用户返回结果后要清空对应的result
    //没有加锁的原因是因为按理论说每一个Oid一个时间段内只有一个需要返回的结果
    public static String getSuccessRes(String key) {
        if (successExecuteRes.containsKey(key)) return jsonTransfer.mapToJsonString(successExecuteRes.remove(key).getViewMap());
        else return "none";
        //return successExecuteRes.get(key).getResponseString();
        //return null;
    }
}
