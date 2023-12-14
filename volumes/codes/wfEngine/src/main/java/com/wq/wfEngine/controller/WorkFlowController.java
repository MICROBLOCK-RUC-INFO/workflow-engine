package com.wq.wfEngine.controller;

import com.alibaba.fastjson.JSON;
import com.wq.wfEngine.entity.NetWorkRes;
import com.wq.wfEngine.entity.WfOperation;
import com.wq.wfEngine.entity.WfTask;
import com.wq.wfEngine.activiti.ActivitiUtils;
import com.wq.wfEngine.activiti.RsaEncrypt;
import com.wq.wfEngine.activiti.workflowFunction;
import com.wq.wfEngine.cache.cachedData;
import com.wq.wfEngine.pojo.flowNode;
import com.wq.wfEngine.tool.jsonTransfer;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.db.redis.entityFieldMap;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operation;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operationBuilder;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operation.oType;
import org.activiti.engine.impl.db.workflowClass.proto.entity.simpleEntity.opType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

@Controller
@CrossOrigin
@RequestMapping("wfEngine")
public class WorkFlowController {
    
    ProcessEngine processEngine = ActivitiUtils.processEngine;
    RepositoryService repositoryService = ActivitiUtils.repositoryService;
    TaskService taskService = ActivitiUtils.taskService;
    RuntimeService runtimeService = ActivitiUtils.runtimeService;
    HistoryService historyService = ActivitiUtils.historyService;
    RsaEncrypt rsaEncrypt = new RsaEncrypt();
    HashSet<String> taskStatus = new HashSet<>();
    // 对于请求服务的任务，taskid若存在于set内则可以完成，若不存在则处于不可完成的状态，如服务未执行
    // HashMap<String,HashMap<String,String>> Variables=new HashMap<>();
    Map<String,Map<String,flowNode>> deployment_flowMap=new HashMap<>();//BPMN model activitiID到flowNode的映射
    Map<String,Map<String,Set<String>>> deployment_activityOutFlows=new HashMap<>();//BPMN model activiti的outflows
    Map<String,Map<String,String>> deployment_activityNametoId=new HashMap<>();//对应filenName的BPMNmodel ACTIVITI Name到ID的映射
    Map<String,String> deployment_NametoId=new HashMap<>();//fileName到deploymentId的映射
    Map<String,String> deployment_IdtoMainProcessId=new HashMap<>();//deploymentId到BPMN mainProcessId的映射
    String redisInitStatus=ActivitiUtils.InitRedis();


    @CrossOrigin
    @RequestMapping(value="/startWorkflowService",method={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String startMonitor() throws IOException, InterruptedException {
        java.lang.Process nacos=  Runtime.getRuntime().exec("sh /usr/local/scripts/nacosScripts/nacosStart.sh");
        java.lang.Process monitor=Runtime.getRuntime().exec("sh /usr/local/scripts/wfServiceScripts/wfServiceStart.sh");
        return String.format("nacos start %s ,monitor start %s", nacos.isAlive()?"success":"failed",monitor.isAlive()?"success":"failed");
    }

    /**
     * 
     * @param req {oid:,taskName:,value:}
     * @param isUserOrService userTaskBind 为true serviceTaskBind 为false
     * @return
     */
    @CrossOrigin
    @RequestMapping(value="/bindSimulate",method={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String bindSimulate(@RequestBody String req) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String operationKey=String.valueOf(requestMap.get("oid"))+"-bind-"+String.valueOf(requestMap.get("taskName"));
        if (!cachedData.lockOperation(operationKey)) throw new RuntimeException("this operation has locked");
        Pair<Boolean,oType> verifyRes= workflowFunction.isUserOrServiceTask(String.valueOf(requestMap.get("oid"))
                                                       , String.valueOf(requestMap.get("taskName")));
        if (!verifyRes.getLeft()) throw new RuntimeException(String.format("This instance:%s is not in the process or no this task:%s", 
                                                        String.valueOf(requestMap.get("oid")), String.valueOf(requestMap.get("taskName"))));
        
        operation o=new operationBuilder().setType(verifyRes.getRight())
                                          .setParams(requestMap)
                                          .build();
        return workflowFunction.commonSimulate(o);
    }
    /**
     * 
     * @param req  {oid=,taskName=,oldValue=,newValue=,opType=taskBind}
     * @return
     */
    //动态绑定
    @CrossOrigin
    @RequestMapping(value="/bindFlush",method={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String bindFlush(@RequestBody String req) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String operationKey=String.valueOf(requestMap.get("oid"))+"-bind-"+String.valueOf(requestMap.get("taskName"));
        Pair<Boolean,oType> verifyRes= workflowFunction.isUserOrServiceTask(String.valueOf(requestMap.get("oid"))
                                                       , String.valueOf(requestMap.get("taskName")));
        String taskId=cachedData.getTaskId(String.valueOf(requestMap.get("oid")), String.valueOf(requestMap.get("taskName")));
        if (taskId!=null) requestMap.put("taskId",taskId);
        if (!verifyRes.getLeft()) throw new RuntimeException(String.format("This instance:%s is not in the process or no this task:%s", 
                                                        String.valueOf(requestMap.get("oid")), String.valueOf(requestMap.get("taskName"))));
        operation o=new operationBuilder().setType(verifyRes.getRight())
                                          .setParams(requestMap)
                                          .build();
        boolean res=workflowFunction.commonFlush(o);
        cachedData.releaseOperationLock(operationKey);
        if (res) return "ok";
        else throw new RuntimeException("commonFlush Error");
    }

    @CrossOrigin
    @RequestMapping(value = "/downloadBpmn", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String downloadBpmn() throws IOException {
        List<Deployment> deployments=workflowFunction.getAllDeployments();
        Map<String,Object> deploymentMap=new HashMap<>();
        for (Deployment deployment:deployments) {
            deploymentMap.put(deployment.getName(),workflowFunction.getBytesByDeployment(deployment));
        }
        return jsonTransfer.toJsonString(deploymentMap);
    }

    @CrossOrigin
    @RequestMapping(value = "/hello", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String hello() {
        return "hello";
    }

    @CrossOrigin
    @RequestMapping(value = "/testErrorBoundaryEventMap", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String testErrorBoundaryEvent() {
        return cachedData.testErrorBoundaryEvent();
    }

    //删除bpmn
    @CrossOrigin
    @RequestMapping(value = "/deleteDeployment",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String deleteDeployment(@RequestBody String req) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String deploymentName=String.valueOf(requestMap.get("deploymentName"));
        workflowFunction.deleteDeploymentByName(deploymentName);

        return "ok";
    }

    //智慧城市，过滤了userTask
    @CrossOrigin
    @RequestMapping(value="/queryDeployment")
    @ResponseBody String queryDeployment() {
        List<Deployment> deployments=workflowFunction.getAllDeployments();
        List<String> filteredDeploymentName=deployments.stream().map(deployment -> deployment.getName()).collect(Collectors.toList());
        filteredDeploymentName.sort(new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return arg0.compareTo(arg1);
            }     
        });
        return jsonTransfer.toJsonString(filteredDeploymentName);
    }

    //智慧城市
    @CrossOrigin
    @RequestMapping(value="/queryDeploymentByName/{deploymentName}")
    @ResponseBody String queryDeploymentByName(@PathVariable String deploymentName) throws IOException {
        //Map<String,Object> deploymentInfo=cachedData.getServiceTaskInfo(deploymentName);
        String deploymentContent=workflowFunction.getBytesByDeploymentName(deploymentName);
        //String svgContent=workflowFunction.getSvgContent(deploymentName);
        Map<String,String> deploymentInfo=new HashMap<>();
        deploymentInfo.put(deploymentName,deploymentContent);
        //deploymentInfo.put("svgContent",svgContent);
        return JSON.toJSONString(deploymentInfo);
    }

    @CrossOrigin
    @RequestMapping(value="/queryStatusByOid/{oid}")
    @ResponseBody String queryStatusByOid(@PathVariable String oid) {
        //return JSON.toJSONString(cachedData.getWorkflowStatusByOid(oid));
        return jsonTransfer.toJsonString(cachedData.getCurrentTaskByOid(oid));
    }
    
    @CrossOrigin
    @RequestMapping(value="/queryStatusByDeploymentName/{deploymentName}")
    @ResponseBody String queryStatusByDeploymentName(@PathVariable String deploymentName) throws IOException {
        return jsonTransfer.toJsonString(cachedData.getCurrentTaskByDeploymentName(deploymentName));    
        //return JSON.toJSONString(cachedData.getWorkflowStatusByDeploymentName(deploymentName,false));
    }

    @RequestMapping(value="/deleteInstanceByOid/{oid}")
    @ResponseBody String deleteInstanceByOid(@PathVariable String oid) {
        return null;
    }

    @RequestMapping(value="/deleteInstanceByDeploymentName/{deploymentName}")
    @ResponseBody String deleteInstanceByDeploymentName(@PathVariable String deploymentName) {
        return null;
    }

    @CrossOrigin
    @RequestMapping(value="/testError")
    @ResponseBody String testError() {
        String errorName=null;
        List<Map<String,Object>> passedList=null;
        try {
            List<Deployment> deployments=workflowFunction.getAllDeployments();
            //List<String> filteredDeploymentName=workflowFunction.filterNoDBAPI(deployments);
            List<String> filteredDeploymentName=deployments.stream().map(deployment -> deployment.getName()).collect(Collectors.toList());
            List<Map<String,Object>> res=new ArrayList<Map<String,Object>>();
            passedList=res;
            for (String deploymentName:filteredDeploymentName) {
                errorName=deploymentName; 
                res.addAll(cachedData.getWorkflowStatusByDeploymentName(deploymentName, true));
            }
            return JSON.toJSONString(res);
        } catch(Exception e) {
            return String.format("errorName:%s", errorName)+String.format(" passedList:%s", JSON.toJSONString(passedList))+" error:"+e.getMessage();
        }
    }

    //智慧城市
    @CrossOrigin
    @RequestMapping(value="/queryAllStatus")
    @ResponseBody String queryAllStatus() {
        List<Deployment> deployments=workflowFunction.getAllDeployments();
        //List<String> filteredDeploymentName=workflowFunction.filterNoDBAPI(deployments);
        List<String> filteredDeploymentName=deployments.stream().map(deployment -> deployment.getName()).collect(Collectors.toList());
        List<Map<String,Object>> res=new ArrayList<Map<String,Object>>();
        for (String deploymentName:filteredDeploymentName) 
            res.addAll(cachedData.getWorkflowStatusByDeploymentName(deploymentName, true));
        return JSON.toJSONString(res);
    }

    // 测试
    @RequestMapping(value = "/test", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String test() {
        // Map<Integer,Integer> map=new HashMap<>();
        // for (int i=0;i<20;i++) {
        //     map.put(i,i);
        // }
        // stringRedisTemplate.opsForValue().set("key","rO0ABXNyAD9vcmcuYWN0aXZpdGkuZW5naW5lLmltcGwucGVyc2lzdGVuY2UuZW50aXR5LkV4ZWN1dGlvbkVudGl0eUltcGwAAAAAAAAAAQIAJUkAEmRlYWRMZXR0ZXJKb2JDb3VudEkAFmV2ZW50U3Vic2NyaXB0aW9uQ291bnRJABFpZGVudGl0eUxpbmtDb3VudFoACGlzQWN0aXZlWgAMaXNDb25jdXJyZW50WgAOaXNDb3VudEVuYWJsZWRaAAxpc0V2ZW50U2NvcGVaABNpc011bHRpSW5zdGFuY2VSb290WgAHaXNTY29wZUkACGpvYkNvdW50SQARc3VzcGVuZGVkSm9iQ291bnRJAA9zdXNwZW5zaW9uU3RhdGVJAAl0YXNrQ291bnRJAA10aW1lckpvYkNvdW50SQANdmFyaWFibGVDb3VudEwACmFjdGl2aXR5SWR0ABJMamF2YS9sYW5nL1N0cmluZztMAAxhY3Rpdml0eU5hbWVxAH4AAUwAC2J1c2luZXNzS2V5cQB+AAFMAAxkZWxldGVSZWFzb25xAH4AAUwADGRlcGxveW1lbnRJZHEAfgABTAALZGVzY3JpcHRpb25xAH4AAUwACWV2ZW50TmFtZXEAfgABTAAIbG9ja1RpbWV0ABBMamF2YS91dGlsL0RhdGU7TAAEbmFtZXEAfgABTAAIcGFyZW50SWRxAH4AAUwAF3BhcmVudFByb2Nlc3NJbnN0YW5jZUlkcQB+AAFMABNwcm9jZXNzRGVmaW5pdGlvbklkcQB+AAFMABRwcm9jZXNzRGVmaW5pdGlvbktleXEAfgABTAAVcHJvY2Vzc0RlZmluaXRpb25OYW1lcQB+AAFMABhwcm9jZXNzRGVmaW5pdGlvblZlcnNpb250ABNMamF2YS9sYW5nL0ludGVnZXI7TAARcHJvY2Vzc0luc3RhbmNlSWRxAH4AAUwADnF1ZXJ5VmFyaWFibGVzdAAQTGphdmEvdXRpbC9MaXN0O0wAFXJvb3RQcm9jZXNzSW5zdGFuY2VJZHEAfgABTAAJc3RhcnRUaW1lcQB+AAJMAAtzdGFydFVzZXJJZHEAfgABTAAQc3VwZXJFeGVjdXRpb25JZHEAfgABTAAIdGVuYW50SWRxAH4AAXhyAD1vcmcuYWN0aXZpdGkuZW5naW5lLmltcGwucGVyc2lzdGVuY2UuZW50aXR5LlZhcmlhYmxlU2NvcGVJbXBsAAAAAAAAAAECAAFMABJ1c2VkVmFyaWFibGVzQ2FjaGV0AA9MamF2YS91dGlsL01hcDt4cgA6b3JnLmFjdGl2aXRpLmVuZ2luZS5pbXBsLnBlcnNpc3RlbmNlLmVudGl0eS5BYnN0cmFjdEVudGl0eQAAAAAAAAABAgACSQAIcmV2aXNpb25MAAJpZHEAfgABeHAAAAABdAAHNjcyNzE4NXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAB3CAAAABAAAAAAeAAAAAAAAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAHBwcHBwcHBwdAAdZmlsZTpsb29wLmJwbW4/NTM2MzMwMDA1MjUyNDJwcHQAE1Byb2Nlc3NfMTo2OjY1NTUwMDN0AAlQcm9jZXNzXzFwc3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAZxAH4ACXBxAH4ACXNyAA5qYXZhLnV0aWwuRGF0ZWhqgQFLWXQZAwAAeHB3CAAAAYEfJQvleHBwdAAA");
        // for (int i=0;i<7;i++) {map.putAll(getInputByOid(oid));     
        //     stringRedisTemplate.opsForValue().get("key");
        // }
        
        return entityFieldMap.look();
    }

    @CrossOrigin
    @RequestMapping(value="/queryInputByOid/{oid}")
    @ResponseBody String queryInputByOid(@PathVariable String oid) {
        return JSON.toJSONString(cachedData.getWorkflowStatusByOid(oid));
    }


    @ExceptionHandler(value = RuntimeException.class) 
    public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        //Map<String,Object> map=new HashMap<>();
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @CrossOrigin
    @RequestMapping(value = "/wfDeploy", method = RequestMethod.POST)
    @ResponseBody
    public String wfDeploy(@RequestBody String req) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String deploymentName=String.valueOf(requestMap.get("deploymentName"));
        if (!deploymentName.substring(deploymentName.length()-5, deploymentName.length()).equals(".bpmn")) {
            throw new RuntimeException("deployment must end with .bpmn");
        }
        String fileContent=String.valueOf(requestMap.get("fileContent"));
        //System.out.println("fileName:"+fileName);
        //System.out.println("fileContent:"+fileContent);
        return workflowFunction.deploy(deploymentName, fileContent);
    }

    @CrossOrigin
    @RequestMapping(value = "/wfInstance/{Oid}",method = RequestMethod.POST)
    @ResponseBody
    public String wfInstance(@RequestBody String req,@PathVariable String Oid) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String deploymentName=String.valueOf(requestMap.get("deploymentName"));
        String businessData=String.valueOf(requestMap.get("businessData"));
        String serviceTaskResultJson=null;
        if (requestMap.containsKey("serviceTaskResultJson")) {
            serviceTaskResultJson=String.valueOf(requestMap.get("serviceTaskResultJson"));
        }
        if (requestMap.containsKey("staticAllocationTable")) {
            //静态分配
            String staticAllocationTable=String.valueOf(requestMap.get("staticAllocationTable"));
            return workflowFunction.instance(deploymentName, Oid, businessData,staticAllocationTable,serviceTaskResultJson);   
        }
        //System.out.println(fileName);
        //System.out.println(Oid);
        return workflowFunction.instance(deploymentName, Oid,businessData,null,serviceTaskResultJson);
    }

    @CrossOrigin
    @RequestMapping(value = "/wfComplete/{Oid}",method = RequestMethod.POST)
    @ResponseBody
    public String wfComplete(@RequestBody String req,@PathVariable String Oid) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String taskName=String.valueOf(requestMap.get("taskName"));
        String processData=String.valueOf(requestMap.get("processData"));
        String businessData=String.valueOf(requestMap.get("businessData"));

        if (!requestMap.containsKey("user")) {
            throw new RuntimeException("user must not be null");
        }
        String user=String.valueOf(requestMap.get("user"));
        String serviceTaskResultJson=null;
        if (requestMap.containsKey("serviceTaskResultJson")) {
            serviceTaskResultJson=String.valueOf(requestMap.get("serviceTaskResultJson"));
        }

        return workflowFunction.complete(Oid, taskName, processData, businessData,user,serviceTaskResultJson);
    }


    @CrossOrigin
    @RequestMapping(value = "/flush",method = RequestMethod.POST)
    @ResponseBody
    public String flush(@RequestBody String req) {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String[] Oids=String.valueOf(requestMap.get("oidsString")).split("\\|");
        workflowFunction.flush(Oids);
        return "ok";
    }
}
