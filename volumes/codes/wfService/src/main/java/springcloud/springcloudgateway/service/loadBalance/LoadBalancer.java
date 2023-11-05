package springcloud.springcloudgateway.service.loadBalance;

import springcloud.springcloudgateway.service.nacosCache.InstanceInfo;

import java.net.URI;
import java.util.List;

/**
 * @Author: 李浩然
 * @Date: 2021/1/22 10:56 上午
 */
public interface LoadBalancer {

    InstanceInfo chooseServer(String serviceName) throws Exception;

    InstanceInfo chooseServer(String serviceName, String group) throws Exception;

    List<InstanceInfo> getReachableServers(String serviceName, String group) throws Exception;

    List<InstanceInfo> getAllServers(String serviceName, String group) throws Exception;

    URI reconstructURI(InstanceInfo instance, URI original);
}