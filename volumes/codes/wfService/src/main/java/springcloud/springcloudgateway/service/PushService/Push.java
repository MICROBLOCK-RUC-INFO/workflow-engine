package springcloud.springcloudgateway.service.PushService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import springcloud.springcloudgateway.service.qps.TimeUtil;

/**
 * @Author: 李浩然
 * @Date: 2021/4/22 10:13 下午
 */
// @Repository
// public class Push {
//     @Async("taskExecutor")
//     public void sendMessage(ProviderService providerService, JSONArray monitorInfos) {
//         try {
//             long consumerTime = System.currentTimeMillis();
//             for (int k = 0; k < monitorInfos.size(); k++) {
//                 JSONObject monitorInfo = monitorInfos.getJSONObject(k);
//                 String monitorData = monitorInfo.getString("blockchainData");
// //                String rawData=monitorInfo.getString("rawData");
//                 String monitorHost = monitorInfo.getString("monitorHost");
//                 String rawData = "[]";
//                 if (!monitorData.equals("[]")) {
//                     String time = monitorInfo.getString("time");
//                     JSONArray mysqlArray = monitorInfo.getJSONArray("mysqlData");
//                     //上链逻辑
//                     providerService.transferMonitorData(monitorInfo.getString("key"), monitorData, rawData, mysqlArray.toJSONString(), time, monitorHost);
//                 }
//             }
//             //发送
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//         }
//     }

//     @Async("taskExecutor")
//     public void sendLastMessage(ProviderService providerService, long time, long click, String consumerName) {
//         try {
//             Thread.sleep(2000L);
//             if (TimeUtil.currentTimeMillis() - time >= 1000 && time - MonitorService.lastUpdate >= 1000) {
//                 long serviceTime = TimeUtil.currentTimeMillis();
//                 MonitorService.isSending = true;
//                 MonitorService.lastUpdate = time;
//                 JSONArray monitorInfos = providerService.addData(time, click, serviceTime, consumerName, "last");
//                 MonitorService.keyMap.clear();
//                 MonitorService.monitorRawDataMap.clear();
//                 for (int j = 0; j < monitorInfos.size(); j++) {
//                     JSONObject jsonObject = monitorInfos.getJSONObject(j);
//                     long consumerTime = System.currentTimeMillis();
//                     String monitorData = jsonObject.getString("blockchainData");
//                     String monitorHost = jsonObject.getString("monitorHost");
// //                    String rawData = jsonObject.getString("rawData");
//                     String rawData = "[]";
//                     if (!monitorData.equals("[" + "]")) {
//                         String now = jsonObject.getString("time");
//                         JSONArray mysqlArray = jsonObject.getJSONArray("mysqlData");
//                         //上链
// //                        res = providerService.fabricPut(jsonObject.getString("key"), monitorData, rawData, now);
//                         providerService.transferMonitorData(jsonObject.getString("key"), monitorData, rawData, mysqlArray.toJSONString(), now, monitorHost);
//                     }
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//         }
//     }
// }
