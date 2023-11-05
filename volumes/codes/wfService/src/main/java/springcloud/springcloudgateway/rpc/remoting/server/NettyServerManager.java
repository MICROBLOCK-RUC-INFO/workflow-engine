package springcloud.springcloudgateway.rpc.remoting.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.remoting.RemotingException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServerManager {

    private final Logger logger = LoggerFactory.getLogger(NettyServerManager.class);

    private static volatile NettyServerManager instance;

    private static boolean flag = true;

    private Map<String, AbstractServer> serverMap = new ConcurrentHashMap();

    private Map<String, ServiceConfig> services = new ConcurrentHashMap<>();

    private NettyServerManager() {
        if (flag) {
            flag = false;
        } else {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static NettyServerManager getInstance() {
        NettyServerManager ret = instance;
        if (ret == null) {
            synchronized (NettyServerManager.class) {
                ret = instance;
                if (ret == null) {
                    instance = ret = new NettyServerManager();
                }
            }
        }
        return ret;
    }

    public ServiceConfig getWrapper(String serviceName) {
        return services.get(serviceName);
    }

    public void exportServer(ServiceConfig service, String host, Integer port) throws RemotingException {
        services.put(service.getInterface(), service);
        String key = host + ":" + port;
        AbstractServer server = serverMap.get(key);
        if (server == null) {
            logger.info("Starting Netty Server....");
            server = new NettyServer(host, port);
            serverMap.put(key, server);
        } else {
            logger.info("Netty Server[{}] already exsit", key);
        }
    }
}
