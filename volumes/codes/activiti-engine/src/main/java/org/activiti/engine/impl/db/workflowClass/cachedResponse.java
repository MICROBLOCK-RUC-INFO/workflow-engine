package org.activiti.engine.impl.db.workflowClass;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class cachedResponse {
    private static final long serialVersionUID = 1L;
    private String Oid;
    private Map<String,String> fromTask=new HashMap<>();
    private Map<String,String> toTasks=new HashMap<>();//本来是set,但是fastjson在多层上的性能有待考证，所有toTaskName采用string,用“,”间隔
    //private String businessData;//弃用，原因是业务数据流可以在上层进行处理，服务任务也是上层进行调用。
    private boolean isDeploy;
    private boolean isEnd;
    private String deploymentName;
    private String businessData;
    private String serviceTaskResultJson;
    private String readSetJson;
    private String writeSetJson;
    
    public cachedResponse() {
        
    }
    
    

    public String getReadSetJson() {
        return readSetJson;
    }



    public void setReadSetJson(String readSetJson) {
        this.readSetJson = readSetJson;
    }



    public String getWriteSetJson() {
        return writeSetJson;
    }



    public void setWriteSetJson(String writeSetJson) {
        this.writeSetJson = writeSetJson;
    }



    public String getServiceTaskResultJson() {
        return serviceTaskResultJson;
    }

    public void setServiceTaskResultJson(String serviceTaskResultJson) {
        this.serviceTaskResultJson = serviceTaskResultJson;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public String getBusinessData() {
        return businessData;
    }
    public void setBusinessData(String businessData) {
        this.businessData = businessData;
    }
    public String getDeploymentName() {
        return deploymentName;
    }
    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }
    public boolean isDeploy() {
        return isDeploy;
    }
    public void setDeploy(boolean isDeploy) {
        this.isDeploy = isDeploy;
    }

    public String getOid() {
        return Oid;
    }
    public void setOid(String Oid) {
        this.Oid = Oid;
    }
    public Map<String, String> getFromTask() {
        return fromTask;
    }
    public void setFromTask(Map<String, String> fromTask) {
        this.fromTask = fromTask;
    }
    public Map<String, String> getToTasks() {
        return toTasks;
    }
    //空则set,不为空则putAll
    public void setToTasks(Map<String, String> toTasks) {
        this.toTasks.putAll(toTasks);
    }



    public String getEncodeString() {
        return new workflowResponse(this).getEncodeString();
        // try {
        //     ByteArrayOutputStream value=new ByteArrayOutputStream();
        //     ObjectOutputStream out = new ObjectOutputStream(value);
        //     out.writeObject(this);
        //     String res=Base64.getEncoder().encodeToString(value.toByteArray());
        //     out.close();
        //     value.close();
        //     return res;
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return e.getMessage();
        // }
    }

    public String getResponseString() {
        //字段属性之间用|分割，字段名与字段值之间用~分割，字段值的hashMap,不同key用,分割，key-value用=分割
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("oid:").append(Oid).append('|')
                     .append("fromTask:").append(fromTask.toString()).append('|')
                     .append("toTasks:").append(toTasks.toString()).append('|')
                     .append("isDeploy:").append(String.valueOf(isDeploy)).append('|')
                     .append("deploymentName:").append(deploymentName).append('|')
                     .append("businessData").append(businessData);
        return stringBuilder.toString();
    }
}

