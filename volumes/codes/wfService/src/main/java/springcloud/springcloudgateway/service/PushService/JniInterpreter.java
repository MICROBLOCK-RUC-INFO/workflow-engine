package springcloud.springcloudgateway.service.PushService;

import jep.Interpreter;
import jep.JepConfig;
import jep.SubInterpreter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: 李浩然
 * @Date: 2021/10/11 1:35 下午
 */
public class JniInterpreter {
    //    public static JepConfig config = new JepConfig().addIncludePaths("/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/fabric-nodejs/fabric-deploy/peer-GMN/model");
    public static JepConfig config = new JepConfig().addIncludePaths("/model");
    private static Interpreter clusterInterp;
    public static ExecutorService jniThreadPool = Executors.newSingleThreadExecutor();
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private JniInterpreter() {
    }

    static class initRunnable implements Runnable {
        private CountDownLatch countDownLatch;

        public initRunnable(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            clusterInterp = new SubInterpreter(config);
            clusterInterp.eval("from scalable_support_vector_clustering import *");
            countDownLatch.countDown();
        }
    }


    public static Interpreter getClusterInterpreter() {
        try {
            if (clusterInterp == null) {
                if (clusterInterp == null) {
                    jniThreadPool.submit(new initRunnable(countDownLatch));
                    countDownLatch.await();
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return clusterInterp;
    }


}

