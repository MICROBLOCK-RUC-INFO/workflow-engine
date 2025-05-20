package springcloud.springcloudgateway.workflow.thread.runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import springcloud.springcloudgateway.workflow.helper.workflowFabric;
import springcloud.springcloudgateway.workflow.thread.consumer.commonUseFlushConsumer;
import springcloud.springcloudgateway.workflow.threadExecutor.activitiChangeExecutor;

/**
 * 2025/4/10
 * 上链
 */
public class commonUseUpLinkRunnable implements Runnable{
    private workflowFabric workflowfabric;
    private String channelName;
    private String chaincodeName;
    //链码对应的功能。根据自己的链码填写
    private String fcn;
    private ArrayList<List<String>> args;
    //线程池
    private activitiChangeExecutor activitiChangeExecutor;
    private final Logger logger=LoggerFactory.getLogger(commonUseUpLinkRunnable.class);


    public commonUseUpLinkRunnable(workflowFabric workflowFabric,String channelName,String chaincodeName,
                                    String fcn,ArrayList<List<String>> args,activitiChangeExecutor activitiChangeExecutor) {
        this.workflowfabric=workflowFabric;
        this.channelName=channelName;
        this.chaincodeName=chaincodeName;
        this.fcn=fcn;
        this.args=args;
        this.activitiChangeExecutor=activitiChangeExecutor;
    }

    @Override
    public void run() {
        boolean success=true;
        //上链
        CompletableFuture<TransactionEvent> orderServiceRes= workflowfabric.workflowInvoke(channelName, chaincodeName
                                                                            , fcn, new ArrayList<String>(){{
                                                                                add(JSON.toJSONString(args.get(0)));
                                                                                add(JSON.toJSONString(args.get(1)));
                                                                            }});
        //为null说明链码执行结果无效
        if (orderServiceRes==null) {
            success=false;
            logger.warn("commonUse UpLink failed");
        }
        if (success) {
            /**
             * 创建flush线程，如果上链成功，则将TransactionEvent 作为flush线程的参数，交给activitiChangeExecutor线程池执行
             */
            Consumer<TransactionEvent> consumer=new commonUseFlushConsumer(workflowfabric.getPeersIp(channelName),args);
            orderServiceRes.thenAcceptAsync(consumer,activitiChangeExecutor.getExecutor());
            logger.info("commonUse UpLink success");
        }
    }
}
