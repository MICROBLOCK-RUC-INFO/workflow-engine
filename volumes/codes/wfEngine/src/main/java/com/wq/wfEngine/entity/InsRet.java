package com.wq.wfEngine.entity;

import com.alibaba.fastjson.JSON;
import java.util.List;

public class InsRet {
    String taskList;
    String state;
    String operationList="[]";
    public String getTaskList() {
        return taskList;
    }

    public void setTaskList(String taskList) {
        this.taskList = taskList;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOperationList() {
        return operationList;
    }

    public void setOperationList(String operationList) {
        this.operationList = operationList;
    }

    public void addOperation(String operation){
        List<String> opList = JSON.parseArray(this.operationList,String.class);
        opList.add(operation);
        this.operationList=JSON.toJSONString(opList);
    }
}
