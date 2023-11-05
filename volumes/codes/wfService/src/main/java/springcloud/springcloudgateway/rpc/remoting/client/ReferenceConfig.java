package springcloud.springcloudgateway.rpc.remoting.client;

import springcloud.springcloudgateway.rpc.proxy.InvokerInvocationHandler;

import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 引用的封装,返回代理
 *
 * @param <T>
 */
public class ReferenceConfig<T> {

    private int connectTimeout;

    private String host;

    private Integer port;

    private String interfaceName;

    /**
     * The interface class of the reference service
     */
    private Class<?> interfaceClass;

    /**
     * The interface proxy reference
     */
    private transient volatile T ref;

    public T getRef() {
        return ref;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? null : interfaceClass.getSimpleName());
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * 获取代理对象
     *
     * @return
     */
    public synchronized T get() {

        if (ref == null) {
            //初始化
            init();
        }
        return ref;
    }

    private void init() {
        //先暂时默认用固定的本地server,后续加上注册功能
        try {
            NettyClientManager.getInstance().connectServer(host, port, connectTimeout);
        } catch (Throwable e) {
            throw new RuntimeException("could not initional the reference: " + interfaceClass);
        }
        ref = createProxy();
    }

    private T createProxy() {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{interfaceClass}, new InvokerInvocationHandler(interfaceName));
    }

    private String findHost() {
        String bindHost;
        try {
            bindHost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            bindHost = "";
        }
        return bindHost;
    }
}
