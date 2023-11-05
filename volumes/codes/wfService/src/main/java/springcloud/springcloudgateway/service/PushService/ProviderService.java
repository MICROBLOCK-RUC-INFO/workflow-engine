// package springcloud.springcloudgateway.service.PushService;

// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
// import org.hyperledger.fabric.sdk.ProposalResponse;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.ApplicationContext;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;
// //import springcloud.springcloudgateway.config.BlockchainConfig;
// //import springcloud.springcloudgateway.config.fabricConfig.MonitorChannelConfig;
// import springcloud.springcloudgateway.service.fabric.FabricServiceImpl;
// import springcloud.springcloudgateway.service.qps.QpsHelper;
// import springcloud.springcloudgateway.service.qps.TimeUtil;
// import springcloud.springcloudgateway.service.utils.HttpClientUtil;

// import javax.annotation.PostConstruct;
// import javax.annotation.Resource;
// import java.net.UnknownHostException;
// import java.sql.Timestamp;
// import java.util.*;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.CopyOnWriteArrayList;
// import java.util.concurrent.FutureTask;
// import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mirror
 */
// @Service
// public class ProviderService {
//     public static String port;

//     @Resource
//     Push push;

//     @Value("${server.port}")
//     public void setPort(String p) {
//         port = p;
//     }

//     private Random rand = new Random();
//     @Resource
//     HttpClientUtil httpClientUtil;
//     public static final String TPS_Request = "totalRequestInSec";
//     public static final String TPS_Success = "totalSuccessInSec";
//     public static final String TPS_Exception = "totalExceptionInSec";
//     public static final String RT_Min = "minRtInSec";
//     public static final String RT_Avg = "avgRtInSec";
//     public static final String QPS_exception = "exceptionQps";
//     public static final String QPS_success = "successQps";
//     private Timer timer = new Timer();
//     public static ConcurrentHashMap<Long, ConcurrentHashMap<String, JSONObject>> monitorDataMap = new ConcurrentHashMap<>();
//     public static ConcurrentHashMap<Long, ConcurrentHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> clusterMap = new ConcurrentHashMap<>();
//     private static ConcurrentHashMap<String, Integer> clusterResultMap = new ConcurrentHashMap<>();
//     @Resource
//     private FabricServiceImpl fabricService;
//     public static AtomicInteger currentNodeNum = new AtomicInteger(8);
//     @Resource
//     MonitorChannelConfig monitorChannelConfig;
//     private Logger logger = LoggerFactory.getLogger(ProviderService.class);
//     private static QpsHelper qpsHelper = new QpsHelper();
//     @Autowired
//     ApplicationContext applicationContext;

//     public ProviderService() {
//     }

//     @PostConstruct
//     public void init() {
//         currentNodeNum.set(monitorChannelConfig.getNodeNum());
//     }

//     /**
//      * 上链
//      *
//      * @return
//      */
//     public String fabricPut(String key, String monitorData, String rawData, String time) throws Exception {
//         String channelName = "monitorchannel";
//         String chainCodeName = "monitor";
//         String func = "Put";
//         String res = "putting...";
//         ArrayList<String> params = new ArrayList<>();
//         params.add(key);
//         params.add(monitorData);
//         params.add(rawData);
//         params.add(time);
//         //上链
//         System.out.println("1::" + TimeUtil.currentTimeMillis());
//         Collection<ProposalResponse> proposals = fabricService.invoke(channelName, chainCodeName, func, params);
//         System.out.println("end::" + TimeUtil.currentTimeMillis());
//         for (ProposalResponse response : proposals) {
//             if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
//                 res = new String(response.getChaincodeActionResponsePayload());
//             } else {
//                 res = new String(response.getChaincodeActionResponsePayload());
//             }
//         }
//         return res;
//     }

//     public void transferMonitorData(String key, String monitorData, String rawData, String mysqlData, String time, String monitorHost) throws Exception {
//         //选举主节点
//         JSONObject json = new JSONObject();
//         json.put("monitorData", monitorData);
//         json.put("rawData", rawData);
//         json.put("mysqlData", mysqlData);
//         json.put("time", time);
//         json.put("monitorHost", monitorHost);
//         long timestamp = Long.parseLong(time);
//         if (isMasterNode(timestamp)) {
//             //调用本地方法
//             sendDataToChain(json.toJSONString());
//         } else {
//             transferLocalMonitorData(json.toJSONString(), time);
//         }

//     }

//     public List<MonitorRawData> systematicSampling(Map<String, CopyOnWriteArrayList<MonitorRawData>> data) {
//         int cnt = data.size();
//         List<MonitorRawData> valueList = new ArrayList<MonitorRawData>();
//         //分层抽样
//         for (CopyOnWriteArrayList<MonitorRawData> dataLists : data.values()) {
//             valueList.addAll(dataLists);
//         }
// //        return systematicSampleData(valueList,cnt/10,10);
//         //全部数据上链
//         return valueList;
//     }

//     /**
//      * 分层系统抽样
//      *
//      * @param data
//      * @return
//      */
//     public List<MonitorRawData> StratifiedSampling(Map<String, CopyOnWriteArrayList<MonitorRawData>> data) {
//         int cnt = data.size();
//         List<MonitorRawData> valueList = new ArrayList<MonitorRawData>();
//         //分层系统抽样
//         for (CopyOnWriteArrayList<MonitorRawData> dataLists : data.values()) {
// //            valueList.addAll(systematicSampleData(dataLists,cnt/10,10));
//             valueList.addAll(dataLists);
//         }
//         return valueList;
//     }

//     public static String getIp() throws UnknownHostException {
//         return BlockchainConfig.hostName;
//     }

//     public JSONArray addData(long time, long clickTime, long serviceTime, String consumerName, String mode) throws UnknownHostException {
//         JSONArray dataArrays = new JSONArray();
//         ArrayList<JSONObject> blockchainArrays = new ArrayList<>();
//         ArrayList<JSONArray> mysqlArrays = new ArrayList<>();
//         ArrayList<JSONArray> rawDataArrays = new ArrayList<>();
//         for (int i = 0; i < BlockchainConfig.packetNums; i++) {
//             blockchainArrays.add(new JSONObject());
//             mysqlArrays.add(new JSONArray());
//             rawDataArrays.add(new JSONArray());
//         }
//         int count = 0, rawCount = 0;
//         //取得原始数据
//         List<MonitorRawData> monitorRawDataArray = systematicSampling(MonitorService.monitorRawDataMap);
//         for (MonitorRawData monitorRawData : monitorRawDataArray) {
//             int num = (rawCount++) % BlockchainConfig.packetNums;
//             JSONObject monitorRawJson = JSONObject.parseObject(JSONObject.toJSON(monitorRawData).toString());
//             rawDataArrays.get(num).add(monitorRawJson);
//         }
//         for (String key : MonitorService.keyMap) {
//             //取出上一秒内访问的服务信息
//             int num = (count++) % BlockchainConfig.packetNums;
//             String service = key.split("@@")[0];
//             String ipaddr = key.split("@@")[1];
//             QpsHelper qpsHelper = MonitorService.QPSHelpers.get(key);
//             JSONObject monitorInfo = new JSONObject();
//             JSONObject jsonObject = new JSONObject();
//             monitorInfo.put("serviceName", service);
//             monitorInfo.put("serviceHost", ipaddr);
//             monitorInfo.put("consumerName", consumerName);
//             monitorInfo.put("time", time);
//             monitorInfo.put("mode", mode);
//             monitorInfo.put("monitorHost", getIp() + ":" + port);
//             double totalRequest = qpsHelper.getTotalRequest();
//             double totalException = qpsHelper.getTotalException();
//             double totalSuccess = qpsHelper.getTotalSuccess();
//             double avgRt = qpsHelper.getAvgRt();
//             String clusterResult = "1";
//             jsonObject.put("clusterExpect", 1);
//             jsonObject.put("trueTotalRequest", totalRequest);
//             jsonObject.put("trueTotalException", totalException);
//             jsonObject.put("trueTotalSuccess", totalSuccess);
//             jsonObject.put("trueAvgRt", avgRt);
//             //作假start
//             double[] rands = new double[3];
//             int[] seeds = new int[3];
//             for (int i = 0; i < 3; i++) {
//                 seeds[i] = rand.nextInt(2);
//                 if (seeds[i] == 0) {
//                     rands[i] = 0.35 + 0.09 * rand.nextGaussian();
//                 } else {
//                     rands[i] = 6 + 1.5 * rand.nextGaussian();
//                 }
//             }
//             totalRequest = totalRequest * rands[0];
//             totalSuccess = totalSuccess * rands[1];
//             avgRt = avgRt * rands[2];
//             clusterResult = "999";
//             jsonObject.put("clusterExpect", 999);
// //            //作假end
//             monitorInfo.put(TPS_Request, totalRequest);
//             monitorInfo.put(TPS_Exception, totalException);
//             monitorInfo.put(TPS_Success, totalSuccess);
//             monitorInfo.put(RT_Min, qpsHelper.getMinRt());
//             monitorInfo.put(RT_Avg, avgRt);
//             monitorInfo.put(QPS_success, qpsHelper.getSuccessQps());
//             monitorInfo.put(QPS_exception, qpsHelper.getExceptionQps());
//             monitorInfo.put("clusterResult", clusterResult);
//             blockchainArrays.get(num).put(service + "@@" + ipaddr, monitorInfo);
//             //mysql未经过验证数据
//             String seriviceCompleted = qpsHelper.getTotalException() == 0 ? "success" : "failed";
//             jsonObject.put("clickTime", clickTime);
//             jsonObject.put("serviceTime", serviceTime);
//             jsonObject.put("service", service);
//             jsonObject.put("ipaddr", ipaddr);
//             jsonObject.put("serviceDelay", qpsHelper.getAvgRt());
//             jsonObject.put("MonitorIp", getIp());
//             jsonObject.put("MonitorPort", port);
//             jsonObject.put("nums", (int) qpsHelper.getTotalRequest());
//             jsonObject.put("serviceCompleted", seriviceCompleted);
//             jsonObject.put("totalRequest", totalRequest);
//             jsonObject.put("totalException", totalException);
//             jsonObject.put("totalSuccess", totalSuccess);
//             jsonObject.put("avgRt", avgRt);
//             mysqlArrays.get(num).add(jsonObject);
//         }
//         for (int i = 0; i < BlockchainConfig.packetNums; i++) {
//             String fabric_key = time + "@@" + getIp() + ":" + port + "-" + i;
//             String blockchainData = blockchainArrays.get(i).toJSONString();
//             String rawData = rawDataArrays.get(i).toJSONString();
//             JSONObject data = new JSONObject();
//             data.put("mysqlData", mysqlArrays.get(i));
//             data.put("blockchainData", blockchainData);
//             data.put("rawData", rawData);
//             data.put("key", fabric_key);
//             data.put("time", time);
//             data.put("monitorHost", getIp() + ":" + port);
//             dataArrays.add(data);
//         }
//         return dataArrays;
//     }

//     public Boolean isMasterNode(long time) {
//         long number = time / 1000 + currentNodeNum.get();
//         if (number % 8 == 0) {
//             return true;
//         } else {
//             return false;
//         }
//     }

//     public void transferLocalMonitorData(String data, String time) throws Exception {
//         String masterServer = null;
//         Map<String, String> params = new HashMap<>();
//         params.put("timestamp", time);
//         for (String server : monitorChannelConfig.getIpList()) {
//             httpClientUtil.asyncGetMasterNode(server, params, null, "GET", data);
//         }
//     }

//     public String getLocalMonitorData() {
//         currentNodeNum.set(currentNodeNum.incrementAndGet() % monitorChannelConfig.getNodeSum());
//         return "default";
//     }

//     //
//     public synchronized void sendDataToChain(String data) {
//         JSONObject jsonObject = JSON.parseObject(data);
//         String time = jsonObject.getString("time");
//         String monitorHost = jsonObject.getString("monitorHost");
//         long timestamp = Long.parseLong(time);
//         long startTime = TimeUtil.currentTimeMillis();
//         JSONObject monitorData = jsonObject.getJSONObject("monitorData");
//         if (!monitorDataMap.containsKey(timestamp)) {
//             ConcurrentHashMap<String, JSONObject> monitorMap = new ConcurrentHashMap<>();
//             monitorDataMap.put(timestamp, monitorMap);
//             TimerTask task = new TimerTask() {
//                 @Override
//                 public void run() {
//                     sendByTimer(timestamp, startTime);
//                 }
//             };
//             timer.schedule(task, monitorChannelConfig.getTimeLimit() * 1000);
//         }
//         monitorDataMap.get(timestamp).put(monitorHost, jsonObject);
//         for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
//             JSONObject instanceData = (JSONObject) entry.getValue();
//             String key = entry.getKey();
//             double totalExceptionInSec = instanceData.getDouble("totalExceptionInSec");
//             double totalSuccessInSec = instanceData.getDouble("totalSuccessInSec");
//             double totalRequestInSec = instanceData.getDouble("totalRequestInSec");
//             double avgRtInSec = instanceData.getDouble("avgRtInSec");
//             ArrayList<Double> list = new ArrayList<>();
//             //四个指标
//             list.add(totalExceptionInSec);
//             list.add(totalSuccessInSec);
//             list.add(totalRequestInSec);
//             list.add(avgRtInSec);
//             if (!clusterMap.containsKey(timestamp)) {
//                 ConcurrentHashMap<String, LinkedHashMap<String, ArrayList<Double>>> clusterMapInTime = new ConcurrentHashMap<>();
//                 clusterMap.put(timestamp, clusterMapInTime);
//             }
//             if (!clusterMap.get(timestamp).containsKey(key)) {
//                 LinkedHashMap<String, ArrayList<Double>> listMap = new LinkedHashMap<>();
//                 clusterMap.get(timestamp).put(key, listMap);
//             }
//             clusterMap.get(timestamp).get(key).put(monitorHost, list);
//         }
//         if (monitorDataMap.get(timestamp).size() >= monitorChannelConfig.getNodeSum()) {
//             ProviderService bean = applicationContext.getBean(ProviderService.class);
//             bean.send(timestamp, "normal", startTime);
//         }
//     }

//     @Async("taskExecutor")
//     public void send(long timestamp, String mode, long startTime) {
//         String time = String.valueOf(timestamp);
//         if (!monitorDataMap.containsKey(timestamp) || !clusterMap.containsKey(timestamp)) {
//             return;
//         }
//         if (monitorDataMap.get(timestamp).size() == 0 || clusterMap.get(timestamp).size() == 0) {
//             return;
//         }
//         JSONObject blockchainData = new JSONObject();
//         try {
//             Iterator<String> iterator = clusterMap.get(timestamp).keySet().iterator();
//             while (iterator.hasNext()) {
//                 String serviceInstance = iterator.next();
//                 Map<String, ArrayList<Double>> listMap = clusterMap.get(timestamp).get(serviceInstance);
//                 double[][] arrays = new double[listMap.size()][];
//                 int cnt = 0;
//                 Iterator<String> listIterator = listMap.keySet().iterator();
//                 HashMap<Integer, String> monitorHostIndexMap = new HashMap<>();
//                 while (listIterator.hasNext()) {
//                     String monitorHost = listIterator.next();
//                     ArrayList<Double> arrayList = listMap.get(monitorHost);
//                     double[] array = new double[arrayList.size()];
//                     for (int i = 0; i < arrayList.size(); i++) {
//                         array[i] = arrayList.get(i);
//                     }
//                     monitorHostIndexMap.put(cnt, monitorHost);
//                     arrays[cnt++] = array;
//                 }
//                 FutureTask<Map<Long, ArrayList<Long>>> result = new FutureTask<Map<Long, ArrayList<Long>>>(new ClusterCallable(JniInterpreter.getClusterInterpreter(), arrays));
//                 JniInterpreter.jniThreadPool.submit(result);
//                 if (result.get() == null) {
//                     throw new Exception("cluster result is new ,it is the first time to call ,please retry");
//                 }
//                 Long maxIndex = 1L;
//                 int count = 0;
//                 for (Map.Entry<Long, ArrayList<Long>> clusterEntry : result.get().entrySet()) {
//                     if (clusterEntry.getValue().size() > count) {
//                         maxIndex = clusterEntry.getKey();
//                         count = clusterEntry.getValue().size();
//                     }
//                 }
//                 if (maxIndex != 1L) {
//                     ArrayList<Long> maxItem = result.get().get(maxIndex);
//                     ArrayList<Long> firstItem = result.get().get(1L);
//                     result.get().put(maxIndex, firstItem);
//                     result.get().put(1L, maxItem);
//                 }
//                 for (Map.Entry<Long, ArrayList<Long>> clusterEntry : result.get().entrySet()) {
//                     if (clusterEntry.getKey().intValue() != 1) {
//                         for (Long index : clusterEntry.getValue()) {
//                             String monitorHost = monitorHostIndexMap.get(index.intValue());
//                             JSONObject doubtfulData = monitorDataMap.get(timestamp).get(monitorHost);
//                             JSONObject doubtfulMonitorData = doubtfulData.getJSONObject("monitorData");
//                             JSONObject modifyData = doubtfulMonitorData.getJSONObject(serviceInstance);
//                             modifyData.put("clusterResult", clusterEntry.getKey());
//                             doubtfulMonitorData.put(serviceInstance, modifyData);
//                             doubtfulData.put("monitorData", doubtfulMonitorData);
//                             monitorDataMap.get(timestamp).put(monitorHost, doubtfulData);
//                             clusterResultMap.put(serviceInstance + "@@" + monitorHost + "@@" + time, clusterEntry.getKey().intValue());
//                         }
//                     }
//                 }
//                 for (Map.Entry<String, JSONObject> monitorEntry : monitorDataMap.get(timestamp).entrySet()) {
//                     JSONObject object = monitorEntry.getValue().getJSONObject("monitorData");
//                     for (Map.Entry entry : object.entrySet()) {
//                         String monitorHost = ((JSONObject) entry.getValue()).getString("monitorHost");
//                         blockchainData.put((String) entry.getKey() + "@@" + monitorHost, entry.getValue());
//                     }
//                 }
//             }
//             //数据上链
//             long serviceTime = TimeUtil.currentTimeMillis();
//             String res = fabricPut(time + "@@", blockchainData.toJSONString(), "[]", time);
//             long blockchainTime = TimeUtil.currentTimeMillis();
//             //采集上链数据
//             String blockchainCompleted = "unknown";
//             if (res.endsWith("success")) {
//                 blockchainCompleted = "success";
//             } else {
//                 blockchainCompleted = "failed";
//             }
//             UUID id = UUID.randomUUID();
//             String uuid = id.toString().replace("-", "");
//             for (Map.Entry<String, JSONObject> monitorEntry : monitorDataMap.get(timestamp).entrySet()) {
//                 JSONArray mysqlArray = monitorEntry.getValue().getJSONArray("mysqlData");
//                 for (int j = 0; j < mysqlArray.size(); j++) {
//                     JSONObject mysqlData = mysqlArray.getJSONObject(j);
//                     long clickTime = timestamp;
//                     String service = mysqlData.getString("service");
//                     String ipaddr = mysqlData.getString("ipaddr");
//                     double serviceDelay = mysqlData.getDouble("serviceDelay");
//                     String MonitorIp = mysqlData.getString("MonitorIp");
//                     String MonitorPort = mysqlData.getString("MonitorPort");
//                     int nums = Integer.parseInt(mysqlData.getString("nums"));
//                     String seriviceCompleted = mysqlData.getString("serviceCompleted");
//                     Timestamp servicetime = new Timestamp(serviceTime);
//                     Timestamp clicktime = new Timestamp(clickTime);
//                     Timestamp blockchainTimestamp = new Timestamp(blockchainTime);
//                     int clusterExpect = mysqlData.getInteger("clusterExpect");
//                     int clusterResult = clusterResultMap.getOrDefault(service + "@@" + ipaddr + "@@" + MonitorIp + ":" + MonitorPort + "@@" + time, 1);
//                     int totalRequest = mysqlData.getInteger("totalRequest");
//                     int totalException = mysqlData.getInteger("totalException");
//                     int totalSuccess = mysqlData.getInteger("totalSuccess");
//                     double avgRt = mysqlData.getDouble("avgRt");
//                     int trueTotalRequest = mysqlData.getInteger("trueTotalRequest");
//                     int trueTotalException = mysqlData.getInteger("trueTotalException");
//                     int trueTotalSuccess = mysqlData.getInteger("trueTotalSuccess");
//                     double trueAvgRt = mysqlData.getDouble("trueAvgRt");
//                     MonitorPort = MonitorPort + "-" + mode;
//                     Consistency.insertMonitorData(clicktime, servicetime, blockchainTimestamp, clickTime, blockchainTime, service, ipaddr, MonitorIp, MonitorPort, serviceDelay, (int) (blockchainTime - serviceTime), (int) (blockchainTime - startTime), nums, seriviceCompleted, blockchainCompleted, clusterExpect, clusterResult, totalRequest, totalException, totalSuccess, avgRt, trueTotalRequest, trueTotalException, trueTotalSuccess, trueAvgRt, uuid);
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         } finally {
//             monitorDataMap.remove(timestamp);
//             //TODO:多线程环境下可能会删除别的数据，要加以区分
//             clusterMap.remove(timestamp);
//             clusterResultMap.clear();
//         }
//     }


//     public void sendByTimer(long timestamp, long startTime) {
//         send(timestamp, "sendByTimer", startTime);
//     }

//     public static void main(String[] args) throws InterruptedException {
// //        JniInterpreter.getClusterInterpreter();
// //        long start = TimeUtil.currentTimeMillis();
// //        for (int i = 0; i < 1; i++) {
// //            double[][] array = {{200, 200, 40}, {200, 200, 50}, {200, 200, 45}, {200, 200, 35}, {200, 200, 40}, {200, 200, 45}, {200, 195, 40}, {200, 200, 150}};
// //            FutureTask<Map<Integer, ArrayList<Integer>>> result = new FutureTask<Map<Integer, ArrayList<Integer>>>(new ClusterCallable(JniInterpreter.getClusterInterpreter(), array));
// //            JniInterpreter.jniThreadPool.submit(result);
// //            try {
// //                System.out.println(result.get());
// //            } catch (InterruptedException e) {
// //                e.printStackTrace();
// //            } catch (ExecutionException e) {
// //                e.printStackTrace();
// //            }
// //        }
// //        System.out.println(TimeUtil.currentTimeMillis() - start);
//     }

// }
