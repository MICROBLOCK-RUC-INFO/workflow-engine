package com.wq.wfEngine.activiti;


import com.wq.wfEngine.tool.Connect;
import com.alibaba.fastjson.JSON;
import com.wq.wfEngine.cache.cachedData;


import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.Future;



public class ActivitiUtils {
    public static ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    public static RepositoryService repositoryService = processEngine.getRepositoryService();
    public static TaskService taskService = processEngine.getTaskService();
    public static RuntimeService runtimeService = processEngine.getRuntimeService();
    public static HistoryService historyService = processEngine.getHistoryService();




    static public String InitRedis() {
        runtimeService.InitRedis();
        return "ok";
    }

    static public void deleteFromRedis() {
        runtimeService.DeleteFormRedis();
        return;
    }


    

    
    static public ProcessInstance init(String fileName, String fileContent) {
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName(fileName).list();
        for (Deployment d : deploymentList) {
            repositoryService.deleteDeployment(d.getId(), true);// 默认是false true就是级联删除
        }

        Deployment deployment = repositoryService.createDeployment()// 创建Deployment对象
                .addString(fileName, fileContent)
                .name(fileName)
                .deploy();

        RuntimeService runtimeService = processEngine.getRuntimeService();
        String deploymentId = deployment.getId();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
        ProcessInstance processInstance = null;
        if (processDefinition != null) {
            processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        }
        return processInstance;
    }

    static public void clean(String fileName) {
        try {
            List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName(fileName).list();
            for (Deployment d : deploymentList) {
                repositoryService.deleteDeployment(d.getId(), true);// 默认是false true就是级联删除
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
