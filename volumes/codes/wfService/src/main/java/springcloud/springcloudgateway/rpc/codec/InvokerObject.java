package springcloud.springcloudgateway.rpc.codec;

import java.util.Arrays;

/**
 * @author shizhengchao
 */
public class InvokerObject {

    /**
     * 类全名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 调用参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 调用参数
     */
    private Object[] parameters;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[RPC Request]")
                .append("{\"className\":").append(className)
                .append("\"methodName\":").append(methodName);
        if (parameters != null && parameters.length > 0) {
            sb.append("\"params\":").append(Arrays.toString(parameters));
        }
        sb.append("}");
        return sb.toString();
    }
}
