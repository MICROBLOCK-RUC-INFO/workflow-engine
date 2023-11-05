package springcloud.springcloudgateway.workflow.simulateCache;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import springcloud.springcloudgateway.workflow.helper.wfConfig;

@Component
public class commonUseSimulateCache {
    private volatile Map<String,String> bindSimulateCache=new HashMap<String,String>();
    private volatile long bindLastFlushTime=0L;
    @Resource
    wfConfig wfConfig;
    /**
     * @apiNote commonUse表示的是通用的操作，之所以与deploy,instance,complete分开，是因为这三个由于读写集太大，都是
     *          缓存在本地缓存里的，http执行flush传递时是传递的key值，这里传递的就是返回的读写集
     * @param oid
     * @param wrSet
     */
    public void bindSimulateStore(String key,String wrSet) {
        synchronized (bindSimulateCache) {
            bindSimulateCache.put(key,wrSet);
        }
    }
    public long getBindLastFlushTime() {
        return bindLastFlushTime;
    }

    public void setBindLastFlushTime(long bindLastFlushTime) {
        this.bindLastFlushTime = bindLastFlushTime;
    }

    public Map<String,String> getBindSimulateDatas() {
        Map<String,String> simulateDatas=new HashMap<>();
        synchronized (bindSimulateCache) {
            simulateDatas.putAll(bindSimulateCache);
            bindSimulateCache.clear();
        }
        return simulateDatas;
    }

    private boolean isBindSimulateCacheEmpty() {
        return bindSimulateCache.isEmpty();
    }

    public boolean isBindNeedFlush() {
        long now=System.currentTimeMillis();
        if (now-bindLastFlushTime>=wfConfig.getFlushTimeInterval()&&(!isBindSimulateCacheEmpty())) {
            return true;
        }
        return false;
    }
}
