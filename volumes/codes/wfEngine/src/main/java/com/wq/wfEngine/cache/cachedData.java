package com.wq.wfEngine.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.db.redis.workflowContext;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.tableOperator;
import org.activiti.engine.impl.db.workflowClass.cachedResponse;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.wq.wfEngine.activiti.workflowFunction;
import com.wq.wfEngine.pojo.flowNode;
import com.wq.wfEngine.tool.jsonTransfer;

public class cachedData {
    //结构是oid-taskName-taskId,通过oid,与taskName去取得taskId,tasksevice.complete需要taskid
    private static volatile ConcurrentHashMap<String,ConcurrentHashMap<String,String>> currentTaskNameId =new ConcurrentHashMap<>();
    //结构是oid-cachedResponse,缓存每个oid预执行的结果
    private static volatile ConcurrentHashMap<String,cachedResponse> cachedWorkflowResponse=new ConcurrentHashMap<>();

    //用于缓存一些会经常访问到的数据
    //结构是deploymentName-processDefinitionKey-processDefinitionId,方便快速取得processDefinitionId
    private static volatile ConcurrentHashMap<String,ConcurrentHashMap<String,String>> processKeyToId=new ConcurrentHashMap<>();
    //结构是deploymentName-mainProcessDefinitionId,方便第一次instance时拿到mainProcessDefinitionId
    private static volatile ConcurrentHashMap<String,String> deploymentNameToMainProcessDefinitionId=new ConcurrentHashMap<>();

    //对应每一个实例，每一个task的拥有者,结构是Oid-taskName-user
    private static volatile ConcurrentHashMap<String,ConcurrentHashMap<String,String>> cachedAllocationTable=new ConcurrentHashMap<>();
    //缓存deploymentName对应的bpmnModel,结构是deploymentName-bpmnModel
    //这个主要是用于缓存对应的model,实例化的时候需要mainProcessId,这个需要model拿到
    private static volatile ConcurrentHashMap<String,BpmnModel> cachedBpmnModel=new ConcurrentHashMap<>();
    //缓存由errorboundaryEvent的activiti ID
    private static volatile ConcurrentHashMap<String,Set<String>> hasErrorBoundaryEventMap=new ConcurrentHashMap<>();
    //缓存deployment,智慧城市用
    private static volatile ConcurrentHashMap<String,Deployment> cachedDeployment=new ConcurrentHashMap<>();
    //用于判断是否已经有相同的operation进行了模拟执行，有的话就直接返回
    private static volatile ConcurrentHashMap<String,Long> operationKeyLock=new ConcurrentHashMap<>();
    
    private static final Logger logger=LoggerFactory.getLogger(cachedData.class);


    public static boolean lockOperation(String operationKey) {
        Long currentTimeStamp=System.currentTimeMillis();
        Long preTimeStamp=operationKeyLock.put(operationKey, currentTimeStamp);
        /**null代表之前不存在这个key,如果有则判断，是否为旧值是否为一小时之前的值
         * null很好理解没有key就代表没有正在执行的操作
         * 但是设想如果在某次执行中，在flush环节的remove操作出现问题而导致operationKeyLock中还有未删除的脏数据
         * 传统的想法，用一个线程定时的去清理，但是这样资源开销较大
         * 或者每次先get得到的值与当前时间进行判断,然后在进行put，但是这样失去了concurrentHashMap的原子性操作
         * 会从代码层面上引入线程不安全的情况，就比如现在有个旧的数据，然后同时来了a,b操作，正好a，b对旧值的判断都符合超时的判断
         * 则对于a,b两个操作都会返回true,但是我们的需求是一个返回false,一个返回true,才能实现我们类似于乐观锁的一个想法
         * 这个设计是每次put返回的都是最新的旧值，所以就不存在以上的情况
         */
        if (preTimeStamp==null||currentTimeStamp-preTimeStamp>=3600000L) {
            return true;
        }
        return false;
    }

    public static Map<String,Object> getCurrentTaskByOid(String oid) {
        if (!currentTaskNameId.containsKey(oid)) return new HashMap<>();
        Map<String,String> status=currentTaskNameId.get(oid);
        Map<String,Object> currentTasks=new HashMap<String,Object>(){{put("oid",oid);}};
        List<String> taskList=List.copyOf(status.keySet());
        //保证各个节点之间结果相同
        if (taskList.size()>1) {
            taskList.sort(new Comparator<String>() {
                @Override
                public int compare(String arg0, String arg1) {
                    return arg0.compareTo(arg1);
                }     
            });
        }
        currentTasks.put("tasks",taskList);
        return currentTasks; 
    }

    public static List<Map<String,Object>> getCurrentTaskByDeploymentName(String deploymentName) {
        Iterator<String> oids= currentTaskNameId.keySet().iterator();
        List<Map<String,Object>> list=new ArrayList<>();
        while (oids.hasNext()) {
            String oid=oids.next();
            if(oid.split("@")[0].equals(deploymentName)) list.add(getCurrentTaskByOid(oid));
        }
        return list;
    }

    public static void releaseOperationLock(String operationKey) {
        operationKeyLock.remove(operationKey);
    }

    public static void testAllocationTable() {
        for (String key:cachedAllocationTable.keySet()) {
            System.out.println(key+"::::");
            for (String taskName:cachedAllocationTable.get(key).keySet()) {
                System.out.println(taskName+"="+cachedAllocationTable.get(key).get(taskName));
            }
        }
    }

    public static Map<String,String> getNowTasks(String oid) {
        return currentTaskNameId.get(oid);
    }

    //卢老师组的需求
    private static List<Map<String,Object>> getWorkflowStatusByDeploymentName(String deploymentName) {
        Iterator<Entry<String, ConcurrentHashMap<String, String>>> iterator= currentTaskNameId.entrySet().iterator();
        List<Map<String,Object>> res=new ArrayList<>();
        while (iterator.hasNext()) {
            Entry<String, ConcurrentHashMap<String, String>> temp=iterator.next();
            String oid=temp.getKey();
            if (oid.split("@")[0].equals(deploymentName)) {
                Map<String,Object> map=new HashMap<>();
                map.put("oid",oid);
                List<String> list=new ArrayList<>();
                Iterator<String> taskNames=temp.getValue().keys().asIterator();
                while (taskNames.hasNext()) {
                    list.add(taskNames.next());
                }
                map.put("task",list);
                res.add(map);
            }
        }
        return res;
    }

    /**
     * 
     * @param deploymentName
     * @param isDetail false只返回oid,taskName,true额外返回下一个serviceTask的详细信息
     * @return
     */
    public static List<Map<String,Object>> getWorkflowStatusByDeploymentName(String deploymentName,boolean isDetail) {
        
        if (!isDetail) return getWorkflowStatusByDeploymentName(deploymentName);
        Iterator<Entry<String, ConcurrentHashMap<String, String>>> iterator= currentTaskNameId.entrySet().iterator();
        List<Map<String,Object>> res=new ArrayList<>();
        while (iterator.hasNext()) {
            Entry<String, ConcurrentHashMap<String, String>> temp=iterator.next();
            String oid=temp.getKey();
            if (oid.split("@")[0].equals(deploymentName)) {
                Map<String,Object> map=new HashMap<>();
                map.put("oid",oid);
                map.putAll(getInputByOid(oid));
                List<String> list=new ArrayList<>();
                Iterator<String> taskNames=temp.getValue().keys().asIterator();
                while (taskNames.hasNext()) {
                    list.add(taskNames.next());
                }
                map.put("task",list);
                res.add(map);
            }
        }
        return res;
    }

    public static Map<String, Object> getWorkflowStatusByOid(String oid) {
        ConcurrentHashMap<String,String> temp= currentTaskNameId.get(oid);
        if (temp==null) {
            throw new RuntimeException("no such oid");
        }
        Map<String,Object> res=new HashMap<>();
        res.put("oid",oid);
        List<String> list=new ArrayList<>();
        Iterator<String> taskNames=temp.keys().asIterator();
        while (taskNames.hasNext()) {
            //Map<String,Object> statusMap=new HashMap<>();
            //String taskName=taskNames.next();
            //statusMap.put("task",taskNames);
            //statusMap.put("oid",oid);
            //statusMap.putAll(getInputByOid(oid, taskName));
            list.add(taskNames.next());
            //list.add(statusMap);
        }
        /**
         * 这里有个坑
         * 应该是根据任务名和BpmnModel生成下一个任务信息
         */
        res.put("task",list);
        res.putAll(getInputByOid(oid));
        return res;
    }

    public static Map<String, Object> getInputByModel(BpmnModel bpmnModel,String taskName) {
        HashSet<String> visited=new HashSet<>();
        Stack<FlowElement> stack=new Stack<>();

        List<String> serviceTaskNames=new ArrayList<>();
        List<String> serviceTaskDocumentions=new ArrayList<>();
        List<String> serviceNames=new ArrayList<>();
        List<String> serviceRoutes=new ArrayList<>();
        List<String> serviceGroups=new ArrayList<>();
        List<String> serviceMethods=new ArrayList<>();
        List<String> businessData=new ArrayList<>();

        List<Process> processes =bpmnModel.getProcesses();
        for (Process process:processes) {
            Collection<FlowElement> flowElements= process.getFlowElements();
            Iterator<FlowElement> fIterator= flowElements.iterator();
            while (fIterator.hasNext()) {
                FlowElement flowElement=fIterator.next();
                if (flowElement instanceof UserTask) {
                    UserTask userTask=(UserTask)flowElement;
                    if (taskName.equals(userTask.getName())) {
                        stack.push(flowElement);
                    } 
                }
            }
        }

        while (!stack.isEmpty()) {
            FlowElement flowElement=stack.pop();
            if (flowElement instanceof ServiceTask) {
                ServiceTask serviceTask=(ServiceTask)flowElement;
                serviceTaskNames.add(serviceTask.getName());
                serviceTaskDocumentions.add(serviceTask.getDocumentation());
                List<FieldExtension> fields =serviceTask.getFieldExtensions();
                for (FieldExtension field:fields) {
                    if (field.getFieldName().equals("serviceName")) {
                        serviceNames.add(field.getStringValue());
                    } else if (field.getFieldName().equals("route")) {
                        serviceRoutes.add(field.getStringValue());
                    } else if (field.getFieldName().equals("serviceGroup")) {
                        serviceGroups.add(field.getStringValue());
                    } else if (field.getFieldName().equals("httpMethod")) {
                        serviceMethods.add(field.getStringValue());
                    } else if (field.getFieldName().equals("input")) {
                        String json=field.getStringValue();
                        Map<String,Object> jsonMap=jsonTransfer.jsonToMap(json);
                        for (Object value:jsonMap.values()) {
                            String[] str=String.valueOf(value).split("\\.");
                            if (str[0].equals("init")) {
                                businessData.add(str[1]);
                            }
                        }
                    }
                }
            }
            visited.add(flowElement.getId());
            if (flowElement instanceof FlowNode) {
                List<SequenceFlow> sequenceFlows=((FlowNode)flowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow:sequenceFlows) {
                    if (!visited.contains(sequenceFlow.getTargetRef())) {
                        FlowElement targetElement =bpmnModel.getFlowElement(sequenceFlow.getTargetRef());
                        if (targetElement instanceof UserTask) continue;
                        else stack.push(bpmnModel.getFlowElement(sequenceFlow.getTargetRef()));
                    }
                }
            }
        }
        Map<String,Object> serviceInfo=new HashMap<String,Object>() {{
            put("serviceNames",serviceNames);
            put("serviceRoutes",serviceRoutes);
            put("serviceGroups",serviceGroups);
            put("serviceMethods",serviceMethods);
            put("businessDataNeed",businessData);
            put("serviceTaskNames",serviceTaskNames);
            put("serviceTaskDocumentions",serviceTaskDocumentions);
            put("description","");
        }};
        return serviceInfo;
    }

    //智慧城市用
    public static Map<String, Object> getInputByModel(BpmnModel bpmnModel) {
        HashSet<String> visited=new HashSet<>();
        Stack<FlowElement> stack=new Stack<>();

        List<String> serviceTaskNames=new ArrayList<>();
        List<String> serviceTaskDocumentions=new ArrayList<>();
        List<String> serviceNames=new ArrayList<>();
        List<String> serviceRoutes=new ArrayList<>();
        List<String> serviceGroups=new ArrayList<>();
        List<String> serviceMethods=new ArrayList<>();
        List<String> businessData=new ArrayList<>();

        for (FlowElement element:bpmnModel.getMainProcess().getFlowElements()) {
            if (element instanceof StartEvent) {
                stack.push(element);
                break;
            }
        }
        while (!stack.isEmpty()) {
            FlowElement flowElement=stack.pop();
            if (flowElement instanceof ServiceTask) {
                ServiceTask serviceTask=(ServiceTask)flowElement;
                serviceTaskNames.add(serviceTask.getName());
                serviceTaskDocumentions.add(serviceTask.getDocumentation());
                List<FieldExtension> fields =serviceTask.getFieldExtensions();
                for (FieldExtension field:fields) {
                    if (field.getFieldName().equals("serviceName")) {
                        serviceNames.add(field.getStringValue());
                    } else if (field.getFieldName().equals("route")) {
                        serviceRoutes.add(field.getStringValue());
                    } else if (field.getFieldName().equals("serviceGroup")) {
                        serviceGroups.add(field.getStringValue());
                    } else if (field.getFieldName().equals("httpMethod")) {
                        serviceMethods.add(field.getStringValue());
                    } else if (field.getFieldName().equals("input")) {
                        String json=field.getStringValue();
                        Map<String,Object> jsonMap=jsonTransfer.jsonToMap(json);
                        for (Object value:jsonMap.values()) {
                            String[] str=String.valueOf(value).split("\\.");
                            if (str[0].equals("init")) {
                                businessData.add(str[1]);
                            }
                        }
                    }
                }
            }
            visited.add(flowElement.getId());
            if (flowElement instanceof FlowNode) {
                List<SequenceFlow> sequenceFlows=((FlowNode)flowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow:sequenceFlows) {
                    if (!visited.contains(sequenceFlow.getTargetRef())) {
                        FlowElement targetElement =bpmnModel.getFlowElement(sequenceFlow.getTargetRef());
                        if (targetElement instanceof UserTask) continue;
                        else stack.push(bpmnModel.getFlowElement(sequenceFlow.getTargetRef()));
                    }
                }
            }
        }
        Map<String,Object> serviceInfo=new HashMap<String,Object>() {{
            put("serviceNames",serviceNames);
            put("serviceRoutes",serviceRoutes);
            put("serviceGroups",serviceGroups);
            put("serviceMethods",serviceMethods);
            put("businessDataNeed",businessData);
            put("serviceTaskNames",serviceTaskNames);
            put("serviceTaskDocumentions",serviceTaskDocumentions);
            put("description","");
        }};
        return serviceInfo;
    }

    //智慧城市用
    public static Map<String, Object> getInputByOid(String oid) {
        Set<String> taskNames=currentTaskNameId.get(oid).keySet();
        HashSet<String> visited=new HashSet<>();
        Stack<FlowElement> stack=new Stack<>();

        List<String> serviceTaskNames=new ArrayList<>();
        List<String> serviceTaskDocumentions=new ArrayList<>();
        List<String> serviceNames=new ArrayList<>();
        List<String> serviceRoutes=new ArrayList<>();
        List<String> serviceGroups=new ArrayList<>();
        List<String> serviceMethods=new ArrayList<>();
        List<String> businessData=new ArrayList<>();

        BpmnModel bpmnModel= cachedBpmnModel.get(oid.split("@")[0]);
        List<Process> processes =bpmnModel.getProcesses();
        for (Process process:processes) {
            Collection<FlowElement> flowElements= process.getFlowElements();
            Iterator<FlowElement> fIterator= flowElements.iterator();
            while (fIterator.hasNext()) {
                FlowElement flowElement=fIterator.next();
                if (flowElement instanceof UserTask) {
                    UserTask userTask=(UserTask)flowElement;
                    if (taskNames.contains(userTask.getName())) {
                        stack.push(flowElement);
                    } 
                }
            }
        }
        while (!stack.isEmpty()) {
            FlowElement flowElement=stack.pop();
            if (flowElement instanceof ServiceTask) {
                ServiceTask serviceTask=(ServiceTask)flowElement;
                serviceTaskNames.add(serviceTask.getName());
                serviceTaskDocumentions.add(serviceTask.getDocumentation());
                List<FieldExtension> fields =serviceTask.getFieldExtensions();
                for (FieldExtension field:fields) {
                    if (field.getFieldName().equals("serviceName")) {
                        serviceNames.add(field.getStringValue());
                    } else if (field.getFieldName().equals("route")) {
                        serviceRoutes.add(field.getStringValue());
                    } else if (field.getFieldName().equals("serviceGroup")) {
                        serviceGroups.add(field.getStringValue());
                    } else if (field.getFieldName().equals("httpMethod")) {
                        serviceMethods.add(field.getStringValue());
                    } else if (field.getFieldName().equals("input")) {
                        String json=field.getStringValue();
                        Map<String,Object> jsonMap=jsonTransfer.jsonToMap(json);
                        for (Object value:jsonMap.values()) {
                            String[] str=String.valueOf(value).split("\\.");
                            if (str[0].equals("init")) {
                                businessData.add(str[1]);
                            }
                        }
                    }
                }
            }
            visited.add(flowElement.getId());
            if (flowElement instanceof FlowNode) {
                List<SequenceFlow> sequenceFlows=((FlowNode)flowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow:sequenceFlows) {
                    if (!visited.contains(sequenceFlow.getTargetRef())) {
                        FlowElement targetElement =bpmnModel.getFlowElement(sequenceFlow.getTargetRef());
                        //下一步是userTask停止
                        if (targetElement instanceof UserTask) continue;
                        else stack.push(bpmnModel.getFlowElement(sequenceFlow.getTargetRef()));
                    }
                }
            }
        }
        Map<String,Object> serviceInfo=new HashMap<String,Object>() {{
            put("serviceNames",serviceNames);
            put("serviceRoutes",serviceRoutes);
            put("serviceGroups",serviceGroups);
            put("serviceMethods",serviceMethods);
            put("businessDataNeed",businessData);
            put("serviceTaskNames",serviceTaskNames);
            put("serviceTaskDocumentions",serviceTaskDocumentions);
            put("description","");
        }};
        return serviceInfo;
    }

    //智慧城市用
    public static Map<String, Object> getInputByOid(String oid,String taskName) {
        Set<String> taskNames=currentTaskNameId.get(oid).keySet();
        if (!taskNames.contains(taskName)) {
            throw new RuntimeException("getInputByOid(oid,taskName) error,no taskName:"+taskName);
        }
        HashSet<String> visited=new HashSet<>();
        Stack<FlowElement> stack=new Stack<>();

        List<String> serviceTaskNames=new ArrayList<>();
        List<String> serviceTaskDocumentions=new ArrayList<>();
        List<String> serviceNames=new ArrayList<>();
        List<String> serviceRoutes=new ArrayList<>();
        List<String> serviceGroups=new ArrayList<>();
        List<String> serviceMethods=new ArrayList<>();
        List<String> businessData=new ArrayList<>();

        BpmnModel bpmnModel= cachedBpmnModel.get(oid.split("@")[0]);
        List<Process> processes =bpmnModel.getProcesses();
        for (Process process:processes) {
            Collection<FlowElement> flowElements= process.getFlowElements();
            Iterator<FlowElement> fIterator= flowElements.iterator();
            while (fIterator.hasNext()) {
                FlowElement flowElement=fIterator.next();
                if (flowElement instanceof UserTask) {
                    UserTask userTask=(UserTask)flowElement;
                    if (taskName.equals(userTask.getName())) {
                        stack.push(flowElement);
                    } 
                }
            }
        }
        while (!stack.isEmpty()) {
            FlowElement flowElement=stack.pop();
            if (flowElement instanceof ServiceTask) {
                ServiceTask serviceTask=(ServiceTask)flowElement;
                serviceTaskNames.add(serviceTask.getName());
                serviceTaskDocumentions.add(serviceTask.getDocumentation());
                List<FieldExtension> fields =serviceTask.getFieldExtensions();
                for (FieldExtension field:fields) {
                    if (field.getFieldName().equals("serviceName")) {
                        serviceNames.add(field.getStringValue());
                    } else if (field.getFieldName().equals("route")) {
                        serviceRoutes.add(field.getStringValue());
                    } else if (field.getFieldName().equals("serviceGroup")) {
                        serviceGroups.add(field.getStringValue());
                    } else if (field.getFieldName().equals("httpMethod")) {
                        serviceMethods.add(field.getStringValue());
                    } else if (field.getFieldName().equals("input")) {
                        String json=field.getStringValue();
                        Map<String,Object> jsonMap=jsonTransfer.jsonToMap(json);
                        for (Object value:jsonMap.values()) {
                            String[] str=String.valueOf(value).split("\\.");
                            if (str[0].equals("init")) {
                                businessData.add(str[1]);
                            }
                        }
                    }
                }
            }
            visited.add(flowElement.getId());
            if (flowElement instanceof FlowNode) {
                List<SequenceFlow> sequenceFlows=((FlowNode)flowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow:sequenceFlows) {
                    if (!visited.contains(sequenceFlow.getTargetRef())) {
                        FlowElement targetElement =bpmnModel.getFlowElement(sequenceFlow.getTargetRef());
                        //下一步是userTask停止
                        if (targetElement instanceof UserTask) continue;
                        else stack.push(bpmnModel.getFlowElement(sequenceFlow.getTargetRef()));
                    }
                }
            }
        }
        Map<String,Object> serviceInfo=new HashMap<String,Object>() {{
            put("serviceNames",serviceNames);
            put("serviceRoutes",serviceRoutes);
            put("serviceGroups",serviceGroups);
            put("serviceMethods",serviceMethods);
            put("businessDataNeed",businessData);
            put("serviceTaskNames",serviceTaskNames);
            put("serviceTaskDocumentions",serviceTaskDocumentions);
            put("description","");
        }};
        return serviceInfo;
    }
    

    public static String testErrorBoundaryEvent() {
        return hasErrorBoundaryEventMap.toString();
    }

    public static boolean hasErrorBoundaryEvent(String activityId,String deploymentName) {
        return hasErrorBoundaryEventMap.get(deploymentName).contains(activityId);
    }

    public static BpmnModel getBpmnModelByName(String deploymentName) {
        cacheModelAndDeployment(deploymentName);

        return cachedBpmnModel.get(deploymentName);
    }


    public static Deployment getDeploymentByName(String deploymentName) {
        cacheModelAndDeployment(deploymentName);

        return cachedDeployment.get(deploymentName);
    }

    //这个好像可以跟某些模块合并复用，暂时还未改 2023-6-1
    private static void cacheModelAndDeployment(String deploymentName) {
        if (!cachedDeployment.containsKey(deploymentName)||!cachedBpmnModel.containsKey(deploymentName)) {
            Deployment deployment = workflowFunction.repositoryService.createDeploymentQuery().deploymentName(deploymentName).singleResult();
            if (deployment==null) {
                throw new RuntimeException("there is no deployment named "+deploymentName);
            }
            String deploymentId = deployment.getId();
            List<ProcessDefinition> processDefinitionList = workflowFunction.repositoryService.createProcessDefinitionQuery()
                                                                            .deploymentId(deploymentId).list();
            BpmnModel bpmnModel = workflowFunction.repositoryService.getBpmnModel(processDefinitionList.get(0).getId());
            cachedDeployment.put(deploymentName,deployment);
            cachedBpmnModel.put(deploymentName,bpmnModel);
            if (!hasErrorBoundaryEventMap.containsKey(deploymentName)) {
                Set<String> sets=initHasErrorBoundaryEventMap(bpmnModel);
                hasErrorBoundaryEventMap.put(deploymentName, sets);
            }
        }
    }


    private static Set<String> initHasErrorBoundaryEventMap(BpmnModel bpmnModel) {
        Set<String> sets=new HashSet<>();
        List<Process> processes=bpmnModel.getProcesses();
        for (Process process:processes) {
            Collection<FlowElement> flowElements= process.getFlowElements();
            for (FlowElement flowElement:flowElements) {
                if (flowElement instanceof BoundaryEvent) {
                    BoundaryEvent boundaryEvent=(BoundaryEvent)flowElement;
                    List<EventDefinition> eventDefinitions= boundaryEvent.getEventDefinitions();
                    if (eventDefinitions.size()>0&&(eventDefinitions.get(0) instanceof ErrorEventDefinition)) {
                        sets.add(boundaryEvent.getAttachedToRefId());
                    }
                }
                if (flowElement instanceof SubProcess) {
                    SubProcess subProcess=(SubProcess)flowElement;
                    Collection<FlowElement> subFlowElements= subProcess.getFlowElements();
                    for (FlowElement subFlowElement:subFlowElements) {
                        if (subFlowElement instanceof BoundaryEvent) {
                            BoundaryEvent subBoundaryEvent=(BoundaryEvent)subFlowElement;
                            List<EventDefinition> eventDefinitions=subBoundaryEvent.getEventDefinitions();
                            if (eventDefinitions.size()>0&&(eventDefinitions.get(0) instanceof ErrorEventDefinition)) {
                                sets.add(subBoundaryEvent.getAttachedToRefId());
                            }
                        }
                    }
                }
            }
        }
        return sets;
    }

    public static void cleanDeploymentByName(String deploymentName) {
        processKeyToId.remove(deploymentName);
        deploymentNameToMainProcessDefinitionId.remove(deploymentName);
        cachedBpmnModel.remove(deploymentName);
        cachedDeployment.remove(deploymentName);
        hasErrorBoundaryEventMap.remove(deploymentName);
    }

    public static ConcurrentHashMap<String,String> getAllocationTable(String oid) {
        return cachedAllocationTable.get(oid);
    }


    //存储workflowResponse,用于等待flush时，更新currentTaskNameId
    public static void storeWorkflowResponse(cachedResponse response,String Oid) {
        response.setEnd(isEnd(response.getFromTask().size(),response.getToTasks().size(), Oid));
        cachedWorkflowResponse.put(Oid,response);
    }

    public static String getProcessId(String processKey,String Oid) {
        String deploymentName=Oid.split("@")[0];
        return processKeyToId.get(deploymentName).get(processKey);
    }


    private static boolean isEnd(int fromTaskSize,int toTaskSize,String Oid) {
        int currentSize=currentTaskNameId.get(Oid)==null?0:currentTaskNameId.get(Oid).size();
        if ((currentSize-fromTaskSize+toTaskSize)==0) return true;
        else return false; 
    }

    //卢服务组合-查询服务组合返回信息
    public static Map<String, Object> getServiceTaskInfo(String deploymentName) {

        cacheModelAndDeployment(deploymentName);

        BpmnModel bpmnModel=cachedBpmnModel.get(deploymentName);
        Date deploymentTime=cachedDeployment.get(deploymentName).getDeploymentTime();
        Map<String,Object> serviceInfo=new HashMap<String,Object>() {{
            putAll(getInputByModel(bpmnModel));
            //put("deploymentTime",deploymentTime);
        }};
        return serviceInfo;
    }

    //卢服务组合-过滤有用户任务的bpmn过滤器
    public static boolean haveUserTask(String deploymentName) {

        cacheModelAndDeployment(deploymentName);

        BpmnModel bpmnModel= cachedBpmnModel.get(deploymentName);
        List<Process> processes=bpmnModel.getProcesses();
        for (Process process:processes) {
            Collection<FlowElement> flowElements =process.getFlowElements();
            for (FlowElement f:flowElements) {
                if (f instanceof UserTask) return true; 
            }
        }
        return false;
    }

    public static boolean isDBAPI(String deploymentName) {

        cacheModelAndDeployment(deploymentName);

        BpmnModel bpmnModel= cachedBpmnModel.get(deploymentName);
        List<Process> processes=bpmnModel.getProcesses();
        for (Process process:processes) {
            Collection<FlowElement> flowElements =process.getFlowElements();
            for (FlowElement f:flowElements) {
                if (f instanceof ServiceTask) {
                    ServiceTask serviceTask=(ServiceTask)f;
                    List<FieldExtension> fields =serviceTask.getFieldExtensions();
                    for (FieldExtension field:fields) {
                        if (field.getFieldName().equals("serviceName")) {
                            if (field.getStringValue().equals("DBAPI")) return true;
                            break;
                        }
                    }
                } 
            }
        }
        return false;
    }

    //用于instance和complete时更新对应实例的当前状态
    public static void updateCurrentTaskStatus(String Oid) {
        cachedResponse response=cachedWorkflowResponse.remove(Oid);
        //如果是deploy则无须更新对应状态
        if (response.isDeploy()) return;
        //若toTasks不为空，则需要处理
        //没有对应的Oid,则创建对应的hashMap
        if (response.getToTasks()!=null) {
            if (!currentTaskNameId.containsKey(Oid)) {
                currentTaskNameId.put(Oid,new ConcurrentHashMap<String,String>());
            }
            currentTaskNameId.get(Oid).putAll(response.getToTasks());
            //System.out.println(currentTaskNameId.toString());
        }
        //再进行删除fromTask,需要进行判空
        Map<String,String> fromTask=response.getFromTask();
        if (fromTask!=null) {
            for (String Name:fromTask.keySet()) {
                currentTaskNameId.get(Oid).remove(Name);
            }
        }
        //智慧
        workflowContext.getServiceCache().flush(Oid, true);
        //删除后，判空，如果对应的Oid的Map为空，即认为该Oid对应的实例已执行完成
        if (currentTaskNameId.get(Oid).isEmpty()) {
            workflowContext.getServiceCache().removeCache(Oid);
            currentTaskNameId.remove(Oid);
            //完成同时删除维护的分配表
            tableOperator.cleanTable(Oid);
        }
    }

    @Deprecated
    /**
     * @apiNote 用来验证静态分配表的
     * @param staticAllocationTable 静态分配表
     * @param deploymentName bpmn图deploy时的名字
     * @return 如果分配表中包含bpmn中没有的userTask返回false
     */
    public static boolean verifyStaticAllocation(String staticAllocationTable,String deploymentName) {
        try {
            //解析staticAllocationTable
            Map<String,Object> allocation=jsonTransfer.jsonToMap(staticAllocationTable);
            cacheModelAndDeployment(deploymentName);
            BpmnModel bpmnModel=cachedBpmnModel.get(deploymentName);
            List<Process> processes= bpmnModel.getProcesses();
            for (Process p:processes) {
                Collection<FlowElement> flowElements =p.getFlowElements();
                for (FlowElement f:flowElements) {
                    if (f instanceof UserTask) {
                        UserTask userTask=(UserTask)f;
                        if (!allocation.containsKey(userTask.getName())) {
                            //todo
                            logger.warn("static allocate warning,casue by the taskName "+userTask.getName()+" in staticAllocationTable not exist");
                        } else {
                            allocation.remove(userTask.getName());
                        }
                    }
                    if (f instanceof SubProcess) {
                        SubProcess subProcess=(SubProcess)f;
                        Collection<FlowElement> subFlowElements=subProcess.getFlowElements();
                        for (FlowElement subF:subFlowElements) {
                            if (subF instanceof UserTask) {
                                UserTask subUserTask=(UserTask)subF;
                                if (!allocation.containsKey(subUserTask.getName())) {
                                    logger.warn("static allocate warning,casue by the taskName "+subUserTask.getName()+" in staticAllocationTable not exist");
                                } else {
                                    allocation.remove(subUserTask.getName());
                                }
                            }
                        }
                    }
                }
            }
            return allocation.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @Deprecated
    //instance的时候调用的静态分配，给每一个任务分配一个拥有者(执行者)
    public static void staticAllocate(String staticAllocationTable,String deploymentName,String oid) {
        try {
            //解析staticAllocationTable
            Map<String,Object> allocation=jsonTransfer.jsonToMap(staticAllocationTable);
            //oid不存在，第一次分配
            if (!cachedAllocationTable.containsKey(oid)) {
                cachedAllocationTable.put(oid,new ConcurrentHashMap<String,String>());
            }
            //先看缓存有没有，没有先放入缓存
            cacheModelAndDeployment(deploymentName);

            BpmnModel bpmnModel=cachedBpmnModel.get(deploymentName);
            List<Process> processes= bpmnModel.getProcesses();
            for (Process p:processes) {
                Collection<FlowElement> flowElements =p.getFlowElements();
                for (FlowElement f:flowElements) {
                    if (f instanceof UserTask) {
                        UserTask userTask=(UserTask)f;
                        if (!allocation.containsKey(userTask.getName())) {
                            throw new RuntimeException("static allocate error,casue by the taskName "+userTask.getName()+" in staticAllocationTable not exist");
                            //return "static allocate error,casue by the taskName "+userTask.getName()+" in staticAllocationTable not exist";
                        }
                        cachedAllocationTable.get(oid).put(userTask.getName(),String.valueOf(allocation.get(userTask.getName())));
                    }
                    if (f instanceof SubProcess) {
                        SubProcess subProcess=(SubProcess)f;
                        Collection<FlowElement> subFlowElements=subProcess.getFlowElements();
                        for (FlowElement subF:subFlowElements) {
                            if (subF instanceof UserTask) {
                                UserTask subUserTask=(UserTask)subF;
                                if (!allocation.containsKey(subUserTask.getName())) {
                                    throw new RuntimeException("static allocate error,casue by the taskName "+subUserTask.getName()+" in staticAllocationTable not exist");
                                    //return "static allocate error,casue by the taskName "+userTask.getName()+" in staticAllocationTable not exist";
                                }
                                cachedAllocationTable.get(oid).put(subUserTask.getName(),String.valueOf(allocation.get(subUserTask.getName())));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getMainProcessId(String deploymentName) {
        try {
            if (!deploymentNameToMainProcessDefinitionId.containsKey(deploymentName)) {

                if (!processKeyToId.containsKey(deploymentName)) {
                    processKeyToId.put(deploymentName,new ConcurrentHashMap<String,String>());
                }
    
                Deployment deployment = workflowFunction.repositoryService.createDeploymentQuery().deploymentName(deploymentName).singleResult();
                if (deployment==null) {
                    throw new RuntimeException("there is no deployment named "+deploymentName);
                }
                String deploymentId = deployment.getId();
                List<ProcessDefinition> processDefinitionList = workflowFunction.repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deploymentId).list();
                if (!cachedBpmnModel.containsKey(deploymentName)) {
                    cachedBpmnModel.put(deploymentName,workflowFunction.repositoryService.getBpmnModel(processDefinitionList.get(0).getId()));
                }
                BpmnModel bpmnModel = cachedBpmnModel.get(deploymentName);

                /**这一句看起来没什么鸟用，但是如果使用到了errorBoundaryEvent就很有用
                 * 你不可能每次判断都去数据库把BpmnModel读出来然后解析一遍吧，而且这个是很固定的
                 * 就每个deployment缓存一个 2023-6-1
                 */
                if (!hasErrorBoundaryEventMap.containsKey(deploymentName)) {
                    hasErrorBoundaryEventMap.put(deploymentName,initHasErrorBoundaryEventMap(bpmnModel));
                }

                //拿到processKey
                String mainProcessKey = bpmnModel.getMainProcess().getId();
                for (ProcessDefinition processDefinition:processDefinitionList) {
                    processKeyToId.get(deploymentName).put(processDefinition.getId().split(":")[0],processDefinition.getId());
                    // if (processDefinition.getId().split(":")[0].equals(mainProcessKey)) {
                    //     mainProcessId=processDefinition.getId();
                    //     break;
                    // }
                }
                deploymentNameToMainProcessDefinitionId.put(deploymentName,processKeyToId.get(deploymentName).get(mainProcessKey));
            }
            return deploymentNameToMainProcessDefinitionId.get(deploymentName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getTaskId(String Oid,String taskName) {
        if (currentTaskNameId.containsKey(Oid))
            return currentTaskNameId.get(Oid).get(taskName);
        else return null;
    }

}
