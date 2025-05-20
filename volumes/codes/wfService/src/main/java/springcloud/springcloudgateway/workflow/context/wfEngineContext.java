package springcloud.springcloudgateway.workflow.context;

import springcloud.springcloudgateway.workflow.tools.distributedLock.distributedLock;
import springcloud.springcloudgateway.workflow.tools.distributedLock.redisLockImpl;

/*
 * 2025/4/1
 * 后续想用来做上下文管理，将一些可复用的类比如对Fabric的访问，线程等等
 * 但是东西太多了，还没来得及
 */
public class wfEngineContext {
    private static distributedLock lock;
    public static void init() {
        /*
         * 这里就放所有对象的初始化
         */
        //lock=new redisLockImpl();
    }
    
    // public static distributedLock getLock() {
    //     return lock;
    // }
    
}
