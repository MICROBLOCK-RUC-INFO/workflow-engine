package springcloud.springcloudgateway.controller;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

import java.util.*;
import java.util.concurrent.ExecutionException;

import springcloud.springcloudgateway.service.PushService.MonitorService;
import springcloud.springcloudgateway.service.nacosCache.CacheService;
import springcloud.springcloudgateway.service.utils.HttpClientUtil;
import springcloud.springcloudgateway.workflow.simulateCache.*;
import springcloud.springcloudgateway.workflow.wfEngine;
import springcloud.springcloudgateway.workflow.helper.wfConfig;
import springcloud.springcloudgateway.workflow.helper.workflowFabric;
import springcloud.springcloudgateway.workflow.threadExecutor.runableManager;
import springcloud.springcloudgateway.workflow.tools.httpUtil;
import springcloud.springcloudgateway.workflow.tools.jsonTransfer;
import springcloud.springcloudgateway.workflow.tools.keyCombination;
import springcloud.springcloudgateway.workflow.userRequestResult.commonUseResult;
import springcloud.springcloudgateway.workflow.userRequestResult.resForUsers;
import springcloud.springcloudgateway.workflow.wfEngine.queryType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@CrossOrigin(allowCredentials = "true")
@RestController
@RequestMapping("grafana")
public class MonitorController implements DataController {

    @Resource
    MonitorService monitor;

    @Resource
    wfEngine wfEngine;

    @Resource
    wfConfig wfConfig;

    @Resource
    workflowFabric workflowFabric;

    @Resource
    CacheService cacheService;
    //public runableManager runableManager=new runableManager();

    //private boolean loopStatus=false;

    // @Resource
    // ProviderService provider;
    Logger logger = LoggerFactory.getLogger(MonitorController.class);


    // @Resource
    // MonitorChannelConfig monitorChannelConfig;


    @ExceptionHandler(value = RuntimeException.class) 
    public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        //Map<String,Object> map=new HashMap<>();
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @RequestMapping(value="/queryDeployments")
    public String queryDeployments() throws InterruptedException, ExecutionException {
        return wfEngine.query(queryType.deployments);
    }

    //字符串看着相等，但是应该包含无法打印出来的byte,导致判定不通过，待检查
    @RequestMapping(value="/queryDeploymentByName/{deploymentName}")
    public String queryDeploymentByName(@PathVariable String deploymentName) throws InterruptedException, ExecutionException {
        return wfEngine.query(deploymentName, queryType.deploymentByName);
    }

    @RequestMapping(value="/queryStatusByDeploymentName/{deploymentName}")
    public String queryStatusByDeploymentName(@PathVariable String deploymentName) throws InterruptedException, ExecutionException {
        return wfEngine.query(deploymentName, queryType.statusByDeploymentName);
    }

    @RequestMapping(value="/queryStatusByOid/{oid}")
    public String queryStatusByOid(@PathVariable String oid) throws InterruptedException, ExecutionException {
        return wfEngine.query(oid, queryType.statusByOid);
    }

    @RequestMapping(value="/testSmartContract/{fcn}",method = RequestMethod.POST)
    public String testSmartContract(@RequestBody String req,@PathVariable String fcn) {
        return wfEngine.testSmartContract(req, fcn);
    } 

    @RequestMapping(value="/getPackageAndTransationCounts",method = RequestMethod.GET) 
    public String getPackageAndTransationCounts() {
        return JSON.toJSONString(wfEngine.getPackageAndTransactionCount());
    }

    @RequestMapping(value="/getResponseByOid/{oid}",method = RequestMethod.GET)
    public String getResByOid(@PathVariable String oid) {
        return resForUsers.getSuccessRes(oid);
    }

    @RequestMapping(value="/getResByOidForTest/{oid}",method = RequestMethod.GET)
    public String getResByOidForTest(@PathVariable String oid) {
        if (resForUsers.isCompleted(oid)) {
            resForUsers.successExecuteRes.remove(oid);
            return "{\"code\":200}";
        } else {
            return "{\"code\":500}";
        }
    }

    @RequestMapping(value="/getBindRes/{oid}/{taskName}",method=RequestMethod.GET)
    public String getBindRes(@PathVariable String oid,@PathVariable String taskName) {
        return commonUseResult.getResult(keyCombination.combine(oid,"bind",taskName));
    }

    @RequestMapping(value = "/deleteDeploymentByName",method = {RequestMethod.GET,RequestMethod.POST})
    public String deleteDeploymentByName(@RequestBody String req) throws InterruptedException, ExecutionException {
        return wfEngine.deleteDeploymentByName(req);
    }

    @RequestMapping(value="testOids",method = RequestMethod.GET)
    public String testOids() {
        Set<String> oids=resForUsers.oids;
        StringBuilder stringBuilder=new StringBuilder();
        for (String oid:oids) {
            stringBuilder.append(oid).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        oids.clear();
        return stringBuilder.toString();
    }

    @RequestMapping(value="testFlush",method = RequestMethod.POST)
    public String testFlush(@RequestBody String req) throws IOException {
        List<String> ips= workflowFabric.getPeersIp("workflowchannel");
        List<Call> calls=new LinkedList<>();
        for (String ip:ips) {
            calls.add(HttpClientUtil.callHttpRequest("http://"+ip+":8888"+"/wfEngine/flush", req));
        }
        List<Response> responses=new LinkedList<>();
        for (Call call:calls) {
            responses.add(call.execute());
        }
        boolean allOk=true;
        int resLength=responses.size();
        //System.out.println("code:"+responses.get(0).getCode());
        //System.out.println(responses.get(0).getCode()==200);
        if (responses.get(0).code()!=200) {
            allOk=false;
        }
        String responseString=responses.get(0).body().string();
        //System.out.println("responseString:"+responseString);
        //判断模拟执行结果是否一致
        for (int i=1;i<resLength;i++) {
            Response now=responses.get(i);
            if (now.code()==200&&responseString.equals(now.body().string())) {
                continue;
            } else {
                logger.error("flushError");
                allOk=false;
                break;
            }
        }
        if (allOk) return "ok";
        else return "error";
    }

    @RequestMapping(value="testflushOids",method = RequestMethod.GET)
    public String testflushOids() {
        Set<String> oids=resForUsers.flushOidsStrings;
        StringBuilder stringBuilder=new StringBuilder();
        for (String oid:oids) {
            stringBuilder.append(oid).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        oids.clear();
        return stringBuilder.toString();
    }

    @RequestMapping(value="getOids",method = RequestMethod.GET)
    public String getOids() {
        Set<String> oids=resForUsers.successExecuteRes.keySet();
        StringBuilder stringBuilder=new StringBuilder();
        for (String oid:oids) {
            stringBuilder.append(oid).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

        /**
     * @apiNote 根据header的不同转发至不同端口，formData是有文件的
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value="/wfRequest/deploy",method=RequestMethod.POST)
    @ResponseBody
    public void deploy(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
        String contentType= req.getContentType().toLowerCase();
        logger.info(contentType.toString());
        if (contentType.contains(ContentType.APPLICATION_JSON.toString().split(";")[0])) {
            req.getRequestDispatcher("/grafana/wfRequest/deploy/json").forward(req, resp);
        } else if (contentType.contains(ContentType.MULTIPART_FORM_DATA.toString().split(";")[0])) {
            req.getRequestDispatcher("/grafana/wfRequest/deploy/form").forward(req, resp);
        }
    }

    @RequestMapping(value= "/wfRequest/deploy/json",method=RequestMethod.POST)
    public Map<String,Object> deployJson(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {

        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        List<String> list=wfEngine.handleWorkflowRequest(requestMap, wfEngine.deploy);
        String Oid=list.get(0);
        String preRes=list.get(1);
        if (!preRes.equals("success")) {
            if (wfConfig.isTest()) {
                httpUtil.doGet("http://10.77.110.222:9988/informResponse/"+URLEncoder.encode(Oid, "UTF-8")+"/simulateError/"+false);
            }
            return new HashMap<String,Object>() {{put("code",500);put("body",preRes);put("Oid",Oid);put("模拟执行结果",false);}};
        }
        return new HashMap<String,Object>() {{put("code",200);put("body","等待上链,更改状态");put("Oid",Oid);put("模拟执行结果",true);}};

    }

    @RequestMapping(value= "/wfRequest/deploy/form",method=RequestMethod.POST)
    public Map<String,Object> deployForm(HttpServletRequest request) {
        MultipartHttpServletRequest params=((MultipartHttpServletRequest) request);  
        // 获取文件
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        List<MultipartFile> sigs=   ((MultipartHttpServletRequest) request).getFiles("signatures");
        // 获取参数
        String deploymentName=params.getParameter("deploymentName");
        try {
            StringBuilder sb=new StringBuilder();
            InputStreamReader isr = new InputStreamReader(files.get(0).getInputStream(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            int num;
            while ((num=br.read())!=-1) {
                sb.append((char)num);
            }
            isr.close();
            br.close();
            Map<String,Object> requestMap=new HashMap<String,Object>();
            requestMap.put("deploymentName",deploymentName);
            requestMap.put("fileContent",sb.toString());
            
            sb.setLength(0);
            isr = new InputStreamReader(sigs.get(0).getInputStream(), StandardCharsets.UTF_8);
            br = new BufferedReader(isr);
            while ((num=br.read())!=-1) {
                sb.append((char)num);
            }
            isr.close();
            br.close();
            requestMap.put("signatures",sb.toString());

            List<String> list=wfEngine.handleWorkflowRequest(requestMap, wfEngine.deploy);
            String Oid=list.get(0);
            String preRes=list.get(1);
            if (!preRes.equals("success")) {
                if (wfConfig.isTest()) {
                    httpUtil.doGet("http://10.77.110.222:9988/informResponse/"+URLEncoder.encode(Oid, "UTF-8")+"/simulateError/"+false);
                }
                return new HashMap<String,Object>() {{put("code",500);put("body",preRes);put("Oid",Oid);put("模拟执行结果",false);}};
            }
            return new HashMap<String,Object>() {{put("code",200);put("body","等待上链,更改状态");put("Oid",Oid);put("模拟执行结果",true);}};
        } catch (Exception e){
            return new HashMap<String,Object>() {{put("code",500);put("body",e.getMessage());put("Oid",deploymentName);put("模拟执行结果",false);}};
        }
    }

    @RequestMapping(value="/wfRequest/instance",method=RequestMethod.POST)
    public Map<String,Object> instance(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        List<String> list=wfEngine.handleWorkflowRequest(requestMap, wfEngine.instance);
        String Oid=list.get(0);
        String preRes=list.get(1);
        if (!preRes.equals("success")) {
            if (wfConfig.isTest()) {
                httpUtil.doGet("http://10.77.110.222:9988/informResponse/"+URLEncoder.encode(Oid, "UTF-8")+"/simulateError/"+false);
            }
            return new HashMap<String,Object>() {{put("code",500);put("body",preRes);put("Oid",Oid);put("模拟执行结果",false);}};
        }
        return new HashMap<String,Object>() {{put("code",200);put("body","等待上链,更改状态");put("Oid",Oid);put("模拟执行结果",true);}};
    }

    @RequestMapping(value="/wfRequest/complete",method=RequestMethod.POST)
    public Map<String,Object> complete(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        List<String> list=wfEngine.handleWorkflowRequest(requestMap, wfEngine.complete);
        String Oid=list.get(0);
        String preRes=list.get(1);
        if (!preRes.equals("success")) {
            if (wfConfig.isTest()) {
                httpUtil.doGet("http://10.77.110.222:9988/informResponse/"+URLEncoder.encode(Oid, "UTF-8")+"/simulateError/"+false);
            }
            return new HashMap<String,Object>() {{put("code",500);put("body",preRes);put("Oid",Oid);put("模拟执行结果",false);}};
        }
        return new HashMap<String,Object>() {{put("code",200);put("body","等待上链,更改状态");put("Oid",Oid);put("模拟执行结果",true);}};
    
    }


    @RequestMapping(value="/dynamicBind",method = RequestMethod.POST)
    public String dynaminBind(@RequestBody String req) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String oid=String.valueOf(requestMap.get("oid"));
        String taskName=String.valueOf(requestMap.get("taskName"));
        String value=String.valueOf(requestMap.get("value"));
        if (wfEngine.handleDynamicBind(oid, taskName, value)) return "ok";
        else throw new RuntimeException("dynamicBind error");
    }

    // @RequestMapping(value="/serviceDynamicBind",method=RequestMethod.POST)
    // public String serviceDynamicBind(@RequestParam String oid,@RequestParam String taskName,
    //                                 @RequestParam String serviceName,@RequestParam String httpMethod,@RequestParam String route,
    //                                 @RequestParam(required = false,defaultValue = "") String input,@RequestParam(required = false,defaultValue = "") String serviceGroup,
    //                                 @RequestParam(required = false,defaultValue = "") String headers,@RequestParam(required = false,defaultValue = "") String output) {
    //     String serviceInfo=wfEngine.verifyServiceDynamicBindInput(serviceName, httpMethod, route, input, serviceGroup, headers, output);
    //     if(wfEngine.handleDynamicBind(oid, taskName, serviceInfo))
    //         return String.format("success,the info of bind service is:%s,oid is:%s,taskName is:%s", serviceInfo,oid,taskName);
    //     else return "serviceDynamicBind error";
    // }

    @RequestMapping(value="/serviceDynamicBind",method=RequestMethod.POST)
    public String serviceDynamicBind(@RequestBody String req) throws InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        if (!wfEngine.bindVerify(req).getLeft()) return "签名验证失败";
        Map<String,Object> data=jsonTransfer.jsonToMap(String.valueOf(requestMap.get("data")));
        String serviceInfo=wfEngine.verifyServiceDynamicBindInput(String.valueOf(data.get("serviceName")), 
                                String.valueOf(data.get("httpMethod")), String.valueOf(data.get("route")), 
                                data.containsKey("input")?String.valueOf(data.get("input")):"", 
                                data.containsKey("serviceGroup")?String.valueOf(data.get("serviceGroup")):"", 
                                data.containsKey("headers")?String.valueOf(data.get("headers")):"", 
                                data.containsKey("output")?String.valueOf(data.get("output")):"");
        if(wfEngine.handleDynamicBind(String.valueOf(data.get("oid")), String.valueOf(data.get("taskName")), serviceInfo))
            return String.format("success,the info of bind service is:%s,oid is:%s,taskName is:%s", serviceInfo,String.valueOf(data.get("oid")), String.valueOf(data.get("taskName")));
        else return "serviceDynamicBind error";
    }

    @RequestMapping(value="/userDynamicBind",method=RequestMethod.POST)
    public String userDynamicBind(@RequestParam String oid,@RequestParam String taskName,@RequestParam String user) {
        if (wfEngine.handleDynamicBind(oid, taskName, user))
            return String.format("success,the oid:%s ,taskName:%s bind to user:%s",oid,taskName,user);
        else return "userDynamicBind error";
    }

    @RequestMapping(value="/register", method=RequestMethod.POST)
    public String requestMethodName(@RequestParam String name,
                                    @RequestParam(required = false,defaultValue = "") String oldPrivateKey) throws InvalidKeyException, CryptoException, 
                                    IllegalAccessException, InstantiationException, ClassNotFoundException, 
                                    InvalidArgumentException, NoSuchMethodException, InvocationTargetException, 
                                    NoSuchAlgorithmException, SignatureException, InterruptedException, ExecutionException {
        Pair<Boolean,String> res=wfEngine.handleRegister(name, oldPrivateKey);
        if (res.getKey()) {
            Map<String,Object> response=new HashMap<String,Object>(){{
                put("name",name);
                put("privateKey",res.getValue());
            }};
            return jsonTransfer.mapToJsonString(response);
        } else throw new RuntimeException(String.format("注册失败，因为%s", res.getValue()));
    }
    
    @PostMapping(value="/serviceRegister")
    public String serviceRegister(@RequestBody String req) throws InterruptedException, ExecutionException {
        //TODO: process POST request
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String provider=String.valueOf(requestMap.get("provider"));
        String serviceMetaData=String.valueOf(requestMap.get("serviceMetaData"));
        String signature=String.valueOf(requestMap.get("signature"));
        return wfEngine.handleServiceRegisty(provider, serviceMetaData, signature);
    }
    

    @PostMapping(value = "/run")
    public String run(@RequestBody String req, @RequestParam(value = "loadBalance", required = false, defaultValue = "enabled") String loadBalance) throws Exception {
        logger.info("start run");
        String res = monitor.monitor(req, loadBalance);
        return res;
        //return null;
    }

    @PostMapping(value = "/runNoCache")
    public String runNoCache(@RequestBody String req, @RequestParam(value = "loadBalance", required = false, defaultValue = "enabled") String loadBalance) throws Exception {
        logger.info("nocahce start run");
        String res = monitor.monitorNoCache(req, loadBalance);
        return res;
        //return null;
    }

    @PostMapping(value="/updateCache")
    public String updateCache() throws Exception {
        cacheService.updateCache();
        return "ok";
    }

    @GetMapping(value="/getBlockHeight")
    public long getBlockHeight() throws ProposalException, InvalidArgumentException {
        return wfEngine.getBlockHeight();
    }
    

    // @PostMapping(value = "/monitor")
    // public String rerun(@RequestBody String req, @RequestParam(value = "loadBalance", required = false, defaultValue = "enabled") String loadBalance) throws Exception {
    //     String res = monitor.monitor(req, loadBalance);
    //     return res;
    // }

    // @GetMapping(value = "/getId")
    // public String getIdByNameAndHost(@RequestHeader(value = "serviceName") String serviceName, @RequestHeader(value = "ipaddr") String ipaddr) throws Exception {
    //     String uuid = monitor.getUUId(serviceName, ipaddr);
    //     return uuid;
    // }

    // @PostMapping(value = "/monitor/collectData")
    // public String collectMonitorData(@RequestBody String data) {
    //     provider.sendDataToChain(data);
    //     return "success";
    // }

    // @GetMapping(value = "/monitor/isMaster")
    // public Boolean getLocalMonitorNum(@RequestHeader("timestamp") long time) {
    //     return provider.isMasterNode(time);
    // }

    // @GetMapping(value = "/monitor/nodeSum/{nodeSum}")
    // public int setNodeSum(@PathVariable("nodeSum") int nodeSum) {
    //     monitorChannelConfig.setNodeSum(nodeSum);
    //     return monitorChannelConfig.getNodeSum();
    // }

    // @GetMapping(value = "/monitor/nodeSum")
    // public int getNodeSum() {
    //     return monitorChannelConfig.getNodeSum();
    // }

    // @GetMapping(value = "/monitor/timeLimit/{timeLimit}")
    // public int setTimeLimit(@PathVariable("timeLimit") int timeLimit) {
    //     monitorChannelConfig.setTimeLimit(timeLimit);
    //     return monitorChannelConfig.getTimeLimit();
    // }
}