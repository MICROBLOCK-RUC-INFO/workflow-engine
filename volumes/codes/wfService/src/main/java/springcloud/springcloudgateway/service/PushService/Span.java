package springcloud.springcloudgateway.service.PushService;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author: 李浩然
 * @Date: 2021/3/18 6:58 下午
 */
public class Span {
    private long startTime;
    private long endTime;
    private String operationName;
    private Ref[] refs;
    private boolean isError;

    public Span(long startTime, long endTime, String operationName, Ref[] refs, boolean isError) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.operationName = operationName;
        this.refs = refs;
        this.isError = isError;
    }

    public Span(long startTime, long endTime, String operationName, Ref[] refs) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.operationName = operationName;
        this.refs = refs;
    }

    public Span(long startTime, long endTime, String operationName) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.operationName = operationName;
    }

    public Span(long startTime, long endTime, String operationName, boolean isError) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.operationName = operationName;
        this.isError = isError;
    }


    @Override
    public String toString() {
        return "Span{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", operationName='" + operationName + '\'' +
                ", refs=" + Arrays.toString(refs) +
                ", isError=" + isError +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Span span = (Span) o;
        return startTime == span.startTime &&
                endTime == span.endTime &&
                isError == span.isError &&
                Objects.equals(operationName, span.operationName) &&
                Arrays.equals(refs, span.refs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(startTime, endTime, operationName, isError);
        result = 31 * result + Arrays.hashCode(refs);
        return result;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Ref[] getRefs() {
        return refs;
    }

    public void setRefs(Ref[] refs) {
        this.refs = refs;
    }

    public boolean getIsError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }
}
