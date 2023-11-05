package springcloud.springcloudgateway.service.nacosCache;

import javax.json.JsonObject;

/**
 * @Author: 李浩然
 * @Date: 2021/1/21 10:09 下午
 */
public class InstanceInfo {
    private String instanceId;
    private String ip;
    private int port;
    private String serviceName;
    private long lastBeat;
    private String metaData;
    private boolean ephemeral;
    private boolean healthy;
    private boolean enabled;
    private String scheme = "http";

    public InstanceInfo() {
    }

    @Override
    public String toString() {
        return "InstanceInfo{" +
                "instanceId='" + instanceId + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", serviceName='" + serviceName + '\'' +
                ", lastBeat=" + lastBeat +
                ", metaData='" + metaData + '\'' +
                ", ephemeral=" + ephemeral +
                ", healthy=" + healthy +
                ", enabled=" + enabled +
                ", scheme='" + scheme + '\'' +
                '}';
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public long getLastBeat() {
        return lastBeat;
    }

    public void setLastBeat(long lastBeat) {
        this.lastBeat = lastBeat;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(JsonObject metaData) {
        this.metaData = metaData.toString();
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}
