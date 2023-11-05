package springcloud.springcloudgateway.rpc.remoting.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.remoting.RemotingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClientManager {

    private final Logger logger = LoggerFactory.getLogger(NettyClientManager.class);

    private static volatile NettyClientManager instance;

    private static boolean flag = true;

    private Map<String, AbstractClient> clientMap = new ConcurrentHashMap();

    private AtomicInteger roundRobin = new AtomicInteger(0);

    private NettyClientManager() {
        if (flag) {
            flag = false;
        } else {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static NettyClientManager getInstance() {
        NettyClientManager ret = instance;
        if (ret == null) {
            synchronized (NettyClientManager.class) {
                ret = instance;
                if (ret == null) {
                    instance = ret = new NettyClientManager();
                }
            }
        }
        return ret;
    }

    public void connectServer(String host, Integer port, int connectTimeout) throws RemotingException {
        String key = host + ":" + port;
        AbstractClient client = clientMap.get(key);
        if (client == null) {
            logger.info("Starting Netty Client....");
            client = new NettyClient(host, port, connectTimeout);
            clientMap.put(key, client);
        } else {
            logger.info("Netty Client[{}] already exsit", key);
        }
    }

    public AbstractClient balanceClient() {
        int size = clientMap.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        List<AbstractClient> list = new ArrayList<>(clientMap.values());
        return list.get(index);
    }
}
