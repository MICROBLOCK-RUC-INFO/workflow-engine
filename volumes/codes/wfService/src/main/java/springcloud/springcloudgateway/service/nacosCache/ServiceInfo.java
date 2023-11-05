package springcloud.springcloudgateway.service.nacosCache;

import java.util.List;


/**
 * @Author: 李浩然
 * @Date: 2021/1/21 6:32 下午
 */

public class ServiceInfo {
    private Long lastModifiedMillis;
    private String serviceName;
    private String group;
    private List<InstanceInfo> InstanceList;
    private List<InstanceInfo> HealthyInstanceList;

    public ServiceInfo(String serviceName, String group) {
        this.serviceName = serviceName;
        this.group = group;
    }

    public Long getLastModifiedMillis() {
        return lastModifiedMillis;
    }

    public void setLastModifiedMillis(Long lastModifiedMillis) {
        this.lastModifiedMillis = lastModifiedMillis;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<InstanceInfo> getInstanceList() {
        return InstanceList;
    }

    public void setInstanceList(List<InstanceInfo> instanceList) {
        InstanceList = instanceList;
    }

    public List<InstanceInfo> getHealthyInstanceList() {
        return HealthyInstanceList;
    }

    public void setHealthyInstanceList(List<InstanceInfo> healthyInstanceList) {
        HealthyInstanceList = healthyInstanceList;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "lastModifiedMillis=" + lastModifiedMillis +
                ", serviceName='" + serviceName + '\'' +
                ", group='" + group + '\'' +
                ", InstanceList=" + InstanceList +
                '}';
    }
}