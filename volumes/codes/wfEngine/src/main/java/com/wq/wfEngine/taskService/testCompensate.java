package com.wq.wfEngine.taskService;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * 简单的compensateEvent实现
 */
public class testCompensate implements JavaDelegate{
    public void execute(DelegateExecution execution) {
        System.out.println("compensate ok--------------------------------------------");
    }
}
