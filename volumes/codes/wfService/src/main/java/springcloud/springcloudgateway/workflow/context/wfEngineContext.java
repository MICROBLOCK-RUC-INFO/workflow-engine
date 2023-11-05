package springcloud.springcloudgateway.workflow.context;

import springcloud.springcloudgateway.workflow.tools.distributedLock.distributedLock;
import springcloud.springcloudgateway.workflow.tools.distributedLock.redisLockImpl;

public class wfEngineContext {
    private static distributedLock lock;
    public static void init() {
        lock=new redisLockImpl();
    }
    
    public static distributedLock getLock() {
        return lock;
    }
    
}
