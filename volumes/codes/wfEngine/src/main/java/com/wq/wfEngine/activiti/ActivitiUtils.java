package com.wq.wfEngine.activiti;

import com.wq.wfEngine.pojo.flowNode;
import com.wq.wfEngine.tool.Connect;
import com.alibaba.fastjson.JSON;
import com.wq.wfEngine.cache.cachedData;
import com.wq.wfEngine.entity.WfTask;

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


    static public WfTask taskToWfTask(Task task) {
        WfTask wfTask = new WfTask();
        UserTask userTask = (UserTask) repositoryService.getBpmnModel(task.getProcessDefinitionId())
                .getFlowElement(task.getTaskDefinitionKey());
        List<FormProperty> formProperties = userTask.getFormProperties();
        wfTask.setTaskType("userTask");
        wfTask.setTaskId(userTask.getId());
        if (userTask.getAttributes().size() != 0) {
            Map<String, List<ExtensionAttribute>> attributes = userTask.getAttributes();
            if (attributes.get("isWebService") != null) {
                wfTask.setRequestNode(Boolean.parseBoolean(attributes.get("isWebService").get(0).getValue()));
                setWfTaskWebParam(wfTask, userTask);
            } else {
                wfTask.setRequestNode(false);
            }
            if (wfTask.isRequestNode()) {
                wfTask.setTaskType("webTask");
            } else {
                wfTask.setTaskType("userTask");
            }
        }
        if (formProperties.size() > 0) {
            wfTask.setFormPropertiesList(formProperties);
        }
        wfTask.setFormKey(userTask.getFormKey());
        wfTask.setName(userTask.getName());
        wfTask.setAssignee(userTask.getAssignee() == null ? "" : userTask.getAssignee());
        wfTask.setCandidateGroups(ActivitiUtils.getCandidateGroups(userTask));
        wfTask.setOwner(userTask.getOwner());
        return wfTask;
    }


    static public Future<SimpleHttpResponse> getWebService(String url,
            String variables, String method) throws InterruptedException, ExecutionException {
        Future<SimpleHttpResponse> future = null;
        if (method.equals("GET")) {
            future = Connect.doGet(url);
        } else if (method.equals("POST")) {
            future = Connect.doPost(url, variables);
        }
        return future;
    }

    static public void setWfTaskWebParam(WfTask wfTask, UserTask userTask) {
        if (userTask.getExtensionElements().size() != 0) {
            Map<String, List<ExtensionElement>> extensionEleMap = userTask.getExtensionElements();
            if (extensionEleMap.get("inputOutput") != null) {
                List<ExtensionElement> inputOutputList = extensionEleMap.get("inputOutput");
                for (ExtensionElement inputOutput : inputOutputList) {
                    Map<String, List<ExtensionElement>> inputAndOutputElemMap = inputOutput.getChildElements();
                    List<ExtensionElement> inputElemList = inputAndOutputElemMap.get("inputParameter");
                    List<ExtensionElement> outputElemList = inputAndOutputElemMap.get("outputParameter");
                    for (ExtensionElement inputElem : inputElemList) {
                        String attrName = inputElem.getAttributes().get("name").get(0).getValue();
                        String attrValue = inputElem.getElementText();
                        switch (attrName) {
                            case "serviceOrg":
                                wfTask.setServiceOrg(attrValue);
                                break;
                            case "serviceName":
                                wfTask.setServiceName(attrValue);
                                break;
                            case "serviceInterface":
                                wfTask.setServiceInterface(attrValue);
                                break;
                            case "serviceRole":
                                wfTask.setServiceRole(attrValue);
                                break;
                            case "httpMethod":
                                wfTask.setHttpMethod(attrValue);
                                break;
                            case "requestParamList":
                                wfTask.setRequestParamList(attrValue);
                                break;
                            case "responseParam":
                                wfTask.setResponseParamList(attrValue);
                                break;
                            case "serviceType":
                                wfTask.setServiceType(attrValue);
                        }
                    }
                }
            }
        }
    }

    static public String getCandidateGroups(UserTask userTask) {
        String ret = "";
        if (userTask.getCandidateGroups().size() == 0) {
            return ret;
        } else {
            ret = String.join(",", userTask.getCandidateGroups());
            return ret;
        }
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

    static public List<WfTask> getWfTaskList(String processInstanceId) {
        List<Task> task = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        List<WfTask> data = new ArrayList<>();
        for (Task t : task) {
            data.add(ActivitiUtils.taskToWfTask(t));
        }
        return data;
    }

    static public Map<String, Object> setVariables(List<FormProperty> formValList) {
        Map<String, Object> variables = new HashMap<>();
        if (formValList == null) {
            return variables;
        }
        for (FormProperty formPro : formValList) {
            if (formPro.getType().equals("long")) {
                variables.put(formPro.getName(), Long.parseLong(formPro.getVariable()));
            } else {
                variables.put(formPro.getName(), formPro.getVariable());
            }
        }
        return variables;
    }

    // public String getNextTaskName(Task task) {
    // BpmnModel bpmnModel =
    // repositoryService.getBpmnModel(task.getProcessDefinitionId());

    // }

    static public Map<String, Object> addVariables(Map<String, Object> formValMap, Map<String, Object> variables) {
        for (String key : formValMap.keySet()) {
            variables.put(key, formValMap.get(key));
        }
        return variables;
    }

    static public void unifyAssignee(List<WfTask> nextWfTaskList, List<WfTask> wfTaskList) {
        String pattern = "(>)(.+)";
        Pattern r = Pattern.compile(pattern);
        for (WfTask nextWfTask : nextWfTaskList) {
            String assignee = nextWfTask.getAssignee();
            Matcher m = r.matcher(assignee);
            if (m.matches()) {
                String operationName = m.group(2);
                for (WfTask wfTask : wfTaskList) {
                    if (wfTask.getName().equals(operationName)) {
                        nextWfTask.setAssignee(wfTask.getAssignee());
                        break;
                    }
                }
            }
        }
    }

    static public Map<String,String> initDeployment_activityNametoId(String deploymentId) {
        List<ProcessDefinition> processDefinitions =repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list();
        Map<String,String> activity_NametoId=new HashMap<>();//activiti name到id的一个映射
        ProcessDefinition processDefinition=processDefinitions.get(0);
        BpmnModel model= repositoryService.getBpmnModel(processDefinition.getId());
        List<Process> processList=model.getProcesses();
        for (Process process:processList) {
            Collection<FlowElement> flowElements= process.getFlowElements();
            for (FlowElement flowElement:flowElements) {
                if (flowElement instanceof Activity) {
                    activity_NametoId.put(flowElement.getName(),flowElement.getId());
                }
            }
        }
        return activity_NametoId;
    }


    static public Map<String,Set<String>> initDeployment_activityOutFlows(String deploymentId) {
        List<ProcessDefinition> processDefinitions =repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list();
        Map<String,Set<String>> activitiNextFlow=new HashMap<>();//是一个key为activityId ,value为该activity的outflow的hashset
        ProcessDefinition processDefinition=processDefinitions.get(0);
        BpmnModel model= repositoryService.getBpmnModel(processDefinition.getId());
        Map<String,MessageFlow> messageFlowMap=model.getMessageFlows();
        List<Process> processList=model.getProcesses();
        for (Process process:processList) {
            Map<String,FlowElement> flowElementMap=process.getFlowElementMap();
            Collection<FlowElement> flowElements= process.getFlowElements();
            for (String messageFlowid:messageFlowMap.keySet()) {
                String sourceNode=messageFlowMap.get(messageFlowid).getSourceRef();
                if (flowElementMap.get(sourceNode) instanceof Activity) {
                    if (activitiNextFlow.containsKey(sourceNode)) {
                        activitiNextFlow.get(sourceNode).add(messageFlowid);
                    } else {
                        Set<String> temp=new HashSet<>();
                        temp.add(messageFlowid);
                        activitiNextFlow.put(sourceNode,temp);
                    }
                }
            }//将信息流全部加入flowmap中（表达式的key,value和下一个node）。如果source是activity并将source->messageflow加入activitinextflow
            for (FlowElement flowElement:flowElements) {
                if (flowElement instanceof SequenceFlow) {
                    SequenceFlow sequenceFlow= (SequenceFlow) flowElement;
                    String sourceNode=sequenceFlow.getSourceRef();
                    if (flowElementMap.get(sourceNode) instanceof Activity) {
                        if (activitiNextFlow.containsKey(sourceNode)) {
                            activitiNextFlow.get(sourceNode).add(sequenceFlow.getId());
                        } else {
                            Set<String> temp=new HashSet<>();
                            temp.add(sequenceFlow.getId());
                            activitiNextFlow.put(sourceNode,temp);
                        }
                    }//更新sequenceflow的activititinextflow;
                }
            }
        }
        return activitiNextFlow;
    }


    static public Map<String,flowNode> initDeployment_FlowMap(String deploymentId) {
        List<ProcessDefinition> processDefinitions =repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list();
        Map<String,flowNode> FlowMap=new HashMap<>();//flowmap中key是flowid,value是一个包括表达式key,value和下一个nodeid的结构体
        ProcessDefinition processDefinition=processDefinitions.get(0);
        BpmnModel model= repositoryService.getBpmnModel(processDefinition.getId());
        Map<String,MessageFlow> messageFlowMap=model.getMessageFlows();
        List<Process> processList=model.getProcesses();
        for (Process process:processList) {
            Collection<FlowElement> flowElements= process.getFlowElements();
            for (String messageFlowid:messageFlowMap.keySet()) {
                flowNode flownode=new flowNode();
                flownode.setNextNode(messageFlowMap.get(messageFlowid).getTargetRef());
                FlowMap.put(messageFlowid,flownode);
            }//将信息流全部加入flowmap中（表达式的key,value和下一个node）。如果source是activity并将source->messageflow加入activitinextflow
            for (FlowElement flowElement:flowElements) {
                if (flowElement instanceof SequenceFlow) {
                    flowNode flownode=new flowNode();
                    SequenceFlow sequenceFlow= (SequenceFlow) flowElement;
                    flownode.setNextNode(sequenceFlow.getTargetRef());//
                    if (sequenceFlow.getConditionExpression()!=null) {
                        String expression=sequenceFlow.getConditionExpression();
                        String formatExpression= expression.substring(2, expression.length()-1);
                        String[] key_value=formatExpression.split("==");
                        String key=key_value[0];
                        String value=key_value[1].substring(1, key_value[1].length()-1);
                        flownode.setKey(key);
                        flownode.setValue(value);
                    }
                    FlowMap.put(flowElement.getId(),flownode);//将序列流加入序列流的map中
                }
            }
        }
        return FlowMap;
    }

    static public Map<String, String> getTaskMessageTarget(Task t) {
        Map<String, String> messageStrMap = new HashMap<>();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(t.getProcessDefinitionId());
        Map<String, MessageFlow> messageFlowMap = bpmnModel.getMessageFlows();
        for (MessageFlow messageFlow : messageFlowMap.values()) {
            if (messageFlow.getSourceRef().equals(t.getTaskDefinitionKey())) {
                String targetId = messageFlow.getTargetRef();
                FlowElement targetElement = bpmnModel.getFlowElement(targetId);
                if (targetElement instanceof StartEvent) {
                    StartEvent targetEvent = (StartEvent) targetElement;
                    List<EventDefinition> eventDefinitionList = targetEvent.getEventDefinitions();
                    for (EventDefinition eventDefinition : eventDefinitionList) {
                        if (eventDefinition instanceof MessageEventDefinition) {
                            Process parentProcess = (Process) targetEvent.getParentContainer();
                            messageStrMap.put(parentProcess.getId(), "StartEvent");
                        }
                    }
                } else if (targetElement instanceof IntermediateCatchEvent) {
                    IntermediateCatchEvent targetEvent = (IntermediateCatchEvent) targetElement;
                    List<EventDefinition> eventDefinitionList = targetEvent.getEventDefinitions();
                    for (EventDefinition eventDefinition : eventDefinitionList) {
                        if (eventDefinition instanceof MessageEventDefinition) {
                            MessageEventDefinition messageEventDefinition = (MessageEventDefinition) eventDefinition;
                            Message message = bpmnModel.getMessage(messageEventDefinition.getMessageRef());
                            if (message == null) {
                                messageStrMap.put(messageEventDefinition.getMessageRef(), "IntermediateCatchEvent");
                            } else {
                                String messageName = message.getName();
                                messageStrMap.put(messageName, "IntermediateCatchEvent");
                            }
                            //

                        }
                    }
                }
            }
        }
        return messageStrMap;
    }

    static public void setSelectServiceParams(WfTask wfTask, String selectServiceParams) {

    }
}
