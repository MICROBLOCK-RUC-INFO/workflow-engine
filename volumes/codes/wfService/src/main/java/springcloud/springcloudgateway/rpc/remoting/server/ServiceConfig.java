package springcloud.springcloudgateway.rpc.remoting.server;

import springcloud.springcloudgateway.rpc.remoting.RemotingException;
import springcloud.springcloudgateway.rpc.utils.Wrapper;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 服务发布
 *
 * @param <T>
 */
public class ServiceConfig<T> {

    private Integer port;

    private String host;

    /**
     * 暴露服务的接口名
     */
    private String interfaceName;

    /**
     * 暴露服务的类
     */
    private Class<?> interfaceClass;

    /**
     * 暴露的接口实现的引用
     */
    private T ref;

    private String zkAddress;

    private Wrapper wrapper;

    public ServiceConfig() {

    }

    public ServiceConfig(Integer port, String host) {
        this.port = port;
        this.host = host;
    }

    public String getInterface() {
        return interfaceName;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public Wrapper getWrapper() {
        return wrapper;
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
     * 1. 启动netty
     * 2. 注册服务到zk
     */
    public void export() {
        Integer port = this.port == null ? 20880 : this.port;
        String host = this.host == null ? findHost() : this.host;
        this.wrapper = Wrapper.getWrapper(ref.getClass());
        try {
            NettyServerManager.getInstance().exportServer(this, host, port);
        } catch (RemotingException e) {

        }
//    startNetty();
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
