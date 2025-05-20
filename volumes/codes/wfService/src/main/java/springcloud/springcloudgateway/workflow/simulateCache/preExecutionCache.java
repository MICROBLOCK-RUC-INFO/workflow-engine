package springcloud.springcloudgateway.workflow.simulateCache;

import java.util.HashMap;
import java.util.Map;


import javax.annotation.Resource;

import org.activiti.engine.impl.db.workflowClass.workflowResponse;
import org.springframework.stereotype.Component;

import springcloud.springcloudgateway.workflow.helper.wfConfig;

/**
 * 2025/4/7
 * 缓存deploy,instance,complete的工作流引擎模拟执行结果，隔一段时间统一上链提高性能
 */
@Component
public class preExecutionCache {
    private volatile Long lastFlushTime=0L;
    @Resource
    wfConfig wfConfig;
    public volatile Map<String,workflowResponse> preResultCache=new HashMap<String,workflowResponse>();


    private volatile int count=0;


    //缓存模拟执行结果
    public void putPreDataToCache(workflowResponse workflowResponse,String Oid) {
        synchronized (preResultCache) {
            preResultCache.put(Oid,workflowResponse);
            // count++;
            // if (count>=200) {
            //     testRunable test=new testRunable(wfEngine, preExecutionCache);
            //     Thread thread=new Thread(test);
            //     thread.start();
            //     count=0;
            // }
        }
    }

    //最近一次flush时间
    public Long getLastFlushTime() {
        return lastFlushTime;
    }

    //设置最新flush时间
    public void setLastFlushTime(Long lastFlushTime) {
        this.lastFlushTime = lastFlushTime;
    }


    //获得已缓存的模拟执行结果，并清空缓存
    public Map<String,workflowResponse> getPreDatas() {
        Map<String,workflowResponse> preDatas=new HashMap<String,workflowResponse>();
        synchronized (preResultCache) {
            preDatas.putAll(preResultCache);
            preResultCache.clear();
        }
        return preDatas;
    }


    //暂时弃用
    // public workflowResponse getReturnData(String Oid) {
    //     synchronized (preResultCache) {
    //         return preResultCache.remove(Oid);
    //     }
    // }

    //缓存是否为空
    private boolean isPreDatasCacheEmpty() {
        synchronized (preResultCache) {
            return preResultCache.isEmpty();
        }
    }

    //判定是否进行flush
    public boolean isNeedFlush() {
        Long now=System.currentTimeMillis();
         //如果 现在时间 减去 最近一次flush时间的间隔大于等于设置的flush间隔时间 且缓存不为空 则返回true 表示需要flush
        if (now-lastFlushTime>=wfConfig.getFlushTimeInterval()&&(!isPreDatasCacheEmpty())) {
            return true;
        }
        return false;
    }
}
