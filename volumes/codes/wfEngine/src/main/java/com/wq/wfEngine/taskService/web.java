package com.wq.wfEngine.taskService;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.wq.wfEngine.WfEngineApplication;
import com.wq.wfEngine.activiti.ActivitiUtils;
import com.wq.wfEngine.cache.cachedData;
import com.wq.wfEngine.cache.cachedServiceTaskResult;
//import com.wq.wfEngine.taskService.locks.unReleaseUrls;
import com.wq.wfEngine.tool.Connect;
import com.wq.wfEngine.tool.jsonTransfer;
import com.wq.wfEngine.tool.serviceComposition.inputSelector.jsonPathInputSelector;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.cache.oidEvents;
import org.activiti.engine.impl.db.redis.workflowContext;
import org.activiti.engine.impl.db.workflowClass.serviceTaskRes;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class web implements JavaDelegate {
    //使用监控链调服务的服务名
    private Expression serviceName;
    //调用服务使用的方法
    private Expression httpMethod;
    //调用服务的路径route
    private Expression route;
    //用于指定输入
    private Expression input;
    //s-group
    private Expression serviceGroup;
    //最后一个serviceTask，用于指定businessData输出
    private Expression output;
    //用于将服务调用数据转换为流程数据用于流程流转
    private Expression processVariable;
    //对调用服务的输入值做修饰，额外的操作
    private Expression modify;
    //指定header
    private Expression headers;
    //用于跨组织
    private Expression nextProcess;
    private Expression nextMessage;
    private final Logger logger=LoggerFactory.getLogger(web.class); 

    static class arguments{
        String serviceName;
        String serviceGroup;
        String httpMethod;
        String route;
        String input;
        String output;
        String processVariable;
        String modify;
        String headers;
        String nextProcess;
        String nextMessage;
        
        public boolean isService() {
            return serviceName!=null;
        }
    }

    public Map<String,Object> getHttpData(arguments args,DelegateExecution execution,
                jsonPathInputSelector selector,Map<String,Object> dataPool) {
        CommandContext commandContext=Context.getCommandContext();
        String businessData=commandContext.getBusinessData();
        String lastResponse=commandContext.getLastResponse();
        String body="{}";
        //提取input
        if (args.input==null||args.input.equals("default")) {
            if (lastResponse==null) {
                body=businessData;
            } else {
                body=lastResponse;
            }
        } else if (args.input.equals("business")) {
            body=businessData;
        } else {
            body=selector.select(dataPool,args.input);
        }
        //智慧城市的modify
        //body=modify(body,args.modify)
        Map<String,Object> postMap=new HashMap<>();
        postMap.put("s-consumerName",commandContext.getOid()+"@"+execution.getCurrentFlowElement().getName());
        postMap.put("s-serviceName",args.serviceName);
        postMap.put("headers",args.headers==null?"{}":args.headers);
        postMap.put("s-url",args.route);
        postMap.put("s-method",args.httpMethod);
        postMap.put("body",body);
        if (args.serviceGroup!=null) {
            postMap.put("s-group",args.serviceGroup);
        }
        return postMap;
    }

    public void handleResponse(SimpleHttpResponse response,DelegateExecution execution) {
        String activitiId=execution.getCurrentActivityId();
        String serviceTaskName=execution.getCurrentFlowElement().getName();
        CommandContext commandContext=Context.getCommandContext();
        String oid=commandContext.getOid();
        if (response.getCode()!=200) {
            if (cachedData.hasErrorBoundaryEvent(activitiId, oid.split("@")[0])) {
                serviceTaskRes result=new serviceTaskRes();
                result.setStatus(false);
                result.setBody(response.getBodyText());
                cachedServiceTaskResult.getServiceTaskRes(oid).put(serviceTaskName,result);
                throw new BpmnError("httpError");
            } else {
                throw new RuntimeException("服务调用出错,返回结果:"+response.getBodyText());
            }
            //throw new BpmnError("httpError",response.getBodyText());
        } else {
            if (response.getBodyText().equals("try lock failed")) throw new RuntimeException("加锁失败"); 
            serviceTaskRes result=new serviceTaskRes();
            result.setStatus(true);
            result.setBody(response.getBodyText());
            cachedServiceTaskResult.getServiceTaskRes(oid).put(serviceTaskName,result);
        }
        commandContext.setLastResponse((response.getBodyText()));
        commandContext.storeOutput(serviceTaskName, jsonTransfer.jsonToMap(response.getBodyText()));
    }

    public void handleNext(arguments args,DelegateExecution execution) {
        RuntimeService runtimeService = ActivitiUtils.runtimeService;
        String oid=Context.getCommandContext().getOid();
        if (args.nextMessage!=null) {
            //表明信息流流向intermidateMessageCatchEvent
            String messageName=args.nextMessage;
            runtimeService.messageEventReceived(messageName, oidEvents.getEventExecutionIdByOidAndName(oid,messageName),execution.getVariables());
        }
        if (args.nextProcess!=null) {
            //表明信息流流向startMessageEvent
            String processDefinitionKey=args.nextProcess;
            runtimeService.startProcessInstanceById(cachedData.getProcessId(processDefinitionKey,oid),execution.getVariables());
        }
    }

    public void argsCheck(arguments args) {
        if (args.serviceName!=null) {
            if (args.route==null) throw new RuntimeException("serviceName不为空的情况下,route不能为空");
            if (args.httpMethod==null) throw new RuntimeException("serviceName不为空的情况下,httpMethod不能为空");
            //if (args.input==null||args.input.equals("default")||args.input.equals("business")jsonTransfer.jsonToMap(args.input)==null) throw new Run
        }
    }

    public arguments getArgs(DelegateExecution execution) {
        arguments args=new arguments();
        String oid=Context.getCommandContext().getOid();
        String serviceTaskName=execution.getCurrentFlowElement().getName();
        String serviceInfo=workflowContext.getServiceTaskBindHandler().getServiceInfo(oid, serviceTaskName);
        Map<String,Object> serviceInfoMap=serviceInfo==null?null:jsonTransfer.jsonToMap(serviceInfo);
        if (serviceInfoMap!=null) {
            args.serviceName=serviceInfoMap.containsKey("serviceName")?String.valueOf(serviceInfoMap.get("serviceName")):null;
            args.serviceGroup=serviceInfoMap.containsKey("serviceGroup")?String.valueOf(serviceInfoMap.get("serviceGroup")):null;
            args.httpMethod=serviceInfoMap.containsKey("httpMethod")?String.valueOf(serviceInfoMap.get("httpMethod")):null;
            args.route=serviceInfoMap.containsKey("route")?String.valueOf(serviceInfoMap.get("route")):null;
            args.input=serviceInfoMap.containsKey("input")?String.valueOf(serviceInfoMap.get("input")):null;
            args.output=serviceInfoMap.containsKey("output")?String.valueOf(serviceInfoMap.get("output")):null;
            args.processVariable=serviceInfoMap.containsKey("processVariable")?String.valueOf(serviceInfoMap.get("processVariable")):null;
            args.modify=serviceInfoMap.containsKey("modify")?String.valueOf(serviceInfoMap.get("modify")):null;
            args.headers=serviceInfoMap.containsKey("headers")?String.valueOf(serviceInfoMap.get("headers")):null;
        } else {
            args.serviceName=serviceName==null?null:serviceName.getValue(execution).toString();
            args.serviceGroup=serviceGroup==null?null:serviceGroup.getValue(execution).toString();
            args.httpMethod=httpMethod==null?null:httpMethod.getValue(execution).toString();
            args.route=route==null?null:route.getValue(execution).toString();
            args.input=input==null?null:input.getValue(execution).toString();
            args.output=output==null?null:output.getValue(execution).toString();
            args.processVariable=processVariable==null?null:processVariable.getValue(execution).toString();
            args.modify=modify==null?null:modify.getValue(execution).toString();
            args.headers=headers==null?null:headers.getValue(execution).toString();
        }
        args.nextMessage=nextMessage==null?null:nextMessage.getValue(execution).toString();
        args.nextProcess=nextProcess==null?null:nextProcess.getValue(execution).toString();
        return args;
    }

    private String modify(String body,String modify) {
        if (modify!=null) {
            Map<String,Object> bodyMap=jsonTransfer.jsonToMap(body);
            Map<String,Object> modifyMap=jsonTransfer.jsonToMap(modify);
            StringBuilder valueBuilder=new StringBuilder();
            for (String modifyKey:modifyMap.keySet()) {
                if (!bodyMap.containsKey(modifyKey)) {
                    throw new RuntimeException("对key为"+modifyKey+"的值加工失败，因为没有对应的key");
                }
                String value=String.valueOf(modifyMap.get(modifyKey));
                if (!value.contains("${}")) {
                    throw new RuntimeException(value+"格式错误，因为没有找到${}");
                }
                int left=value.indexOf("${}");
                valueBuilder.append(value.substring(0, left)).append(String.valueOf(bodyMap.get(modifyKey)))
                            .append(value.substring(left+3));
                bodyMap.put(modifyKey,valueBuilder.toString());
                valueBuilder.setLength(0);
            }
            body=jsonTransfer.toJsonString(bodyMap);
        }
        return body;
    }

    public void execute (DelegateExecution execution) {
        try {
            CommandContext commandContext=Context.getCommandContext();
            String oid=commandContext.getOid();
            arguments args=getArgs(execution);
            argsCheck(args);
            if (args.isService()) {
                Map<String,serviceTaskRes> serviceTaskResMap= commandContext.getServiceTaskRes();                                
                if (serviceTaskResMap==null) {
                    if (!cachedServiceTaskResult.isCreateMap(oid)) {
                        cachedServiceTaskResult.addServiceTaskRes(new HashMap<String,serviceTaskRes>(), oid);    
                    }
                    String monitorUrl="http://127.0.0.1:8999/grafana/run?loadBalance=enabled";
                    jsonPathInputSelector jsonPathInputSelector=WfEngineApplication.context.getBeanFactory().getBean(jsonPathInputSelector.class);
                    Map<String,Object> dataPool=commandContext.getCachedOutput();
                    Map<String,Object> postMap=getHttpData(args, execution, jsonPathInputSelector,dataPool);
                    System.out.println(String.format("service:%s调用服务%s",execution.getCurrentFlowElement().getName(),jsonTransfer.toJsonString(postMap)));
                    Future<SimpleHttpResponse> future = Connect.doPost(monitorUrl, jsonTransfer.toJsonString(postMap));
                    SimpleHttpResponse response = future.get();
                    handleResponse(response, execution);
                    if (args.output!=null) {
                        String outputString=jsonPathInputSelector.select(dataPool, args.output);
                        commandContext.setLastResponse(outputString);
                    }
                    if (args.processVariable!=null) {
                        Map<String,Object> variables=jsonTransfer.jsonToMap(jsonPathInputSelector.select(dataPool, args.processVariable));
                        execution.setVariables(variables);
                    }
                } else {
                    String serviceTaskName=execution.getCurrentFlowElement().getName();
                    serviceTaskRes serviceTaskRes= serviceTaskResMap.get(serviceTaskName);
                    if (serviceTaskRes==null) {
                        throw new RuntimeException("未检测到"+serviceTaskName+"的服务任务的执行结果");
                    }
                    if (!serviceTaskRes.isStatus()) {
                        throw new BpmnError("httpError");
                    }
                }
            }
            handleNext(args, execution);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof BpmnError) throw new BpmnError("httpError");
            else throw new RuntimeException("serviceTask:"+execution.getCurrentFlowElement().getName()+"执行失败,失败原因:"+e.getMessage());            
        } 
    }
    // public void execute(DelegateExecution execution) {
    //     jsonPathInputSelector jsonPathInputSelector=WfEngineApplication.context.getBeanFactory().getBean(jsonPathInputSelector.class);
    //     try {
    //         //拿变量
    //         String out=null;
    //         CommandContext commandContext= Context.getCommandContext();
    //         String oid=commandContext.getOid();
    //         String activityId=execution.getCurrentActivityId();
    //         String serviceTaskName=execution.getCurrentFlowElement().getName();
    //         String serviceInfo=workflowContext.getServiceTaskBindHandler().getServiceInfo(oid, serviceTaskName);
    //         Map<String,Object> serviceInfoMap=serviceInfo==null?null:jsonTransfer.jsonToMap(serviceInfo);

    //         System.out.println(String.format("web serviceInfoMap %s", jsonTransfer.toJsonString(serviceInfoMap)));
    //         //logger.info("web serviceInfoMap {}",serviceInfoMap);

    //         //serviceName存在，且为当前节点执行
    //         if (serviceName!=null||serviceInfoMap!=null) {
    //             Map<String,serviceTaskRes> serviceTaskResMap= commandContext.getServiceTaskRes();
    //             if (serviceTaskResMap==null) {
    //                 if (!cachedServiceTaskResult.isCreateMap(oid)) {
    //                     cachedServiceTaskResult.addServiceTaskRes(new HashMap<String,serviceTaskRes>(), oid);    
    //                 }
    //                 String businessData=commandContext.getBusinessData();
    //                 String lastResponse=commandContext.getLastResponse();


    //                 //轮询获得monitorIp，底层是return monitorNumber++
    //                 if (commandContext.getMonitorAmount()==0) {
    //                     int hashCode=Math.abs((oid+serviceTaskName).hashCode());
    //                     commandContext.setMonitorAmount(monitorIps.getMonitorAmount());
    //                     commandContext.setMonitorNumber(monitorIps.getInitialMonitorNumber(hashCode));
    //                 }
    //                 //String monitorIp=monitorIps.chooseMonitorIp(commandContext.getMonitorNumber());
    //                 String monitorUrl="http://127.0.0.1:8999/grafana/run?loadBalance=enabled";


    //                 Map<String,Object> dataPool=commandContext.getCachedOutput();
    //                 String body="{}";
    //                 String header=headers==null?"{}":headers.getValue(execution).toString();
    //                 if (serviceInfoMap==null) {
    //                     if (output!=null) out=output.getValue(execution).toString();
    //                     //input的缺省值
    //                     if (input==null||input.getValue(execution).toString().equals("default")) {
    //                         if (lastResponse==null) {
    //                             body=businessData;
    //                         } else {
    //                             body=lastResponse;
    //                         }
    //                     } else if (input.getValue(execution).toString().equals("business")) {
    //                         body=businessData;
    //                     } else {
    //                         body=jsonPathInputSelector.select(dataPool, input.getValue(execution).toString());
    //                     }
    //                     //智慧城市需要，对输入做一些处理
    //                     if (modify!=null) {
    //                         Map<String,Object> bodyMap=jsonTransfer.jsonToMap(body);
    //                         String modifyString=modify.getValue(execution).toString();
    //                         Map<String,Object> modifyMap=jsonTransfer.jsonToMap(modifyString);
    //                         StringBuilder valueBuilder=new StringBuilder();
    //                         for (String modifyKey:modifyMap.keySet()) {
    //                             if (!bodyMap.containsKey(modifyKey)) {
    //                                 throw new RuntimeException("对key为"+modifyKey+"的值加工失败，因为没有对应的key");
    //                             }
    //                             String value=String.valueOf(modifyMap.get(modifyKey));
    //                             if (!value.contains("${}")) {
    //                                 throw new RuntimeException(value+"格式错误，因为没有找到${}");
    //                             }
    //                             int left=value.indexOf("${}");
    //                             valueBuilder.append(value.substring(0, left)).append(String.valueOf(bodyMap.get(modifyKey)))
    //                                         .append(value.substring(left+3));
    //                             bodyMap.put(modifyKey,valueBuilder.toString());
    //                             valueBuilder.setLength(0);
    //                         }
    //                         body=jsonTransfer.toJsonString(bodyMap);
    //                     }
    //                 } else {
    //                     if (serviceInfoMap.containsKey("output")) out=String.valueOf(serviceInfoMap.get("output"));
    //                     if (!serviceInfoMap.containsKey("input")||String.valueOf(serviceInfoMap.get("input")).equals("default")) {
    //                         if (lastResponse==null) {
    //                             body=businessData;
    //                         } else {
    //                             body=lastResponse;
    //                         }
    //                     } else if (String.valueOf(serviceInfoMap.get("input")).equals("business")) {
    //                         body=businessData;
    //                     } else {
    //                         body=jsonPathInputSelector.select(dataPool, String.valueOf(serviceInfoMap.get("input")));
    //                     }
    //                     //智慧城市需要，对输入做一些处理
    //                     if (serviceInfoMap.containsKey("modify")) {
    //                         Map<String,Object> bodyMap=jsonTransfer.jsonToMap(body);
    //                         String modifyString=String.valueOf(serviceInfoMap.get("modify"));
    //                         Map<String,Object> modifyMap=jsonTransfer.jsonToMap(modifyString);
    //                         StringBuilder valueBuilder=new StringBuilder();
    //                         for (String modifyKey:modifyMap.keySet()) {
    //                             if (!bodyMap.containsKey(modifyKey)) {
    //                                 throw new RuntimeException("对key为"+modifyKey+"的值加工失败，因为没有对应的key");
    //                             }
    //                             String value=String.valueOf(modifyMap.get(modifyKey));
    //                             if (!value.contains("${}")) {
    //                                 throw new RuntimeException(value+"格式错误，因为没有找到${}");
    //                             }
    //                             int left=value.indexOf("${}");
    //                             valueBuilder.append(value.substring(0, left)).append(String.valueOf(bodyMap.get(modifyKey)))
    //                                         .append(value.substring(left+3));
    //                             bodyMap.put(modifyKey,valueBuilder.toString());
    //                             valueBuilder.setLength(0);
    //                         }
    //                         body=jsonTransfer.toJsonString(bodyMap);
    //                     }
    //                 }
    //                 /*
    //                 * 拼装json数据
    //                 */
    //                 Map<String,Object> postMap=new HashMap<String,Object>();
    //                 Map<String,Object> temp=jsonTransfer.jsonToMap(body);
    //                 //这下面这个是为了实现租约锁，个人认为并不是一个好的方案，没有租约锁的可以删除这个put
    //                 //temp.put("oid",oid);


    //                 body=jsonTransfer.toJsonString(temp);
    //                 if (serviceInfoMap==null) {
    //                     //s-consumerName用oid@serviceTaskName
    //                     if (route==null) throw new RuntimeException("serviceName不为空的情况下,route不能为空");
    //                     if (httpMethod==null) throw new RuntimeException("serviceName不为空的情况下,httpMethod不能为空");
    //                     postMap.put("s-consumerName",commandContext.getOid()+"@"+execution.getCurrentFlowElement().getName());
    //                     postMap.put("s-serviceName",serviceName.getValue(execution).toString());
    //                     postMap.put("headers",header);
    //                     postMap.put("s-url",route.getValue(execution).toString());
    //                     postMap.put("s-method",httpMethod.getValue(execution).toString());
    //                     postMap.put("body",body);
    //                     if (serviceGroup!=null) {
    //                         postMap.put("s-group",serviceGroup.getValue(execution).toString());
    //                     } 
    //                 } else {
    //                     postMap.put("s-consumerName",commandContext.getOid()+"@"+execution.getCurrentFlowElement().getName());
    //                     postMap.put("s-serviceName",serviceInfoMap.get("serviceName"));
    //                     postMap.put("headers",serviceInfoMap.containsKey("headers")?serviceInfoMap.get("headers"):"{}");
    //                     postMap.put("s-url",serviceInfoMap.get("route"));
    //                     postMap.put("s-method",serviceInfoMap.get("httpMethod"));
    //                     postMap.put("body",body);
    //                     if (serviceInfoMap.containsKey("serviceGroup")) {
    //                         postMap.put("s-group",serviceInfoMap.get("serviceGroup"));
    //                     } 

    //                 }
    //                 /*
    //                 * 这里做随机选择用post的数据，以oid做密钥，生成一个数
    //                 * 然后取余，通过对各个节点标记，从而进行选择
    //                 */
    //                 //表明要请求服务
    //                 Future<SimpleHttpResponse> future = Connect.doPost(monitorUrl, jsonTransfer.toJsonString(postMap));
    //                 SimpleHttpResponse response = future.get();
    //                 /*
    //                 * 一个线程里如果同时只有一个activiti的命令在运行，则上下文栈里只有唯一一个上下文
    //                 * 可以通过增加上下文中的属性，对属性进行改写来达到一个数据传递的作用
    //                 * 这里改写的是业务数据
    //                 * 将返回的数据，保存为业务数据
    //                 */
    //                 if (response.getCode()!=200) {
    //                     if (cachedData.hasErrorBoundaryEvent(activityId, oid.split("@")[0])) {
    //                         serviceTaskRes result=new serviceTaskRes();
    //                         result.setStatus(false);
    //                         result.setBody(response.getBodyText());
    //                         cachedServiceTaskResult.getServiceTaskRes(oid).put(serviceTaskName,result);
    //                         throw new BpmnError("httpError");
    //                     } else {
    //                         throw new RuntimeException("服务调用出错,返回结果:"+response.getBodyText());
    //                     }
    //                     //throw new BpmnError("httpError",response.getBodyText());
    //                 } else {
    //                     if (response.getBodyText().equals("try lock failed")) throw new RuntimeException("加锁失败"); 
    //                     serviceTaskRes result=new serviceTaskRes();
    //                     result.setStatus(true);
    //                     result.setBody(response.getBodyText());
    //                     cachedServiceTaskResult.getServiceTaskRes(oid).put(serviceTaskName,result);
    //                 }
    //                 commandContext.setLastResponse((response.getBodyText()));
    //                 commandContext.storeOutput(execution.getCurrentFlowElement().getName(), jsonTransfer.jsonToMap(response.getBodyText()));
    //                 if (serviceInfoMap==null) unReleaseUrls.addUrl(serviceName.getValue(execution).toString());
    //                 else unReleaseUrls.addUrl(String.valueOf(serviceInfoMap.get("serviceName")));

    //                 //对齐output
    //                 if (out!=null) {
    //                     //如果有指定输出
    //                     String outputString=jsonPathInputSelector.select(dataPool, out);
    //                     commandContext.setLastResponse(outputString);
    //                 }
    //                 //流程变量设置
    //                 if (processVariable!=null) {
    //                     Map<String,Object> variables=jsonTransfer.jsonToMap(jsonPathInputSelector.select(dataPool, processVariable.getValue(execution).toString()));
    //                     execution.setVariables(variables);
    //                 }
    //             } else {
    //                 serviceTaskRes serviceTaskRes= serviceTaskResMap.get(serviceTaskName);
    //                 if (serviceTaskRes==null) {
    //                     throw new RuntimeException("未检测到"+serviceTaskName+"的服务任务的执行结果");
    //                 }
    //                 if (!serviceTaskRes.isStatus()) {
    //                     throw new BpmnError("httpError");
    //                 }
    //             }
    //         }

    //         /*
    //          * 判断是否要开启或跳转至其他流程
    //          */
    //         RuntimeService runtimeService = ActivitiUtils.runtimeService;
    //         if (nextMessage!=null) {
    //             //表明信息流流向intermidateMessageCatchEvent
    //             String messageName=nextMessage.getValue(execution).toString();
    //             runtimeService.messageEventReceived(messageName, oidEvents.getEventExecutionIdByOidAndName(oid,messageName),execution.getVariables());
    //         }
    //         if (nextProcess!=null) {
    //             //表明信息流流向startMessageEvent
    //             String processDefinitionKey=nextProcess.getValue(execution).toString();
    //             runtimeService.startProcessInstanceById(cachedData.getProcessId(processDefinitionKey,oid),execution.getVariables());
    //         }
    //         // variables.put("have", "true");
    //         // variables =
    //         // ActivitiUtils.addVariables(JSON.parseObject(response.getBodyText()),
    //         // variables);
    //         // runtimeService.setVariables(execution.getId(), variables);
    //     } catch (Exception e) {
    //         if (e instanceof BpmnError) throw new BpmnError("httpError");
    //         else throw new RuntimeException("serviceTask:"+execution.getCurrentFlowElement().getName()+"执行失败,失败原因:"+e.getMessage());
    //     }
    // }

    // public static class lockException extends RuntimeException {}
}