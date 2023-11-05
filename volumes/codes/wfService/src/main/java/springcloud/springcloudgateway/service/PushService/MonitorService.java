package springcloud.springcloudgateway.service.PushService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import springcloud.springcloudgateway.service.loadBalance.LoadBalanceClient;
//import springcloud.springcloudgateway.config.fabricConfig.MonitorChannelConfig;
//import springcloud.springcloudgateway.service.loadBalance.LoadBalanceClient;
import springcloud.springcloudgateway.service.nacosCache.InstanceInfo;
import springcloud.springcloudgateway.service.qps.QpsHelper;
import springcloud.springcloudgateway.service.qps.TimeUtil;
import springcloud.springcloudgateway.service.utils.HttpClientUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MonitorService {
    Logger logger = LoggerFactory.getLogger(MonitorService.class);
    private static final long TIME_COLLECT_INTERVAL = 1000L;
    public static volatile boolean isSending = false;
    public static volatile long lastUpdate = 0, lastSend = 0;
    @Autowired
    LoadBalanceClient loadBalanceClient;

    // @Resource
    // MonitorChannelConfig monitorChannelConfig;
    // @Autowired
    // Push push;
    @Resource
    HttpClientUtil httpClientUtil;
    // @Resource
    // private ProviderService providerService;
    private static AtomicInteger serverNum;
    public static HashSet<String> keyMap = new HashSet<>();
    public static ConcurrentHashMap<String, CopyOnWriteArrayList<MonitorRawData>> monitorRawDataMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, QpsHelper> QPSHelpers = new ConcurrentHashMap<String, QpsHelper>();
    public volatile BiMap<String, String> serviceInstanceId = HashBiMap.create();
    public volatile BiMap<String, String> serviceInstanceIdInverse = HashBiMap.create();

    public MonitorService() {
    }

    @PostConstruct
    public void init() {
        //serverNum = new AtomicInteger(monitorChannelConfig.getNodeNum());
    }

    // /**
    //  * 收集并上传数据
    //  *
    //  * @param service      服务名称
    //  * @param ipaddr       服务地址以及endpoint
    //  * @param qpsHelper    统计包
    //  * @param clickTime    点击时间
    //  * @param consumerName 调用方服务名称
    //  * @throws Exception
    //  */
    // public synchronized void collectDataHighPerformance(String service, String ipaddr, QpsHelper qpsHelper, long clickTime, String consumerName) throws Exception {
    //     if (qpsHelper.getTimeInMills() - qpsHelper.getTimestamp() >= TIME_COLLECT_INTERVAL) {
    //         保存上一秒
    //         long time = qpsHelper.getTimestamp();
    //         if (qpsHelper.getTimestamp() != 0 && time - lastUpdate >= 1000) {
    //             lastUpdate = time;
    //             JSONArray monitorInfos = providerService.addData(time, clickTime, TimeUtil.currentTimeMillis(), consumerName, "next");
    //             keyMap.clear();
    //             monitorRawDataMap.clear();
    //             push.sendMessage(providerService, monitorInfos);
    //                executor.submit(new sendMessageRunnable(providerService, monitorInfos));

    //         }
    //         重置
    //         qpsHelper.setTimestamp(qpsHelper.getTimeInMills());
    //         qpsHelper.setTotalRequest((double) qpsHelper.totalRequestInSec());
    //         qpsHelper.setTotalSuccess((double) qpsHelper.totalSuccessInSec());
    //         qpsHelper.setTotalException((double) qpsHelper.totalExceptionInSec());
    //         qpsHelper.setMinRt(qpsHelper.minRtInSec());
    //         qpsHelper.setAvgRt(qpsHelper.avgRtInSec());
    //         qpsHelper.setExceptionQps(qpsHelper.exceptionQps());
    //         qpsHelper.setSuccessQps(qpsHelper.successQps());
    //         keyMap.add(service + "@@" + ipaddr + "@@" + consumerName);
    //         long timestamp = qpsHelper.getTimestamp();
    //         if (timestamp - lastSend >= 1000) {
    //             lastSend = timestamp;
    //             push.sendLastMessage(providerService, timestamp, clickTime, consumerName);
    //         }
    //        executor.submit(new sendLastMessageRunnable(providerService, timestamp, clickTime, consumerName));
    //     } else {
    //         qpsHelper.setTotalRequest((double) qpsHelper.totalRequestInSec());
    //         qpsHelper.setTotalSuccess((double) qpsHelper.totalSuccessInSec());
    //         qpsHelper.setTotalException((double) qpsHelper.totalExceptionInSec());
    //         qpsHelper.setMinRt(qpsHelper.minRtInSec());
    //         qpsHelper.setAvgRt(qpsHelper.avgRtInSec());
    //         qpsHelper.setExceptionQps(qpsHelper.exceptionQps());
    //         qpsHelper.setSuccessQps(qpsHelper.successQps());
    //     }
    // }


    // /**
    //  * 取得服务名称以及实例唯一对应uuid
    //  *
    //  * @param serviceName
    //  * @param ipaddr
    //  * @return
    //  */
    // public String getUUId(String serviceName, String ipaddr) {
    //     String address = serviceName + "@@" + ipaddr;
    //     if (serviceInstanceId.containsKey(address)) {
    //         return serviceInstanceId.get(address);
    //     }
    //     UUID id = UUID.randomUUID();
    //     String uuid = id.toString().replace("-", "");
    //     serviceInstanceId.put(address, uuid);
    //     serviceInstanceIdInverse = serviceInstanceId.inverse();
    //     return uuid;
    // }

    // /**
    //  * 转发
    //  *
    //  * @param loadBalance
    //  * @return
    //  * @throws Exception
    //  */
    // public String redirect(String body, String loadBalance) throws Exception {
    //     使用 LoadBalanceClient 和 RestTemplate 结合的方式来访问
    //     Response response = null;
    //     String server = null;
    //     try {
    //         Map<String, String> params = new HashMap<>();
    //         访问
    //         server = selectNode();
    //         String ipaddr = "http://" + server + "/grafana/monitor?loadBalance=" + loadBalance;
    //         logger.info("server:" + server);
    //         response = HttpClientUtil.redirectHttpRequest(ipaddr, params, body, "POST");
    //         业务逻辑
    //         return response.body().string();
    //     } catch (Exception e) {
    //         logger.error("server:" + server + " " + response.message());
    //         logger.error(e.getMessage());
    //         return e.getMessage();
    //     } finally {
    //         response.close();
    //     }
    // }

    // private synchronized String selectNode() {
    //     if (serverNum.get() >= monitorChannelConfig.getNodeSum()) {
    //         serverNum.set(0);
    //     }
    //     String server = monitorChannelConfig.getIpList().get(serverNum.getAndIncrement());
    //     return server;
    // }

    
    public String monitorNoCache(String req,String loadBalance) throws Exception {
        //使用 LoadBalanceClient 和 RestTemplate 结合的方式来访问
        Response response = null;
        JSONObject requesJson = JSONObject.parseObject(req);
        JSONObject json = new JSONObject();
        String service, ipaddr, method, consumerName = "default", headers = "{}", body = "{}", consumerHost = "", consumerEndPoint = "",group=null;
        if (requesJson.containsKey("s-serviceName")) {
            service = requesJson.getString("s-serviceName");
        } else {
            throw new Exception("there is no serviceName");
        }
        if (requesJson.containsKey("s-url")) {
            ipaddr = requesJson.getString("s-url");
        } else {
            throw new Exception("there is no serviceUrl");
        }
        if (requesJson.containsKey("s-method")) {
            method = requesJson.getString("s-method");
        } else {
            throw new Exception("there is no serviceMethod");
        }
        if (requesJson.containsKey("s-consumerName")) {
            consumerName = requesJson.getString("s-consumerName");
        }
        if (requesJson.containsKey("headers")) {
            headers = requesJson.getString("headers");
        }
        if (requesJson.containsKey("body")) {
            body = requesJson.getString("body");
        }
        if (requesJson.containsKey("s-consumerHost")) {
            consumerHost = requesJson.getString("s-consumerHost");
        }
        if (requesJson.containsKey("s-consumerEndPoint")) {
            consumerEndPoint = requesJson.getString("s-consumerEndPoint");
        }
        if (requesJson.containsKey("s-group")) {
            group = requesJson.getString("s-group");
        }
        //long clickTime = TimeUtil.currentTimeMillis();
        if (loadBalance.equals("enabled")) {
            InstanceInfo serviceInstance;
            if (group==null) serviceInstance = loadBalanceClient.chooseServerNoCache(service);
            else serviceInstance = loadBalanceClient.chooseServerNoCache(service,group);
            URI uri = URI.create(ipaddr);
            ipaddr = loadBalanceClient.reconstructURI(serviceInstance, uri).toString();
        }
        try {
            Map<String, String> params = null;
            if (headers != null) {
                params = JSONObject.parseObject(headers, new TypeReference<Map<String, String>>() {
                });
            }
            //访问
            logger.info("ipaddr {},params {},body {},method {}",ipaddr,params,body,method);
            response = HttpClientUtil.httpRequest(ipaddr, params, body, method);
            String responseBody = response.body().string();
            json.put("body", responseBody);
            json.put("headers", response.headers());
            json.put("code", response.code());
            json.put("isRedirect", response.isRedirect());
            json.put("isSuccessful", response.isSuccessful());
//            return json.toString();
            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            response.close();
        }
    }

    /**
     * 监控服务
     *
     * @return
     * @throws Exception
     */
    public String monitor(String req, String loadBalance) throws Exception {
        //使用 LoadBalanceClient 和 RestTemplate 结合的方式来访问
        Response response = null;
        JSONObject requesJson = JSONObject.parseObject(req);
        JSONObject json = new JSONObject();
        String service, ipaddr, method, consumerName = "default", headers = "{}", body = "{}", consumerHost = "", consumerEndPoint = "",group=null;
        if (requesJson.containsKey("s-serviceName")) {
            service = requesJson.getString("s-serviceName");
        } else {
            throw new Exception("there is no serviceName");
        }
        if (requesJson.containsKey("s-url")) {
            ipaddr = requesJson.getString("s-url");
        } else {
            throw new Exception("there is no serviceUrl");
        }
        if (requesJson.containsKey("s-method")) {
            method = requesJson.getString("s-method");
        } else {
            throw new Exception("there is no serviceMethod");
        }
        if (requesJson.containsKey("s-consumerName")) {
            consumerName = requesJson.getString("s-consumerName");
        }
        if (requesJson.containsKey("headers")) {
            headers = requesJson.getString("headers");
        }
        if (requesJson.containsKey("body")) {
            body = requesJson.getString("body");
        }
        if (requesJson.containsKey("s-consumerHost")) {
            consumerHost = requesJson.getString("s-consumerHost");
        }
        if (requesJson.containsKey("s-consumerEndPoint")) {
            consumerEndPoint = requesJson.getString("s-consumerEndPoint");
        }
        if (requesJson.containsKey("s-group")) {
            group = requesJson.getString("s-group");
        }
        //long clickTime = TimeUtil.currentTimeMillis();
        if (loadBalance.equals("enabled")) {
            InstanceInfo serviceInstance;
            if (group==null) serviceInstance = loadBalanceClient.chooseServer(service);
            else serviceInstance = loadBalanceClient.chooseServer(service,group);
            URI uri = URI.create(ipaddr);
            ipaddr = loadBalanceClient.reconstructURI(serviceInstance, uri).toString();
        }
        //String address = service + "@@" + ipaddr + "@@" + consumerName;

        // QpsHelper qpsHelper = null;
        // if (QPSHelpers.get(address) == null) {
        //     qpsHelper = new QpsHelper();
        //     QPSHelpers.put(address, qpsHelper);
        // }
        // qpsHelper = QPSHelpers.get(address);
        //long startTime = TimeUtil.currentTimeMillis();
        try {
            Map<String, String> params = null;
            if (headers != null) {
                params = JSONObject.parseObject(headers, new TypeReference<Map<String, String>>() {
                });
            }
            //访问
            logger.info("ipaddr {},params {},body {},method {}",ipaddr,params,body,method);
            response = HttpClientUtil.httpRequest(ipaddr, params, body, method);
            // 业务逻辑
            // long endTime = TimeUtil.currentTimeMillis();
            // long rt = endTime - startTime;
            // qpsHelper.incrSuccess(rt);
            //调用记录
//            Ref[] refs = new Ref[1];
//            if (consumerHost.equals("") || consumerEndPoint.equals("")) {
//                refs[0] = new Ref(consumerName);
//            } else {
//                refs[0] = new Ref(consumerName, consumerHost, consumerEndPoint, consumerHost);
//            }
//            Span[] spans = new Span[1];
//            spans[0] = new Span(startTime, endTime, "/" + ipaddr.split(":.*/")[1], refs);
//            MonitorRawData monitorRawData = new MonitorRawData(service, ipaddr, spans);
//            if (monitorRawDataMap.containsKey(address + "success")) {
//                monitorRawDataMap.get(address + "success").add(monitorRawData);
//            } else {
//                monitorRawDataMap.put(address + "success", new CopyOnWriteArrayList<MonitorRawData>());
//                monitorRawDataMap.get(address + "success").add(monitorRawData);
//            }
            //collectDataHighPerformance(service, ipaddr, qpsHelper, clickTime, consumerName);
            String responseBody = response.body().string();
            json.put("body", responseBody);
            json.put("headers", response.headers());
            json.put("code", response.code());
            json.put("isRedirect", response.isRedirect());
            json.put("isSuccessful", response.isSuccessful());
//            return json.toString();
            return responseBody;
        } catch (Exception e) {
            //qpsHelper.incrException();
            //调用记录
//            Span[] spans = new Span[1];
//            Ref[] refs = new Ref[1];
//            if (consumerHost.equals("") || consumerEndPoint.equals("")) {
//                refs[0] = new Ref(consumerName);
//            } else {
//                refs[0] = new Ref(consumerName, consumerHost, consumerEndPoint, consumerHost);
//            }
//            spans[0] = new Span(startTime, TimeUtil.currentTimeMillis(), "/" + ipaddr.split(":.*/")[1], refs, true);
//            MonitorRawData monitorRawData = new MonitorRawData(service, ipaddr, spans);
//            if (monitorRawDataMap.containsKey(address + "failed")) {
//                monitorRawDataMap.get(address + "failed").add(monitorRawData);
//            } else {
//                monitorRawDataMap.put(address + "failed", new CopyOnWriteArrayList<MonitorRawData>());
//                monitorRawDataMap.get(address + "failed").add(monitorRawData);
//            }
            //collectDataHighPerformance(service, ipaddr, qpsHelper, clickTime, consumerName);
            e.printStackTrace();
            return e.toString();
        } finally {
            response.close();
        }
    }
}
