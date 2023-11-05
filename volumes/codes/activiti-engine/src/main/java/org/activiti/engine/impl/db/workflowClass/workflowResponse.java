package org.activiti.engine.impl.db.workflowClass;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.db.redis.tools.jackJson.jsonTransfer;

public class workflowResponse implements Serializable{
    private static final long serialVersionUID = 1L;
    private String Oid;
    private Set<String> fromTaskName;
    private Set<String> toTaskName;//本来是set,但是fastjson在多层上的性能有待考证，所有toTaskName采用string,用“,”间隔
    //private String businessData;//弃用，原因是业务数据流可以在上层进行处理，服务任务也是上层进行调用。
    private boolean isDeploy;
    private boolean isEnd;
    private String deploymentName;
    private String businessData;
    private String serviceTaskResultJson="{}";
    //用于读写集
    private String readSetJson="";
    private String writeSetJson="";
    //用户的input?
    private transient long startTime;
    private transient long simulationEndTime;
    private transient long startPutToBlockChain;
    private transient long flushStartTime;
    private transient long flushEndTime;
    
    public workflowResponse(cachedResponse cache) {
        this.Oid=cache.getOid();
        this.fromTaskName=cache.getFromTask()==null?new HashSet<String>():new HashSet<>(cache.getFromTask().keySet());
        this.toTaskName=cache.getToTasks()==null?new HashSet<String>():new HashSet<>(cache.getToTasks().keySet());
        this.isDeploy=cache.isDeploy();
        this.deploymentName=cache.getDeploymentName()==null?Oid.split("@")[0]:cache.getDeploymentName();
        this.businessData=cache.getBusinessData()==null?"...":cache.getBusinessData();
        this.isEnd=cache.isEnd();
        this.serviceTaskResultJson=cache.getServiceTaskResultJson();
        this.readSetJson=cache.getReadSetJson()==null?"":cache.getReadSetJson();
        this.writeSetJson=cache.getWriteSetJson()==null?"":cache.getWriteSetJson();
    }

    public workflowResponse() {
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




    public long getStartTime() {
        return startTime;
    }




    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }




    public long getSimulationEndTime() {
        return simulationEndTime;
    }




    public void setSimulationEndTime(long simulationEndTime) {
        this.simulationEndTime = simulationEndTime;
    }




    public long getStartPutToBlockChain() {
        return startPutToBlockChain;
    }




    public void setStartPutToBlockChain(long startPutToBlockChain) {
        this.startPutToBlockChain = startPutToBlockChain;
    }




    public long getFlushStartTime() {
        return flushStartTime;
    }




    public void setFlushStartTime(long flushStartTime) {
        this.flushStartTime = flushStartTime;
    }




    public long getFlushEndTime() {
        return flushEndTime;
    }




    public void setFlushEndTime(long flushEndTime) {
        this.flushEndTime = flushEndTime;
    }




    public boolean isEnd() {
        return isEnd;
    }




    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }




    public boolean isDoServiceTask() {
        return !businessData.equals("...");
    }

    public String getOid() {
        return Oid;
    }



    public void setOid(String oid) {
        Oid = oid;
    }



    public Set<String> getFromTaskName() {
        return fromTaskName;
    }



    public void setFromTaskName(Set<String> fromTaskName) {
        this.fromTaskName = fromTaskName;
    }



    public Set<String> getToTaskName() {
        return toTaskName;
    }



    public void setToTaskName(Set<String> toTaskName) {
        this.toTaskName = toTaskName;
    }



    public boolean isDeploy() {
        return isDeploy;
    }



    public void setDeploy(boolean isDeploy) {
        this.isDeploy = isDeploy;
    }



    public String getDeploymentName() {
        return deploymentName;
    }



    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }



    public String getEncodeString() {
        try {
            ByteArrayOutputStream value=new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(value);
            out.writeObject(this);
            String res=Base64.getEncoder().encodeToString(value.toByteArray());
            out.close();
            value.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public Map<String,Object> getViewMap() {
        Map<String,Object> viewMap=new HashMap<>();
        //viewMap.put("Oid",Oid);
        viewMap.put("fromTaskName",fromTaskName.toString());
        viewMap.put("toTaskName",toTaskName.toString());
        viewMap.put("isDeploy",isDeploy);
        viewMap.put("deploymentName",deploymentName);
        viewMap.put("businessData",businessData);
        viewMap.put("isEnd",isEnd);
        //viewMap.put("readSetJson",readSetJson);
        //viewMap.put("writeSetJson",writeSetJson);
        viewMap.put("startTime",startTime);
        viewMap.put("simulationEndTime",simulationEndTime);
        viewMap.put("startPutToBlockChain",startPutToBlockChain);
        viewMap.put("flushStartTime",flushStartTime);
        viewMap.put("flushEndTime",flushEndTime);
        return viewMap;
    }

    public String getUploadJson() {
        Map<String,Object> jsonMap=new HashMap<>();
        jsonMap.put("Oid",Oid);
        jsonMap.put("isDeploy",isDeploy);
        jsonMap.put("deploymentName",deploymentName);
        jsonMap.put("businessData",businessData);
        jsonMap.put("serviceTaskResponses",serviceTaskResultJson);
        jsonMap.put("isEnd",isEnd);
        jsonMap.put("readSetJson",readSetJson);
        jsonMap.put("writeSetJson",writeSetJson);
        return jsonTransfer.mapToJsonString(jsonMap);
    }

    public String getUploadString() {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("Oid:").append(Oid).append("$$")
                     .append("isDeploy:").append(String.valueOf(isDeploy)).append("$$")
                     .append("deploymentName:").append(deploymentName).append("$$")
                     .append("businessData:").append(businessData).append("$$")
                     .append("serviceTaskResponses:").append(serviceTaskResultJson).append("$$")
                     .append("isEnd:").append(isEnd).append("$$")
                     .append("readSetJson").append(readSetJson).append("$$")
                     .append("writeSetJson").append(writeSetJson);
        return stringBuilder.toString();
    }

    public String getTestUploadString() {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("Oid:").append(Oid).append("$$")
                     .append("isDeploy:").append(String.valueOf(isDeploy)).append("$$")
                     .append("deploymentName:").append(deploymentName).append("$$")
                     .append("businessData:").append(businessData).append("$$")
                     .append("isEnd:").append(isEnd).append("$$");
        return stringBuilder.toString();
    }

    public boolean equals(workflowResponse wResponse) {
        if (wResponse.getOid().equals(Oid)&&wResponse.getFromTaskName().toString().equals(fromTaskName.toString())
            &&wResponse.getToTaskName().toString().equals(toTaskName.toString())&&wResponse.isDeploy==isDeploy
            &&wResponse.getDeploymentName().equals(deploymentName)&&wResponse.getReadSetJson().equals(readSetJson)
            &&wResponse.getWriteSetJson().equals(writeSetJson)) return true;
        else return false;
    }
}
