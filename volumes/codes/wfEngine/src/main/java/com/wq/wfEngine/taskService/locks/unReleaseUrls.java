package com.wq.wfEngine.taskService.locks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wq.wfEngine.WfEngineApplication;
import com.wq.wfEngine.tool.Connect;
import com.wq.wfEngine.tool.jsonTransfer;

/**
 * 这里是为了防止在背书阶段的加锁过程中，有多个服务，前面的加锁成功后面的加锁失败，设计的rollback
 */
// public class unReleaseUrls {
//     private static ThreadLocal<List<String>> urls=new ThreadLocal<>();
//     private static final Logger logger=LoggerFactory.getLogger(unReleaseUrls.class);

//     public static void addUrl(String url) {
//         if (urls.get()==null) {
//             urls.set(new ArrayList<>());
//         }
//         urls.get().add(url);
//     }

//     public static List<String> getUrls() {
//         List<String> res=urls.get();
//         return res==null?new ArrayList<>():res;
//     }

//     public static void rollBack(String oid) {
//         try {
//             List<String> rollBackUrls=urls.get();
//             if (rollBackUrls!=null) {
//                 Map<String,Object> requestMap=new HashMap<>();
//                 requestMap.put("oids",oid);
//                 List<Future<SimpleHttpResponse>> futures=new ArrayList<>();
                
//                 for (String url:rollBackUrls) {
//                     Map<String,Object> postMap=new HashMap<>();
//                     postMap.put("s-consumerName","");
//                     postMap.put("s-serviceName",url);
//                     postMap.put("headers","{}");
//                     postMap.put("s-url","/unLock");
//                     postMap.put("s-method","POST");
//                     postMap.put("body",jsonTransfer.toJsonString(requestMap));
//                     postMap.put("s-group","WORKFLOW");
//                     futures.add(Connect.doPost("http://127.0.0.1:8999/grafana/run?loadBalance=enabled", 
//                                 jsonTransfer.toJsonString(postMap)));
//                 }
//                 for (Future<SimpleHttpResponse> future:futures) {
//                     SimpleHttpResponse rollBackRes=future.get();
//                     logger.info("release Lock roll back:"+rollBackRes.getBodyText());
//                 }
//             }
//         } catch (Exception e) {
//             logger.info("roll back error");
//         }
//     }

//     public static void reset() {
//         urls.remove();
//     }
// }
