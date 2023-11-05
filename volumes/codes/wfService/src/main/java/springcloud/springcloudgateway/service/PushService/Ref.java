package springcloud.springcloudgateway.service.PushService;

import java.util.Objects;

/**
 * @Author: 李浩然
 * @Date: 2021/3/18 7:01 下午
 */
public class Ref {
    private String parentService;
    private String parentServiceInstance;
    private String parentEndpoint;
    private String networkAddressUsedAtPeer;

    public Ref(String parentService, String parentServiceInstance, String parentEndpoint, String networkAddressUsedAtPeer) {
        this.parentService = parentService;
        this.parentServiceInstance = parentServiceInstance;
        this.parentEndpoint = parentEndpoint;
        this.networkAddressUsedAtPeer = networkAddressUsedAtPeer;
    }

    public Ref(String parentService) {
        this.parentService = parentService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref ref = (Ref) o;
        return Objects.equals(parentService, ref.parentService) &&
                Objects.equals(parentServiceInstance, ref.parentServiceInstance) &&
                Objects.equals(parentEndpoint, ref.parentEndpoint) &&
                Objects.equals(networkAddressUsedAtPeer, ref.networkAddressUsedAtPeer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentService, parentServiceInstance, parentEndpoint, networkAddressUsedAtPeer);
    }

    @Override
    public String toString() {
        return "Ref{" +
                "parentService='" + parentService + '\'' +
                ", parentServiceInstance='" + parentServiceInstance + '\'' +
                ", parentEndpoint='" + parentEndpoint + '\'' +
                ", networkAddressUsedAtPeer='" + networkAddressUsedAtPeer + '\'' +
                '}';
    }

    public String getParentService() {
        return parentService;
    }

    public void setParentService(String parentService) {
        this.parentService = parentService;
    }

    public String getParentServiceInstance() {
        return parentServiceInstance;
    }

    public void setParentServiceInstance(String parentServiceInstance) {
        this.parentServiceInstance = parentServiceInstance;
    }

    public String getParentEndpoint() {
        return parentEndpoint;
    }

    public void setParentEndpoint(String parentEndpoint) {
        this.parentEndpoint = parentEndpoint;
    }

    public String getNetworkAddressUsedAtPeer() {
        return networkAddressUsedAtPeer;
    }

    public void setNetworkAddressUsedAtPeer(String networkAddressUsedAtPeer) {
        this.networkAddressUsedAtPeer = networkAddressUsedAtPeer;
    }
}
