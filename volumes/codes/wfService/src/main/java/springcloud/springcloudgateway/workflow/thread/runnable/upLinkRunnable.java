package springcloud.springcloudgateway.workflow.thread.runnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.activiti.engine.impl.db.workflowClass.workflowResponse;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import springcloud.springcloudgateway.workflow.helper.workflowFabric;
import springcloud.springcloudgateway.workflow.thread.consumer.flushConsumer;
import springcloud.springcloudgateway.workflow.threadExecutor.activitiChangeExecutor;

public class upLinkRunnable implements Runnable{
    private workflowFabric workflowfabric;
    private String channelName;
    private String chaincodeName;
    private String fcn;
    private ArrayList<String> args;
    private Map<String, workflowResponse> preDatas;
    private activitiChangeExecutor activitiChangeExecutor;
    private boolean isTest;
    private final Logger logger=LoggerFactory.getLogger(upLinkRunnable.class);

    public upLinkRunnable(workflowFabric workflowFabric,String channelName,String chaincodeName,String fcn,ArrayList<String> args,Map<String,workflowResponse> preDatas,activitiChangeExecutor activitiChangeExecutor) {
        this.workflowfabric=workflowFabric;
        this.channelName=channelName;
        this.chaincodeName=chaincodeName;
        this.fcn=fcn;
        this.args=args;
        this.preDatas=preDatas;
        this.activitiChangeExecutor=activitiChangeExecutor;
        this.isTest=false;
    }

    public upLinkRunnable(workflowFabric workflowFabric,String channelName,String chaincodeName,String fcn,ArrayList<String> args,Map<String,workflowResponse> preDatas,activitiChangeExecutor activitiChangeExecutor,boolean isTest) {
        this.workflowfabric=workflowFabric;
        this.channelName=channelName;
        this.chaincodeName=chaincodeName;
        this.fcn=fcn;
        this.args=args;
        this.preDatas=preDatas;
        //测试用
        Iterator<workflowResponse> workflowResponses= preDatas.values().iterator();
        long time=System.currentTimeMillis();
        while (workflowResponses.hasNext()) {
            workflowResponses.next().setStartPutToBlockChain(time);
        }
        this.activitiChangeExecutor=activitiChangeExecutor;
        this.isTest=isTest;
    }
    @Override
    public void run() {
        boolean success=true;
        CompletableFuture<TransactionEvent> orderServiceRes= workflowfabric.workflowInvoke(channelName, chaincodeName, fcn, args);
        //为null说明链码执行结果无效
        if (orderServiceRes==null) {
            success=false;
        }
        if (success) {
            Consumer<TransactionEvent> flushConsumer=new flushConsumer(workflowfabric.getPeersIp(channelName), preDatas, args.get(0), isTest);
            orderServiceRes.thenAcceptAsync(flushConsumer,activitiChangeExecutor.getExecutor());
        }
        logger.info("wfRequest upLink success");
    }
}
