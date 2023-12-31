package com.wq.wfEngine.activiti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.cache.allocationTable;
import org.activiti.engine.impl.db.redis.workflowContext;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operation;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operation.oType;
import org.activiti.engine.impl.db.workflowClass.cachedResponse;
import org.activiti.engine.impl.db.workflowClass.randomGenerator;
import org.activiti.engine.impl.db.workflowClass.workflowResponse;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.wq.wfEngine.cache.cachedData;
import com.wq.wfEngine.cache.cachedServiceTaskResult;
import com.wq.wfEngine.config.evilNodeConfig;
import com.wq.wfEngine.tool.jsonTransfer;


public class workflowFunction {
    public static ProcessEngine processEngine = ActivitiUtils.processEngine;
    public static RepositoryService repositoryService = ActivitiUtils.repositoryService;
    public static TaskService taskService = ActivitiUtils.taskService;
    public static RuntimeService runtimeService = ActivitiUtils.runtimeService;
    public static HistoryService historyService = ActivitiUtils.historyService;

    //Map<String,Map<String,flowNode>> deployment_flowMap=new HashMap<>();//BPMN model activitiID到flowNode的映射

    
    public static List<Deployment> getAllDeployments() {
        List<Deployment> deployments=repositoryService.createDeploymentQuery().list();
        return deployments;
    }

    //这是因为mybatis数据库连接池隔一段时间会失效，并且连接池不会去做检验，当时改了配置文件仍然没用，就先用了这个方法
    public static void loop() {
        repositoryService.createProcessDefinitionQuery().list();
        runtimeService.createProcessInstanceQuery().list();
    }


    //获取bpmn的矢量图
    public static String getSvgContent(String deploymentName) throws IOException {
        BpmnModel model=cachedData.getBpmnModelByName(deploymentName);
        if (model!=null&&model.getLocationMap().size()>0) {
            DefaultProcessDiagramGenerator ge = new DefaultProcessDiagramGenerator();
            InputStream in = ge.generateDiagram(model,"宋体", "宋体", "宋体");
            byte[] bytes=new byte[in.available()];
            in.read(bytes);
            in.close();
            return new String(bytes);
        } else {
            throw new RuntimeException("bpmnModel 为空");
        }
    }

    public static void deleteDeploymentByName(String deploymentName) {
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName(deploymentName).list();
        if (deploymentList.size()==0) {
            throw new RuntimeException("there is no deployment named "+deploymentName);
        }
        for (Deployment d : deploymentList) {
            repositoryService.deleteDeployment(d.getId(), true);// 默认是false true就是级联删除
        }

        cachedData.cleanDeploymentByName(deploymentName);
    }

    //过滤掉deployments中包含用户任务的，当时是为了智慧城市
    public static List<String> filterUserTask(List<Deployment> deployments) {
        List<String> deploymentWithNoUserTask=new ArrayList<>();
        for (Deployment deployment:deployments) {
            if (!cachedData.haveUserTask(deployment.getName())) {
                deploymentWithNoUserTask.add(deployment.getName());
            }
        }
        return deploymentWithNoUserTask;
    }

    //过滤非dbapi
    public static List<String> filterNoDBAPI(List<Deployment> deployments) {
        List<String> deploymentDBAPI=new ArrayList<>();
        for (Deployment deployment:deployments) {
            if (cachedData.isDBAPI(deployment.getName())) {
                deploymentDBAPI.add(deployment.getName());
            }
        }
        return deploymentDBAPI;
    }


    //智慧城市，拿到serviceTaskInfo
    public static Map<String,Object> getServiceTaskInfo(List<String> deploymentNames) {
        Map<String,Object> serviceTaskInfos=new HashMap<>();
        for (String deploymentName:deploymentNames) {
            serviceTaskInfos.put(deploymentName,cachedData.getServiceTaskInfo(deploymentName));
        }
        return serviceTaskInfos;
    }


    public static String getBytesByDeployment(Deployment deployment) throws IOException {
        InputStream in=repositoryService.getResourceAsStream(deployment.getId(), deployment.getName());
        byte[] bytes=new byte[in.available()];
        in.read(bytes);
        in.close();
        return new String(bytes);
    }

    public static String getBytesByDeploymentName(String deploymentName) throws IOException {
        Deployment deployment=repositoryService.createDeploymentQuery().deploymentName(deploymentName).singleResult();
        if (deployment==null) throw new RuntimeException("there is no deployment with name is "+deploymentName);
        InputStream in=repositoryService.getResourceAsStream(deployment.getId(), deployment.getName());
        byte[] bytes=new byte[in.available()];
        in.read(bytes);
        in.close();
        return new String(bytes);
    }

    public static String deploy(String deploymentName,String fileContent) {
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName(deploymentName).list();

        for (Deployment d : deploymentList) {
            repositoryService.deleteDeployment(d.getId(), true);// 默认是false true就是级联删除
        }

        cachedData.cleanDeploymentByName(deploymentName);

        Deployment deployment = repositoryService.createDeployment()// 创建Deployment对象
        .addString(deploymentName, fileContent)
        .name(deploymentName)
        .deploy();//对于deployment,Name就是他的Oid

        cachedResponse response= runtimeService.getWorkflowResponse(deploymentName);

        //存至缓存，等待flush时更新，对应实例的状态
        cachedData.storeWorkflowResponse(response, deploymentName);

        return response.getEncodeString();
        //return response.getResponseString();
    }


    /**
     * 
     * @param deploymentName 要实例化的bpmn图对应的Deployment的名字
     * @param Oid 实例的唯一oid
     * @param businessData 这个是传入的业务数据
     * @param staticAllocationTable 这个是静态分配表
     * @param serviceTaskResultJson 如果不是这个接受请求的节点为null,否则为接受请求的节点先执行的结果
     * @return 返回的是模拟执行后的结果序列化后用base64加密后的字符串
     */
    public static String instance(String deploymentName,String Oid,String businessData,String staticAllocationTable,String serviceTaskResultJson) {
        if (evilNodeConfig.isEvil()) {
            return randomGenerator.getWorkflowResponseString();
        }
        Map<String,Object> table=null;
        if (staticAllocationTable!=null) {
            table=jsonTransfer.jsonToMap(staticAllocationTable);
            if (table==null) throw new RuntimeException("jsonTransfer error,jsonStr:"+staticAllocationTable);
        }
        workflowContext.getUserTaskBindHandler().staticAllocate(Oid,table);
        Map<String,Object> variables=new HashMap<String,Object>() {
            {
                put("Oid",Oid);
            };
        };
        if (businessData.length()>=2) {
            variables.put("businessData",businessData);
        }
        if (serviceTaskResultJson!=null) {
            variables.put("serviceTaskResultJson",serviceTaskResultJson);
        }
        //拿到需要实例化的第一个流程Id
        String mainProcessId=cachedData.getMainProcessId(deploymentName);
        //实例化
        runtimeService.startProcessInstanceById(mainProcessId,variables);
        //拿到实例化的workflowResponse
        cachedResponse response=runtimeService.getWorkflowResponse(Oid);
        

        //存至缓存，等待flush时更新，对应实例的状态
        cachedData.storeWorkflowResponse(response, Oid);
        //如果是预先执行的设置serviceTaskResultJson
        response.setServiceTaskResultJson(jsonTransfer.serviceTaskResToJsonString(cachedServiceTaskResult.removeServiceTaskRes(Oid)));
        
        //返回给请求方的数据
        return response.getEncodeString(); 
    }


    public static String complete(String Oid,String taskName,String processData,String businessData,String user,String serviceTaskResultJson) {
        if (evilNodeConfig.isEvil()) {
            return randomGenerator.getWorkflowResponseString();
        }
        String taskId=cachedData.getTaskId(Oid, taskName);
        if (taskId==null) {
            throw new RuntimeException("no task named "+taskName+" with oid "+Oid);
        }
        Map<String,Object> variables=jsonTransfer.jsonToMap(processData);
        //传入的变量中放入Oid唯一标识
        variables.put("Oid",Oid);
        variables.put("user",user);
        //如果有businessData则加入variables,因为business传入的是json数据的字符串格式
        //todo:businessData持久化
        if (businessData.length()>=2) {
            variables.put("businessData",businessData);
        }
        if (serviceTaskResultJson!=null) {
            variables.put("serviceTaskResultJson",serviceTaskResultJson);
        }
        //先完成complete
        taskService.complete(taskId, variables,false);

        cachedResponse response=runtimeService.getWorkflowResponse(Oid);

        //存至缓存，等待flush时更新，对应实例的状态
        cachedData.storeWorkflowResponse(response, Oid);
        //如果是预先执行的设置serviceTaskResultJson
        response.setServiceTaskResultJson(jsonTransfer.serviceTaskResToJsonString(cachedServiceTaskResult.removeServiceTaskRes(Oid)));


        return response.getEncodeString();
    }

    /**
     * @apiNote 根据读写集来完成状态更新，这里的读写集用的是json形式，protoBuf不够灵活，如果后期需要可以进行更改
     * @param value 读写集的json字符串,map结构{opType="操作类型",剩下的数据根据opType不一致}，底层有判断逻辑
     */
    public static boolean commonFlush(operation o) {
        return runtimeService.flush(o);
    }

    /**
     * 
     * @param o 模拟执行需要的参数封装的类，deploy,instance,complete还未集成进来
     * @return 返回给用户的数据，一般为读写集
     */
    public static String commonSimulate(operation o) {
        return runtimeService.simulate(o);
    }


    /**
     * @apiNote 这个没有做需要绑定的task是否已经执行完成，可以通过BpmnModel的序列流和信息流的广度遍历来做，但是或许可以有更好的方法
     * @param oid
     * @param taskName
     * @return pair<bool,oType> 有效则返回<true,oType>,无效返回<false,null>
     */
    public static Pair<Boolean,oType> isUserOrServiceTask(String oid,String taskName) {
        BpmnModel model=cachedData.getBpmnModelByName(oid.split("@")[0]);
        Iterator<Process> processes= model.getProcesses().iterator();
        //这个oid的流程已经执行完了
        Map<String,String> nowTasks=cachedData.getNowTasks(oid);
        if (nowTasks==null) return Pair.of(false, null);
        while (processes.hasNext()) {
            Process process=processes.next();
            Iterator<Activity> activities= process.findFlowElementsOfType(Activity.class, true).iterator();
            while (activities.hasNext()) {
                Activity activity=activities.next();
                if (activity instanceof UserTask) {
                    UserTask userTask=(UserTask)activity;
                    if (userTask.getName().equals(taskName)) return Pair.of(true, oType.userTaskBind);
                } else if (activity instanceof ServiceTask) {
                    ServiceTask serviceTask=(ServiceTask)activity;
                    if (serviceTask.getName().equals(taskName)) return Pair.of(true, oType.serviceTaskBind);
                }
            }
        }
        //没有找到对应的task
        return Pair.of(false, null);
    }

    /**
     * @apiNote 这个flush主要用于工作流deploy,instance,complete，因为现阶段的读写集太大，只能以缓存的方式实现flush
     * @param Oids
     */
    public static void flush(String[] Oids) {
        try {
            runtimeService.flushCache(Oids);
            for (String Oid:Oids) {
                cachedData.updateCurrentTaskStatus(Oid);
            } 
        } catch (Exception e) {
            for (StackTraceElement element:e.getStackTrace()) {
                System.out.println(element.toString());
            }
            System.out.println(e.getMessage());
            System.out.println(e.getCause().getMessage());
            throw e;
        }
        //runtimeService.flushCache(Oids);
        // for (String Oid:Oids) {
        //     cachedData.updateCurrentTaskStatus(Oid);
        // }
    }
    /**
     * 服务任务还没有适配，还需要进一步扩展
     */
}
