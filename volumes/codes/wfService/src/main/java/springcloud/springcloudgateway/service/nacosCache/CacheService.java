package springcloud.springcloudgateway.service.nacosCache;

/**
 * @Author: 李浩然
 * @Date: 2021/1/21 7:15 下午
 */
public interface CacheService {

    /**
     * 增加服务信息
     *
     * @param Info 服务缓存
     */
    void addCacheInfo(ServiceInfo Info);

    /**
     * 获取服务信息
     *
     * @param serviceName 服务名称
     * @param group       组织
     * @return 用户信息
     */
    ServiceInfo getCacheInfo(String serviceName, String group) throws Exception;

    ServiceInfo getCacheInfo(String serviceName) throws Exception;

    /**
     * 删除服务信息
     *
     * @param serviceName
     * @param group
     */
    void deleteCache(String serviceName, String group);

    ServiceInfo getServiceNoCache(String serviceName, String group) throws Exception;

    void updateCache() throws Exception;

}