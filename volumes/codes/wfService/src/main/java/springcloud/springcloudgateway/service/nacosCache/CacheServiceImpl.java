package springcloud.springcloudgateway.service.nacosCache;

/**
 * @Author: 李浩然
 * @Date: 2021/1/21 7:19 下午
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.*;

import springcloud.springcloudgateway.workflow.helper.workflowFabric;

import org.hyperledger.fabric.sdk.ProposalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
//import springcloud.springcloudgateway.service.fabric.FabricServiceNacosImpl;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CacheServiceImpl implements CacheService {
    private Lock lock = new ReentrantLock();
    private Object mutex=new Object();

    @Autowired
    private workflowFabric workflowFabric;

    private Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);

    Cache<String, Object> caffeineCache = Caffeine.newBuilder()
            // 初始的缓存空间大小
            .initialCapacity(100)
            // 缓存的最大条数
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .refreshAfterWrite(1, TimeUnit.HOURS)
            .writer(new CacheWriter<Object, Object>() {
                @Override
                public void write(@NotNull Object key, @NotNull Object value) {
                    log.info("key={},CacheWriter write", key);
                }

                @Override
                public void delete(@NotNull Object key, @Nullable Object value, @NotNull RemovalCause removalCause) {
                    log.info("key={},cause={},CacheWriter delete", key, removalCause);
                }
            })
            .build(new CacheLoader<String, Object>() {
                @Nullable
                @Override
                public Object load(@NotNull String key) throws Exception {
                    log.info("从链上读取");
                    String serviceName = key.split("@@")[1];
                    String group = key.split("@@")[0];
                    ServiceInfo serviceInfo = new ServiceInfo(serviceName, group);
                    String ServiceInstances = getInstanceList(serviceName, group);
                    JSONObject Service = JSONObject.parseObject(ServiceInstances);
                    JSONArray InstanceList = Service.getJSONObject("value").getJSONArray("instanceList");
                    List<InstanceInfo> list = InstanceList.toJavaList(InstanceInfo.class);//参数1为要转换的JSONArray数据，参数2为要转换的目标数据，即List盛装的数据
                    serviceInfo.setInstanceList(list);
                    List<InstanceInfo> reachedList = new ArrayList<>(list);
                    reachedList.removeIf(info -> !info.isHealthy());
                    serviceInfo.setHealthyInstanceList(reachedList);
                    // 如果用户信息不为空，则加入缓存
                    return serviceInfo;
                }

                @Nullable
                @Override
                public Object reload(@NotNull String key, @NotNull Object oldValue) throws Exception {
                    log.info("从链上读取");
                    String serviceName = key.split("@@")[1];
                    String group = key.split("@@")[0];
                    ServiceInfo serviceInfo = new ServiceInfo(serviceName, group);
                    String ServiceInstances = getInstanceList(serviceName, group);
                    JSONObject Service = JSONObject.parseObject(ServiceInstances);
                    JSONArray InstanceList = Service.getJSONObject("value").getJSONArray("instanceList");
                    List<InstanceInfo> list = InstanceList.toJavaList(InstanceInfo.class);//参数1为要转换的JSONArray数据，参数2为要转换的目标数据，即List盛装的数据
                    serviceInfo.setInstanceList(list);
                    List<InstanceInfo> reachedList = new ArrayList<>(list);
                    reachedList.removeIf(info -> !info.isHealthy());
                    serviceInfo.setHealthyInstanceList(reachedList);
                    // 如果用户信息不为空，则加入缓存
                    return serviceInfo;
                }
            });

    public CacheServiceImpl() {
    }

    public void updateCache() throws Exception {
        for (String key:caffeineCache.asMap().keySet()) {
            log.info("从链上读取");
            String serviceName = key.split("@@")[1];
            String group = key.split("@@")[0];
            ServiceInfo serviceInfo = new ServiceInfo(serviceName, group);
            String ServiceInstances = getInstanceList(serviceName, group);
            JSONObject Service = JSONObject.parseObject(ServiceInstances);
            JSONArray InstanceList = Service.getJSONObject("value").getJSONArray("instanceList");
            List<InstanceInfo> list = InstanceList.toJavaList(InstanceInfo.class);//参数1为要转换的JSONArray数据，参数2为要转换的目标数据，即List盛装的数据
            serviceInfo.setInstanceList(list);
            List<InstanceInfo> reachedList = new ArrayList<>(list);
            reachedList.removeIf(info -> !info.isHealthy());
            serviceInfo.setHealthyInstanceList(reachedList);
            // 如果用户信息不为空，则加入缓存
            caffeineCache.put(key, serviceInfo);
        }
    }

    @Override
    public void addCacheInfo(ServiceInfo Info) {
        String key = Info.getGroup() + "@@" + Info.getServiceName();
        caffeineCache.put(key, Info);

    }

    //nocache
    public ServiceInfo getServiceNoCache(String serviceName,String group) throws Exception {
        log.info("从链上读取");
        ServiceInfo serviceInfo = new ServiceInfo(serviceName, group);
        String ServiceInstances=null;
        synchronized (mutex) {
            ServiceInstances = getInstanceList(serviceName, group);
        }
        JSONObject Service = JSONObject.parseObject(ServiceInstances);
        JSONArray InstanceList = Service.getJSONObject("value").getJSONArray("instanceList");
        List<InstanceInfo> list = InstanceList.toJavaList(InstanceInfo.class);//参数1为要转换的JSONArray数据，参数2为要转换的目标数据，即List盛装的数据
        serviceInfo.setInstanceList(list);
        List<InstanceInfo> reachedList = new ArrayList<>(list);
        reachedList.removeIf(info -> !info.isHealthy());
        serviceInfo.setHealthyInstanceList(reachedList);
        return serviceInfo;
    }

    @Override
    public ServiceInfo getCacheInfo(String serviceName, String group) throws Exception {
        String key = group + "@@" + serviceName;
        // 先从缓存读取
        caffeineCache.getIfPresent(key);
        ServiceInfo serviceInfo = (ServiceInfo) caffeineCache.asMap().get(String.valueOf(key));
        if (serviceInfo != null) {
            return serviceInfo;
        }
        if (!caffeineCache.asMap().containsKey(key)) {
            log.info("从链上读取");
            serviceInfo = new ServiceInfo(serviceName, group);
            String ServiceInstances=null;
            synchronized (mutex) {
                ServiceInstances = getInstanceList(serviceName, group);
            }
            JSONObject Service = JSONObject.parseObject(ServiceInstances);
            JSONArray InstanceList = Service.getJSONObject("value").getJSONArray("instanceList");
            List<InstanceInfo> list = InstanceList.toJavaList(InstanceInfo.class);//参数1为要转换的JSONArray数据，参数2为要转换的目标数据，即List盛装的数据
            serviceInfo.setInstanceList(list);
            List<InstanceInfo> reachedList = new ArrayList<>(list);
            reachedList.removeIf(info -> !info.isHealthy());
            serviceInfo.setHealthyInstanceList(reachedList);
            // 如果用户信息不为空，则加入缓存
            caffeineCache.put(key, serviceInfo);
            System.out.println(caffeineCache.asMap());
        } else {
            Thread.sleep(100);
            serviceInfo = getCacheInfo(serviceName, group);
        }
        //这里使用Lock，会导致如果线程因为数组越界等崩溃，Lock将无法释放，从而死锁
        // if (lock.tryLock()) {
        //     if (!caffeineCache.asMap().containsKey(key)) {
        //         log.info("从链上读取");
        //         serviceInfo = new ServiceInfo(serviceName, group);
        //         String ServiceInstances = getInstanceList(serviceName, group);
        //         JSONObject Service = JSONObject.parseObject(ServiceInstances);
        //         JSONArray InstanceList = Service.getJSONObject("value").getJSONArray("instanceList");
        //         List<InstanceInfo> list = InstanceList.toJavaList(InstanceInfo.class);//参数1为要转换的JSONArray数据，参数2为要转换的目标数据，即List盛装的数据
        //         serviceInfo.setInstanceList(list);
        //         List<InstanceInfo> reachedList = new ArrayList<>(list);
        //         reachedList.removeIf(info -> !info.isHealthy());
        //         serviceInfo.setHealthyInstanceList(reachedList);
        //         // 如果用户信息不为空，则加入缓存
        //         caffeineCache.put(key, serviceInfo);
        //         System.out.println(caffeineCache.asMap());
        //     }
        //     lock.unlock();
        // } else {
        //     Thread.sleep(100);
        //     serviceInfo = getCacheInfo(serviceName, group);
        // }
        return serviceInfo;
    }

    @Override
    public ServiceInfo getCacheInfo(String serviceName) throws Exception {
        return getCacheInfo(serviceName, null);
    }


    @Override
    public void deleteCache(String serviceName, String group) {
        String key = group + "@@" + serviceName;
        log.info("从缓存中删除" + key);
        // 从缓存中删除
        caffeineCache.asMap().remove(key);
    }

    private String getInstanceList(String serviceName, String group) throws Exception {
        if (group == null) {
            group = "DEFAULT_GROUP";
        }
        String channelName = "workflowchannel";
        String chainCodeName = "nacos";
        String func = "QueryByKey";
        ArrayList<String> params = new ArrayList<>();
        String key = "com.alibaba.nacos.naming.iplist.public##" + group + "@@" + serviceName;
        params.add(key);
        Collection<ProposalResponse> proposals = workflowFabric.query(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                log.info("response:::" + response.getMessage());
                log.info(new String(response.getChaincodeActionResponsePayload()));
                return new String(response.getChaincodeActionResponsePayload());
            } else {
                log.info("Query失败, reason: {}", response.getMessage());
            }
        }
        return null;
    }

    private String getServiceMetaData(String serviceName, String group) throws Exception {
        if (group == null) {
            group = "DEFAULT_GROUP";
        }
        String channelName = "workflowchannel";
        String chainCodeName = "nacos";
        String func = "QueryByKey";
        ArrayList<String> params = new ArrayList<>();
        String key = "com.alibaba.nacos.naming.domains.meta.public##" + group + "@@" + serviceName;
        params.add(key);
        Collection<ProposalResponse> proposals = workflowFabric.query(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                log.info("Query成功 Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
                log.info("response:::" + response.getMessage());
                return response.getMessage();
            } else {
                log.info("Query失败, reason: {}", response.getMessage());
            }
        }
        return null;
    }
}