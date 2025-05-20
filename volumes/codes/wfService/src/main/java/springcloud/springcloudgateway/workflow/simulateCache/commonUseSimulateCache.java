package springcloud.springcloudgateway.workflow.simulateCache;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import springcloud.springcloudgateway.workflow.helper.wfConfig;

/**
 * 2025/4/7
 * 缓存工作流引擎的模拟执行结果(不包括deploy,instance,complete)，隔一段时间统一上链提高性能
 *@apiNote commonUse表示的是通用的操作，之所以与deploy,instance,complete分开，是因为这三个由于返回的结果数据量大，都是
       缓存在本地缓存里的，http执行flush传递时是传递的key值(oid)。这里传递的就是返回的结果
 */
@Component
public class commonUseSimulateCache {
    private volatile Map<String,String> bindSimulateCache=new HashMap<String,String>();
    private volatile long bindLastFlushTime=0L;
    @Resource
    wfConfig wfConfig;
    /**
     * 缓存模拟执行结果
     * @param key
     * @param wrSet
     */
    public void bindSimulateStore(String key,String wrSet) {
        synchronized (bindSimulateCache) {
            bindSimulateCache.put(key,wrSet);
        }
    }
    //最近一次flush时间
    public long getBindLastFlushTime() {
        return bindLastFlushTime;
    }

    //设置最新flush时间
    public void setBindLastFlushTime(long bindLastFlushTime) {
        this.bindLastFlushTime = bindLastFlushTime;
    }

    //获得已缓存的模拟执行结果，并清空缓存
    public Map<String,String> getBindSimulateDatas() {
        Map<String,String> simulateDatas=new HashMap<>();
        synchronized (bindSimulateCache) {
            simulateDatas.putAll(bindSimulateCache);
            bindSimulateCache.clear();
        }
        return simulateDatas;
    }

    //缓存是否为空
    private boolean isBindSimulateCacheEmpty() {
        return bindSimulateCache.isEmpty();
    }

    //判断是否需要执行flush
    public boolean isBindNeedFlush() {
        long now=System.currentTimeMillis();
        //如果 现在时间 减去 最近一次flush时间的间隔大于等于设置的flush间隔时间 且缓存不为空 则返回true 表示需要flush
        if (now-bindLastFlushTime>=wfConfig.getFlushTimeInterval()&&(!isBindSimulateCacheEmpty())) {
            return true;
        }
        return false;
    }
}
