package com.wq.wfEngine.entity;

import com.alibaba.fastjson.JSON;
import org.activiti.bpmn.model.FormProperty;

import java.util.*;

public class WfTask {
    private String name;
    private String assignee = "";
    private String candidateGroups;
    private String owner;
    private String taskId;
    private List<FormProperty> formPropertiesList = new ArrayList<>();
    private String formKey = "";
    private List<String> selectServer = new ArrayList<>();
    private List<String> requestServer = new ArrayList<>();
    private boolean requestNode;
    // private WebRequest webRequest;
    private int status = 0;
    // 请求服务节点的状态初始为0，服务执行完毕后置1
    private String taskType;
    private String serviceOrg;
    private String serviceName;
    private String serviceInterface;
    private String serviceRole;
    private String httpMethod;
    private String requestParamList;
    private String responseParamList;
    private String serviceType;

    /*
     * sync表明自动执行的服务无须很长时间的等待
     * send是异步发送
     * recall是异步的接收
     */

    public String getServiceType() {
        return serviceType;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getFormPropertiesList() {
        return JSON.toJSONString(formPropertiesList);
    }

    public List<FormProperty> nonJsonGetFormPropertiesList() {
        return formPropertiesList;
    }

    public void setFormPropertiesList(List<FormProperty> formPropertiesList) {
        this.formPropertiesList = formPropertiesList;
    }

    public void addFormProperty(FormProperty formProperty) {
        this.formPropertiesList.add(formProperty);
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        if (formKey == null) {
            this.formKey = "";
        } else {
            this.formKey = formKey;
        }
    }

    public List<String> getSelectServer() {
        return selectServer;
    }

    public void setSelectServer(List<String> selectServer) {
        this.selectServer = selectServer;
    }

    public void addSelectServer(String selectServerName) {
        this.selectServer.add(selectServerName);
    }

    public List<String> getRequestServer() {
        return requestServer;
    }

    public void setRequestServer(List<String> requestServer) {
        this.requestServer = requestServer;
    }

    public void addRequestServer(String requestServerName) {
        this.requestServer.add(requestServerName);
    }

    public boolean isRequestNode() {
        return requestNode;
    }

    public void setRequestNode(boolean requestNode) {
        this.requestNode = requestNode;
    }

    public String getCandidateGroups() {
        return candidateGroups;
    }

    public void setCandidateGroups(String candidateGroups) {
        this.candidateGroups = candidateGroups;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getServiceOrg() {
        return serviceOrg;
    }

    public void setServiceOrg(String serviceOrg) {
        this.serviceOrg = serviceOrg;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestParamList() {
        return requestParamList;
    }

    public void setRequestParamList(String requestParamList) {
        this.requestParamList = requestParamList;
    }

    public String getResponseParamList() {
        return responseParamList;
    }

    public void setResponseParamList(String responseParamList) {
        this.responseParamList = responseParamList;
    }
}
