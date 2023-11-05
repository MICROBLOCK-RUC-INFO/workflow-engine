package springcloud.springcloudgateway.workflow.simulateCache;

import java.util.HashMap;
import java.util.Map;


import javax.annotation.Resource;

import org.activiti.engine.impl.db.workflowClass.workflowResponse;
import org.springframework.stereotype.Component;

import springcloud.springcloudgateway.workflow.helper.wfConfig;

@Component
public class preExecutionCache {
    private volatile Long lastFlushTime=0L;
    @Resource
    wfConfig wfConfig;
    public volatile Map<String,workflowResponse> preResultCache=new HashMap<String,workflowResponse>();


    private volatile int count=0;



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

    
    public Long getLastFlushTime() {
        return lastFlushTime;
    }


    public void setLastFlushTime(Long lastFlushTime) {
        this.lastFlushTime = lastFlushTime;
    }


    //深拷贝需要上链的内容，并返回上链数据和他对应的Oids
    public Map<String,workflowResponse> getPreDatas() {
        Map<String,workflowResponse> preDatas=new HashMap<String,workflowResponse>();
        //将需要上链的数据进行深拷贝并返回，防止长期占用锁
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

    private boolean isPreDatasCacheEmpty() {
        synchronized (preResultCache) {
            return preResultCache.isEmpty();
        }
    }

    //判定是否进行flush,距离上一次flush时间的间隔和preDataCache不为空
    public boolean isNeedFlush() {
        Long now=System.currentTimeMillis();
        if (now-lastFlushTime>=wfConfig.getFlushTimeInterval()&&(!isPreDatasCacheEmpty())) {
            return true;
        }
        return false;
    }
}
