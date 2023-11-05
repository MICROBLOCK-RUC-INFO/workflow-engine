package org.activiti.engine.impl.db.workflowClass;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;

public class randomGenerator {
    public static String getWorkflowResponseString() {
        Random r=new Random();
        workflowResponse workflowResponse=new workflowResponse();
        workflowResponse.setOid(RandomStringUtils.random(10));
        workflowResponse.setDeploy(r.nextBoolean());
        workflowResponse.setDeploymentName(RandomStringUtils.random(10));
        workflowResponse.setEnd(r.nextBoolean());
        Set<String> fromTask=new HashSet<String>() {{
            add(RandomStringUtils.random(5));
        }};
        workflowResponse.setFromTaskName(fromTask);
        Set<String> toTask=new HashSet<String>() {{
            add(RandomStringUtils.random(5));
        }};
        workflowResponse.setToTaskName(toTask);
        return workflowResponse.getEncodeString();
    }
}
