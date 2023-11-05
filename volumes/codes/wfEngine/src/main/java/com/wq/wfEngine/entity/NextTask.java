package com.wq.wfEngine.entity;

import java.util.List;

public class NextTask {
    private String insName;
    private List<String> taskName;

    public String getInsName() {
        return insName;
    }

    public void setInsName(String insName) {
        this.insName = insName;
    }

    public List<String> getTaskName() {
        return taskName;
    }

    public void setTaskName(List<String> taskName) {
        this.taskName = taskName;
    }
}
