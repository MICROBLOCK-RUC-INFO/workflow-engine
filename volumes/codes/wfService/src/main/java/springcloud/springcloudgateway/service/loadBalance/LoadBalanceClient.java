package springcloud.springcloudgateway.service.loadBalance;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import springcloud.springcloudgateway.service.nacosCache.CacheService;
import springcloud.springcloudgateway.service.nacosCache.InstanceInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 李浩然
 * @Date: 2021/1/22 1:38 上午
 */
@Service
public class LoadBalanceClient implements LoadBalancer {

    @Autowired
    private CacheService cacheService;

    private static final HashMap<String, AtomicInteger> currentIndexMap = new HashMap<>();

    public InstanceInfo chooseServerNoCache(String serviceName) throws Exception {
        return chooseServer(serviceName, "DEFAULT_GROUP");
    }

    public InstanceInfo chooseServerNoCache(String serviceName,String group) throws Exception {
        List<InstanceInfo> upList = null; //当前存活的服务
        List<InstanceInfo> allList = null;  //获取全部的服务
        try {
            upList = getReachableServersNoCache(serviceName, group);
            allList = getAllServersNoCache(serviceName, group);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int serverCount = allList.size();
        if (serverCount == 0) {
            return null;
        }
        String key = group + "@@" + serviceName;
        InstanceInfo server = null;
        while (server == null) {
            if (!currentIndexMap.containsKey(key)) {
                AtomicInteger a = new AtomicInteger();
                a.set(0);
                currentIndexMap.put(key, a);
            }
            if (currentIndexMap.get(key).get() < serverCount) {
                server = upList.get(currentIndexMap.get(key).getAndIncrement());
            } else {
                currentIndexMap.get(key).set(0);
                server = upList.get(currentIndexMap.get(key).getAndIncrement());
            }
        }
        return server;
    }

    @Override
    public InstanceInfo chooseServer(String serviceName) throws Exception {
        return chooseServer(serviceName, "DEFAULT_GROUP");
    }

    @Override
    public synchronized InstanceInfo chooseServer(String serviceName, String group) throws Exception {
        List<InstanceInfo> upList = null; //当前存活的服务
        List<InstanceInfo> allList = null;  //获取全部的服务
        try {
            upList = getReachableServers(serviceName, group);
            allList = getAllServers(serviceName, group);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int serverCount = allList.size();
        if (serverCount == 0) {
            return null;
        }
        String key = group + "@@" + serviceName;
        InstanceInfo server = null;
        while (server == null) {
            if (!currentIndexMap.containsKey(key)) {
                AtomicInteger a = new AtomicInteger();
                a.set(0);
                currentIndexMap.put(key, a);
            }
            if (currentIndexMap.get(key).get() < serverCount) {
                server = upList.get(currentIndexMap.get(key).getAndIncrement());
            } else {
                currentIndexMap.get(key).set(0);
                server = upList.get(currentIndexMap.get(key).getAndIncrement());
            }
        }
        return server;
    }

    @Override
    public List<InstanceInfo> getReachableServers(String serviceName, String group) throws Exception {
        return cacheService.getCacheInfo(serviceName, group).getInstanceList();
    }

    @Override
    public List<InstanceInfo> getAllServers(String serviceName, String group) throws Exception {
        return cacheService.getCacheInfo(serviceName, group).getInstanceList();
    }

    public List<InstanceInfo> getReachableServersNoCache(String serviceName,String group) throws Exception {
        return cacheService.getServiceNoCache(serviceName, group).getInstanceList();
    }

    public List<InstanceInfo> getAllServersNoCache(String serviceName,String group) throws Exception {
        return cacheService.getServiceNoCache(serviceName, group).getInstanceList();
    }

    @Override
    public URI reconstructURI(InstanceInfo instance, URI original) {
        Assert.notNull(instance, "instance can not be null");
        String host = instance.getIp();
        int port = instance.getPort();
        String scheme = instance.getScheme();
        if (host.equals(original.getHost()) && port == original.getPort() && scheme == original.getScheme()) {
            return original;
        } else {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(scheme).append("://");
                if (!Strings.isNullOrEmpty(original.getRawUserInfo())) {
                    sb.append(original.getRawUserInfo()).append("@");
                }

                sb.append(host);
                if (port >= 0) {
                    sb.append(":").append(port);
                }

                sb.append(original.getRawPath());
                if (!Strings.isNullOrEmpty(original.getRawQuery())) {
                    sb.append("?").append(original.getRawQuery());
                }

                if (!Strings.isNullOrEmpty(original.getRawFragment())) {
                    sb.append("#").append(original.getRawFragment());
                }

                URI newURI = new URI(sb.toString());
                return newURI;
            } catch (URISyntaxException var8) {
                throw new RuntimeException(var8);
            }
        }
    }
}
