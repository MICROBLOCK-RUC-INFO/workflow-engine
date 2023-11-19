package springcloud.springcloudgateway.workflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Base64.Decoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.print.DocFlavor.STRING;

import org.activiti.engine.impl.db.workflowClass.workflowResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import springcloud.springcloudgateway.workflow.simulateCache.*;
import springcloud.springcloudgateway.workflow.context.wfEngineContext;
import springcloud.springcloudgateway.workflow.helper.wfConfig;
import springcloud.springcloudgateway.workflow.helper.workflowFabric;
import springcloud.springcloudgateway.workflow.thread.runnable.commonUseUpLinkRunnable;
import springcloud.springcloudgateway.workflow.thread.runnable.upLinkRunnable;
import springcloud.springcloudgateway.workflow.threadExecutor.activitiChangeExecutor;
import springcloud.springcloudgateway.workflow.threadExecutor.flushThreadPool;
import springcloud.springcloudgateway.workflow.tools.deCoder;
import springcloud.springcloudgateway.workflow.tools.httpUtil;
import springcloud.springcloudgateway.workflow.tools.jsonTransfer;
import springcloud.springcloudgateway.workflow.tools.keyCombination;
import springcloud.springcloudgateway.workflow.tools.distributedLock.distributedLock;

/**
 * @apiNote 其实wfEngine与wfService的代码是可以合并的，但是要考虑依赖的版本兼容问题
 */

@Service
public class wfEngine {
    @Resource
    workflowFabric workflowFabric;
    @Resource
    wfConfig wfConfig;
    @Resource
    preExecutionCache preExecutionCache;
    @Resource
    commonUseSimulateCache commonUseSimulateCache;
    @Resource
    flushThreadPool flushThreadPool;
    @Resource
    activitiChangeExecutor activitiChangeExecutor;
    public static int packageCounts=0;
    private static Integer packageCountsMutex=-1;
    public static int transactionCounts=0;
    private static Integer transactionCountMutex=-1;
    public final String instance="instance";
    public final String complete="complete";
    public final String deploy="deploy";
    private final String channelName="workflowchannel";
    private final String chaincodeName="wfscc";
    private static final Logger logger = LoggerFactory.getLogger(wfEngine.class);
    private final Set<String> httpMethods=new HashSet<String>(){{add("GET");add("POST");add("PUT");add("DELETE");}};
    public enum queryType {
        deployments,deploymentByName,statusByDeploymentName,statusByOid
    } 

    private Map<queryType,String> queryRoutes=new HashMap<queryType,String>(){{
        put(queryType.deployments,"/wfEngine/queryDeployment");
        put(queryType.deploymentByName,"/wfEngine/queryDeploymentByName");
        put(queryType.statusByDeploymentName,"/wfEngine/queryStatusByDeploymentName/");
        put(queryType.statusByOid,"/wfEngine/queryStatusByOid/");
    }};

    //不需要参数的查询
    public String query(queryType type) throws InterruptedException, ExecutionException {
        String port=wfConfig.getWorkflowPort();
        List<String> querys=new LinkedList<String>();
        Iterator<String> peerIps= workflowFabric.getPeersIp(channelName).iterator();
        StringBuilder sb=new StringBuilder();
        String route=queryRoutes.get(type);
        while (peerIps.hasNext()) {
            sb.append("http://").append(peerIps.next()).append(':')
              .append(port).append(route);
            querys.add(sb.toString());
            sb.setLength(0);
        }
        List<Future<SimpleHttpResponse>> futures=httpUtil.mutilGet(querys);
        Pair<Boolean,String> compareResult= compareResponse(futures);
        return compareResult.getValue();
    }

    //单一值查询
    public String query(String value,queryType type) throws InterruptedException, ExecutionException {
        String port=wfConfig.getWorkflowPort();
        List<String> querys=new LinkedList<String>();
        Iterator<String> peerIps= workflowFabric.getPeersIp(channelName).iterator();
        StringBuilder sb=new StringBuilder();
        String route=queryRoutes.get(type)+value;
        while (peerIps.hasNext()) {
            sb.append("http://").append(peerIps.next()).append(':')
              .append(port).append(route);
            querys.add(sb.toString());
            sb.setLength(0);
        }
        List<Future<SimpleHttpResponse>> futures=httpUtil.mutilGet(querys);
        Pair<Boolean,String> compareResult= compareResponse(futures);
        //这里还差一个上链应该，得考虑如何实现
        return compareResult.getValue();
    }

    public String verifyServiceDynamicBindInput(String serviceName,String httpMethod,String route,String input,String serviceGroup,
                                                String headers,String output) {
        Map<String,Object> serviceInfo=new HashMap<String,Object>(){{
            put("serviceName",serviceName);
            put("httpMethod",verifyHttpMethod(httpMethod));
            put("route",route);
        }};
        if (!serviceGroup.equals("")) serviceInfo.put("serviceGroup",serviceGroup);
        if (!input.equals("")) serviceInfo.put("input",verifyInput(input));
        if (!headers.equals("")) serviceInfo.put("headers",verifyHeaders(headers));
        if (!output.equals("")) serviceInfo.put("output",verifyOutput(output));
        return jsonTransfer.mapToJsonString(serviceInfo);
    }

    private String verifyHttpMethod(String httpMethod) {
        httpMethod=httpMethod.toUpperCase();
        if (!httpMethods.contains(httpMethod)) throw new RuntimeException(String.format("httpMethod: %s not supported",httpMethod));
        else return httpMethod;
    }

    private String verifyInput(String input) {
        if (jsonTransfer.jsonToMap(input)==null) throw new RuntimeException(String.format("input: %s is not json", input));
        return input;
    }

    private String verifyHeaders(String headers) {
        if (jsonTransfer.jsonToMap(headers)==null) throw new RuntimeException(String.format("headers: %s is not json", headers));
        return headers;
    }

    private String verifyOutput(String output) {
        if (jsonTransfer.jsonToMap(output)==null) throw new RuntimeException(String.format("output: %s is not json",output));
        return output;
    }


    public String testSmartContract(String requestBody,String fcn) {
        try {
            if (fcn.equals(deploy)) {
                CompletableFuture<TransactionEvent> transactionEvent =
                                     workflowFabric.invoke(channelName, "scscc", fcn, new ArrayList<String>(){{add("test");}});
                if(transactionEvent.get().isValid()) return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",200);put("body","ok");}});
                else return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",500);put("body","failed");}});
            } else if (fcn.equals(instance)) {
                Map<String,Object> requestMap=jsonTransfer.jsonToMap(requestBody);
                String deploymentName=String.valueOf(requestMap.get("deploymentName"));
                String oid=deploymentName+"@"+UUID.randomUUID().toString();
                String processData=String.valueOf(requestMap.get("processData"));
                String businessData=String.valueOf(requestMap.get("businessData"));
                ArrayList<String> args=new ArrayList<String>(){{
                    add(oid);add(deploymentName);add(processData);add(businessData);
                }};
                CompletableFuture<TransactionEvent> transactionEvent = 
                        workflowFabric.invoke(channelName, "scscc", fcn, args);
                if(transactionEvent.get().isValid()) {
                    return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{
                        put("code",200);
                        put("oid",oid);
                        put("body","ok");
                    }});
                } else return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",500);put("body","failed");}});
            } else if (fcn.equals(complete)) {
                Map<String,Object> requestMap=jsonTransfer.jsonToMap(requestBody);
                String oid=String.valueOf(requestMap.get("oid"));
                String taskName=String.valueOf(requestMap.get("taskName"));
                String processData=String.valueOf(requestMap.get("processData"));
                String businessData=String.valueOf(requestMap.get("businessData"));
                ArrayList<String> args=new ArrayList<String>() {{
                    add(oid);add(taskName);add(processData);add(businessData);
                }};
                CompletableFuture<TransactionEvent> transactionEvent = 
                            workflowFabric.invoke(channelName, "scscc", fcn, args);
                if(transactionEvent.get().isValid()) return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",200);put("body","ok");}});
                else return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",500);put("body","failed");}});
            } else return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",500);put("body","no such function");}});
        } catch (Exception e) {
            logger.warn("testSmartContract error");
            return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("code",500);put("body","failed");}});
        }
    }

    //统计大包的数量与交易的总数
    private void packageAndTransactionCount(int transactionSize) {
        synchronized (transactionCountMutex) {
            transactionCounts+=transactionSize;
        }
        synchronized (packageCountsMutex) {
            packageCounts+=1;
        }
    }

    //因为这个是测试跑完之后取值，所以没有加锁
    public List<Integer> getPackageAndTransactionCount() {
        List<Integer> list=new ArrayList<Integer>() {{
            add(packageCounts);
            add(transactionCounts);
        }};
        packageCounts=0;
        transactionCounts=0;
        return list;
    }

    private List<SimpleHttpResponse> flushbroadCast(String body) throws IOException, InterruptedException, ExecutionException {
        List<String> allPeerIps=workflowFabric.getPeersIp(channelName);
        String port=wfConfig.getWorkflowPort();
        if (allPeerIps==null) {
            logger.error("get all peers ip failure");
        }
        List<Future<SimpleHttpResponse>> futures=new LinkedList<>();
        for (String ip:allPeerIps) {
            String url="http://"+ip+":"+port+"/wfEngine/flush";
            futures.add(httpUtil.doPost(url, body));
        }
        List<SimpleHttpResponse> responses=new LinkedList<>();
        for (Future<SimpleHttpResponse> future:futures) {
            SimpleHttpResponse response=future.get();
            responses.add(response);
        }
        return responses;
    }
    
    //just for use
    public String deleteDeploymentByName(String request) throws InterruptedException, ExecutionException {
        Map<String,Object> responseMap=new HashMap<>();
        List<String> allPeerIps=workflowFabric.getPeersIp(channelName);
        String port=wfConfig.getWorkflowPort();
        if (allPeerIps==null) {
            responseMap.put("code",500);
            responseMap.put("body","delete error,cause by get peer ip failed");
            return jsonTransfer.mapToJsonString(responseMap);
        }
        List<Future<SimpleHttpResponse>> futures=new LinkedList<>();
        StringBuilder url=new StringBuilder().append("http://");
        for (String ip:allPeerIps) {
            url.append(ip).append(':').append(port).append("/wfEngine").append("/deleteDeployment");
            futures.add(httpUtil.doPost(url.toString(), request));
            url.setLength(7);
        }
        StringBuilder responseStringBuilder=new StringBuilder();
        boolean allOk=true;
        responseStringBuilder.append("delete error,cause there are some peers execute error,their are:");
        for (int i=0;i<futures.size();i++) {
            SimpleHttpResponse response=futures.get(i).get();
            if (response.getCode()!=200||!response.getBodyText().equals("ok")) {
                allOk=false;
                responseStringBuilder.append(allPeerIps.stream().skip(i).findFirst().get()).append(',');
            }
        }
        if (allOk) {
            responseMap.put("code",200);
            responseMap.put("body","ok");
        } else {
            responseMap.put("code",500);
            responseMap.put("body",responseStringBuilder.toString());
        }
        return jsonTransfer.mapToJsonString(responseMap);
    }

    
    //广播所有节点，并回收所有模拟执行返回结果 bodyMap需要传输的数据，method方法
    private Pair<workflowResponse, Pair<String,List<SimpleHttpResponse>>> broadCast(Map<String,Object> bodyMap,String fcn,String Oid) throws IOException, InterruptedException, ExecutionException {
        List<String> allPeerIps=workflowFabric.getPeersIp(channelName);
        String port=wfConfig.getWorkflowPort();
        String localIp=wfConfig.getPeerName();
        workflowResponse localWorkflowResponse=null;
        if (allPeerIps==null) {
            logger.error("get all peers ip failure");
            return null;
        }
        if (fcn.equals(instance)||fcn.equals(complete)||fcn.equals(deploy)) {
            String url =null;
            if (fcn.equals(instance)) {
                url="http://localhost:"+port+"/wfEngine/wfInstance/"+URLEncoder.encode(Oid, "UTF-8");
            } else if(fcn.equals(complete)) {
                url="http://localhost:"+port+"/wfEngine/wfComplete/"+URLEncoder.encode(Oid, "UTF-8");
            } else {
                url="http://localhost:"+port+"/wfEngine/wfDeploy";
            }
            Future<SimpleHttpResponse> future=httpUtil.doPost(url, jsonTransfer.mapToJsonString(bodyMap));
            SimpleHttpResponse response=future.get();
            if (response.getCode()!=200) {
                return Pair.of(null, Pair.of(response.getBodyText(), null));
            }
            String localSimulation=future.get().getBodyText();
            localWorkflowResponse=(workflowResponse)deCoder.streamToEntity(localSimulation);
            //pair=Pair.of(localWorkflowResponse, null);
            bodyMap.put("serviceTaskResultJson",localWorkflowResponse.getServiceTaskResultJson());
            //System.out.println(bodyMap.toString());
        }
        if (localWorkflowResponse==null) return null;
        //List<Call> calls=new LinkedList<>();
        List<String> urls=new ArrayList<String>();
        String body=jsonTransfer.mapToJsonString(bodyMap);
        for (String ip:allPeerIps) {
            String url;
            //如果是本机ip则跳过
            if (ip.equals(localIp)) continue;
            if (fcn.equals(deploy)) {
                url="http://"+ip+":"+port+"/wfEngine/wfDeploy";
            } else if (fcn.equals(complete)) {
                url="http://"+ip+":"+port+"/wfEngine/wfComplete/"+URLEncoder.encode(Oid, "UTF-8");
            } else if (fcn.equals(instance)) {
                url="http://"+ip+":"+port+"/wfEngine/wfInstance/"+URLEncoder.encode(Oid, "UTF-8");
                //url="http://"+ip+":"+port+"/wfEngine/hello";
            } else {
                logger.error("fcn type error:fcn must be deploy or instance or complete");
                return null;
            }
            urls.add(url);
        }
        List<Future<SimpleHttpResponse>> futures=httpUtil.multiPost(urls.iterator(), body);
        List<SimpleHttpResponse> responses=new LinkedList<>();
        for (int i=0;i<futures.size();i++) {
            responses.add(futures.get(i).get());
        }
        // for (Future<SimpleHttpResponse> future:futures) {
        //     SimpleHttpResponse response=future.get();
        //     responses.add(response);
        // }
        return Pair.of(localWorkflowResponse, Pair.of(null,responses));
    }
    //模拟执行
    private String workflowSimulatedExecute(Map<String,Object> bodyMap,String fcn,String Oid) throws IOException, InterruptedException, ExecutionException {
        long startTime=System.currentTimeMillis();
        //广播
        Pair<workflowResponse, Pair<String,List<SimpleHttpResponse>>> pair=broadCast(bodyMap, fcn, Oid);
        if (pair==null) {
            return "fcnError or get peers ip error";
        }
        if (pair.getLeft()==null) {
            return pair.getRight().getLeft();
        }
        workflowResponse needToStore=pair.getLeft();
        List<SimpleHttpResponse> responses=pair.getRight().getRight();
        boolean allOk=true;
        int resLength=responses.size()+1;
        String res="success";
        List<String> errorText=new ArrayList<>();
        int count=1;
        for (int i=0;i<responses.size();i++) {
            SimpleHttpResponse response=responses.get(i);
            if (response.getCode()!=200) {
                count--;
                //logger.info("第"+i+"个:"+response.getBodyText());
                errorText.add(response.getBodyText());
                // allOk=false;
                // res=response.getBodyText();
                // logger.error(res);
                // break;
            } else {
                String responseString=response.getBodyText();
                workflowResponse wResponse=(workflowResponse)deCoder.streamToEntity(responseString);
                if (wResponse==null) {
                    //logger.info("第"+i+"个:为空");
                    count--;
                } else {
                    if (wResponse.equals(needToStore)) {
                        count++;
                    }
                     else {
                        count--;
                        if (!wResponse.getReadSetJson().equals(needToStore.getReadSetJson())) {
                            System.out.println(wResponse.getReadSetJson());
                            System.out.println(needToStore.getReadSetJson());
                        }
                        if (!wResponse.getWriteSetJson().equals(needToStore.getWriteSetJson())) {
                            System.out.println(wResponse.getWriteSetJson());
                            System.out.println(needToStore.getWriteSetJson());
                        }
                        //logger.info("第"+i+"个:不同");
                     }
                }
                //responseList.add(wResponse);
                //这里应该检查serviceTask执行的结果,并做出标记哪个
            }
        }
        if (count<=resLength/3) {
            allOk=false;
            res="相同的结果少于等于2/3,errorTextList:"+errorText.toString();
        }
        //将模拟执行结果收集延迟上链
        if (allOk) {
            //测试用
            pair.getLeft().setStartTime(startTime);
            pair.getLeft().setSimulationEndTime(System.currentTimeMillis());
            preExecutionCache.putPreDataToCache(pair.getLeft(), Oid);
        }
        return res;
    }

    //上链加flushdata
    public void preDatasToBlockChainAndFlush() throws InvalidArgumentException, InterruptedException, IOException, ExecutionException {
        if (!preExecutionCache.isNeedFlush()) return;
        //得到上链参数args
        Map<String, workflowResponse> preDatas=preExecutionCache.getPreDatas();
        //做大包数量及交易总数的统计
        int mapLength=preDatas.size();
        packageAndTransactionCount(mapLength);

        ArrayList<String> args=new ArrayList<>();
        StringBuilder oidBuilder=new StringBuilder();
        StringBuilder oidTaskStatusBuilder=new StringBuilder();
        for (workflowResponse preData:preDatas.values()) {
            oidBuilder.append(preData.getOid()).append('|');
            oidTaskStatusBuilder.append(preData.getUploadString()).append('|');
        }
        //处理最后一个'|'
        oidBuilder.deleteCharAt(oidBuilder.length()-1);
        oidTaskStatusBuilder.deleteCharAt(oidTaskStatusBuilder.length()-1);
        

        //将值一次加入args,依次为0-oid,1-fromTask,2-toTask,3-isDeploy,4-deploymentName
        String valueHash=httpUtil.uploadFile(oidTaskStatusBuilder.toString(), channelName);
        args.add(oidBuilder.toString());
        args.add(valueHash);

        //拿到需要执行flush的数据，然后创建一个flushRunnable对象丢给线程池
        upLinkRunnable flushThread=new upLinkRunnable(workflowFabric, channelName, chaincodeName, "flush", args, preDatas,activitiChangeExecutor,wfConfig.isTest());
        flushThreadPool.flush(flushThread);
        logger.info("get UpLink data success");
        preExecutionCache.setLastFlushTime(System.currentTimeMillis());
    }

    //list[0]是oid,list[1]是执行结果
    public List<String> handleWorkflowRequest(Map<String,Object> requestMap,String fcn) throws IOException, InterruptedException, ExecutionException {
        //String fcn=String.valueOf(requestMap.get("fcn"));
        String Oid=null;
        Map<String,Object> bodyMap=null;
        List<String> list=new LinkedList<String>();
        list.add(Oid);
        list.add(null);
        if (fcn.equals(deploy)) {
            String deploymentName=String.valueOf(requestMap.get("deploymentName"));
            if (deploymentName.length()<5||!deploymentName.substring(deploymentName.length()-5, deploymentName.length()).equals(".bpmn")) {
                deploymentName=deploymentName+".bpmn";
            }
            Oid=deploymentName;
            String fileContent=String.valueOf(requestMap.get("fileContent"));

            Map<String,Object> temp=new HashMap<String,Object>() {{
                put("fileContent",fileContent);
            }};
            temp.put("deploymentName",deploymentName);
            bodyMap=temp;
            list.set(0,Oid);
        } else if (fcn.equals(instance)) {
            String deploymentName=String.valueOf(requestMap.get("deploymentName"));
            String processData=String.valueOf(requestMap.get("processData"));
            String businessData=String.valueOf(requestMap.get("businessData"));
            //String staticAllocationTable=String.valueOf(requestMap.get("staticAllocationTable"));
            Map<String,Object> temp=new HashMap<String,Object>() {{
                put("deploymentName",deploymentName);
                put("processData",processData);
                put("businessData",businessData);
                //put("staticAllocationTable",staticAllocationTable);
            }};
            if (requestMap.containsKey("staticAllocationTable")) {
                //没有就是默认所有人都可以执行userTask
                temp.put("staticAllocationTable",String.valueOf(requestMap.get("staticAllocationTable")));
            }
            Oid=deploymentName+"@"+UUID.randomUUID().toString();
            bodyMap=temp;
            list.set(0,Oid); 
        } else if (fcn.equals(complete)) {
            String taskName=String.valueOf(requestMap.get("taskName"));
            String processData=String.valueOf(requestMap.get("processData"));
            String businessData=String.valueOf(requestMap.get("businessData"));
            String user=String.valueOf(requestMap.get("user"));
            Oid=String.valueOf(requestMap.get("Oid"));
            Map<String,Object> temp=new HashMap<String,Object>() {{
                put("taskName",taskName);
                put("processData",processData);
                put("businessData",businessData);
                put("user",user);
            }};
            bodyMap=temp;
            list.set(0,Oid);
        } else {
            list.set(1,"fcn参数错误,必须为deploy,instance或complete");
        }
        if (bodyMap==null||Oid==null) {
            if (bodyMap==null) {
                list.set(1,"转发请求数据初始化失败，检查各字段输入是否正确");
            }
            if (Oid==null) {
                list.set(1,"未检查到Oid参数");
            }
        } else {
            list.set(1,handleWorkflowRequest(bodyMap, fcn, Oid));
        }
        return list;
    }

    private String handleWorkflowRequest(Map<String,Object> bodyMap,String fcn,String Oid) throws IOException, InterruptedException, ExecutionException {
        String simulatedRes=workflowSimulatedExecute(bodyMap, fcn, Oid);
        if (!simulatedRes.equals("success")) {
            return simulatedRes;
            //return "simulateError";
        }
        return "success";        
    }


    public void getBindPackageData() {
        if (!commonUseSimulateCache.isBindNeedFlush()) return;
        Map<String,String> simulateDatas=commonUseSimulateCache.getBindSimulateDatas();
        commonUseSimulateCache.setBindLastFlushTime(System.currentTimeMillis());
        List<String> oids=new ArrayList<>(),wrSets=new ArrayList<>();
        simulateDatas.entrySet().stream().forEach(entry -> {
                                                            oids.add(entry.getKey());
                                                            wrSets.add(entry.getValue());
                                                        });
        ArrayList<List<String>> args=new ArrayList<List<String>>(){{
            add(oids);
            add(wrSets);
        }};
        flushThreadPool.flush(new commonUseUpLinkRunnable(workflowFabric, channelName, chaincodeName, "flush", args, activitiChangeExecutor));
        logger.info("get CommonUse UpLink data success");
    }

    /**
     * 
     * @param oid 动态绑定的实例
     * @param taskName 动态绑定的任务名
     * @param value 动态绑定的属性，userTask是userId,serviceTask是对应service的所有属性
     * @param isUserOrService true代表是userTask,false代表serviceTask
     * @return 成功返回true,失败为false
     */
    public boolean handleDynamicBind(String oid,String taskName,String value) {
        try {
            String json_data=jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{
                put("oid",oid);
                put("taskName",taskName);
                put("value",value);
            }});
            String port=wfConfig.getWorkflowPort();
            List<String> simulateBindUrls=new ArrayList<String>();
            Iterator<String> peerIps= workflowFabric.getPeersIp(channelName).iterator();
            StringBuilder sb=new StringBuilder();
            while (peerIps.hasNext()) {
                sb.append("http://").append(peerIps.next()).append(':')
                  .append(port).append("/wfEngine/bindSimulate");
                simulateBindUrls.add(sb.toString());
                sb.setLength(0);
            }
            List<Future<SimpleHttpResponse>> simulateFutures=httpUtil.multiPost(simulateBindUrls.iterator(), json_data);
            Pair<Boolean,String> simulatePair=compareResponse(simulateFutures);
            //比较背书的结果失败，返回false
            if (!simulatePair.getLeft().booleanValue())  return false;
        
            //打大包
            commonUseSimulateCache.bindSimulateStore(keyCombination.combine(oid,"bind",taskName), simulatePair.getValue());
            return true;
        } catch (Exception e) {
            logger.warn("dynamicBind simulate error:"+e.getMessage());
            throw new RuntimeException(String.format("dynamicBind simulate error:%s"+e.getMessage()));
        }
        //下面是使用分布式锁的逻辑
        // String lockKey="bind-"+oid+"-"+taskName;
        // distributedLock lock=wfEngineContext.getLock();
        // String lockValue=lock.tryLock(lockKey);
        // try {
        //     //等于null则没拿到锁
        //     if (lockValue==null) {
        //         Thread.sleep(5000);
        //         lockValue=lock.tryLock(lockKey);
        //         if (lockValue==null) {
        //             //等了5秒没拿到锁不等了，直接返回
        //             throw new RuntimeException(String.format("dynamicBind error,cause there is another operation about the oid:%s taskName:%s do not complete", oid,taskName));
        //         }
        //     }
        //     //拿到锁了,下面是业务逻辑
        //     String json_data=jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("oid",oid);
        //                                                                                 put("taskName",taskName);
        //                                                                                 put("value",value);}});
        //     String port=wfConfig.getWorkflowPort();
        //     List<String> simulateBindUrls=new LinkedList<String>();
        //     Iterator<String> peerIps= workflowFabric.getPeersIp(channelName).iterator();
        //     StringBuilder sb=new StringBuilder();
        //     while (peerIps.hasNext()) {
        //         sb.append("http://").append(peerIps.next()).append(':')
        //           .append(port).append("/wfEngine/bindSimulate");
        //         simulateBindUrls.add(sb.toString());
        //         sb.setLength(0);
        //     }
        //     List<Future<SimpleHttpResponse>> simulateFutures=httpUtil.multiPost(simulateBindUrls.iterator(), json_data);
        //     Pair<Boolean,String> simulatePair=compareResponse(simulateFutures);
        //     //比较背书的结果失败，返回false
        //     if (!simulatePair.getLeft().booleanValue())  return false;
        //     //上链的数据暂时为key=lockKey,value=模拟执行返回的结果
        //     ArrayList<String> args=new ArrayList<String>(){{
        //         add(lockKey);add(simulatePair.getRight());
        //     }};
        //     CompletableFuture<TransactionEvent> transactionFuture= workflowFabric.workflowInvoke(channelName, chaincodeName, "flush", args);
        //     TransactionEvent transactionEvent= transactionFuture.get();
        //     //交易无效,直接返回false
        //     if (!transactionEvent.isValid()) return false;
        //     List<String> flushBindUrls=new LinkedList<String>();
        //     peerIps=workflowFabric.getPeersIp(channelName).iterator();
        //     while (peerIps.hasNext()) {
        //         sb.append("http://").append(peerIps.next()).append(':')
        //         .append(port).append("/wfEngine/bindFlush");
        //         flushBindUrls.add(sb.toString());
        //         sb.setLength(0);
        //     }
        //     List<Future<SimpleHttpResponse>> flushFutures=httpUtil.multiPost(flushBindUrls.iterator(), simulatePair.getRight());
        //     Pair<Boolean,String> flushPair=compareResponse(flushFutures);
        //     if (!flushPair.getLeft()) return false;
        //     return true;
        // } catch (Exception e) {
        //     logger.warn("dynamicBind error:"+e.getMessage());
        //     return false;
        // } finally {
        //     lock.releaseLock(lockKey, lockValue);
        // }
    }

    private Pair<Boolean,String> compareResponse(List<Future<SimpleHttpResponse>> futures) {
        try {
            int length=futures.size(),count=0;
            String temp="";
            for (int i=0;i<length;i++) {
                SimpleHttpResponse response=futures.get(i).get();
                //System.out.println(response.getBodyText());
                if (count==0) {
                    if (response.getCode()==200) {
                        temp=response.getBodyText();
                        count+=1;
                    } else continue;
                } else {
                    if (response.getCode()==200&&temp.equals(response.getBodyText())) {
                        count++;
                    } else {
                        if (response.getCode()!=200) throw new RuntimeException(String.format("存在节点执行http请求未正常返回, code:%d, body:%s, peer:%s",
                                                                                response.getCode(),response.getBodyText(),workflowFabric.getPeersIp(channelName).get(i)));
                        count-=1;
                    }
                }
            }
            if (count<=length/3) {
                throw new RuntimeException("相同的结果少于等于2/3");
            } else {
                Pair<Boolean,String> pair=Pair.of(true, temp);
                return pair;
            }
        } catch (Exception e) {
            logger.warn("compare responses error:"+ e.getMessage());
            return Pair.of(false, e.getMessage());
        }
    }

    
}
