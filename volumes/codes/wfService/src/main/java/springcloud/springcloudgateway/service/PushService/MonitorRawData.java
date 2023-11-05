package springcloud.springcloudgateway.service.PushService;

import java.util.Arrays;
import java.util.Objects;

/**
 * 原始数据类
 *
 * @Author: 李浩然
 * @Date: 2020/12/13 11:29 下午
 */
public class MonitorRawData {
    private String traceId;
    private String traceSegmentId;
    private String service;
    private String serviceInstance;
    private Span[] spans;

    public MonitorRawData(String traceId, String traceSegmentId, String service, String serviceInstance, Span[] spans) {
        this.traceId = traceId;
        this.traceSegmentId = traceSegmentId;
        this.service = service;
        this.serviceInstance = serviceInstance;
        this.spans = spans;
    }

    public MonitorRawData(String service, String serviceInstance, Span[] spans) {
        this.service = service;
        this.serviceInstance = serviceInstance;
        this.spans = spans;
    }

    @Override
    public String toString() {
        return "MonitorRawData{" +
                "traceId='" + traceId + '\'' +
                ", traceSegmentId='" + traceSegmentId + '\'' +
                ", service='" + service + '\'' +
                ", serviceInstance='" + serviceInstance + '\'' +
                ", spans=" + Arrays.toString(spans) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitorRawData that = (MonitorRawData) o;
        return Objects.equals(traceId, that.traceId) &&
                Objects.equals(traceSegmentId, that.traceSegmentId) &&
                Objects.equals(service, that.service) &&
                Objects.equals(serviceInstance, that.serviceInstance) &&
                Arrays.equals(spans, that.spans);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(traceId, traceSegmentId, service, serviceInstance);
        result = 31 * result + Arrays.hashCode(spans);
        return result;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTraceSegmentId() {
        return traceSegmentId;
    }

    public void setTraceSegmentId(String traceSegmentId) {
        this.traceSegmentId = traceSegmentId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(String serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public Span[] getSpans() {
        return spans;
    }

    public void setSpans(Span[] spans) {
        this.spans = spans;
    }
}
