package springcloud.springcloudgateway.workflow.thread.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.activiti.engine.impl.db.workflowClass.workflowResponse;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import springcloud.springcloudgateway.workflow.tools.httpUtil;
import springcloud.springcloudgateway.workflow.tools.jsonTransfer;
import springcloud.springcloudgateway.workflow.userRequestResult.resForUsers;

public class flushConsumer implements Consumer<TransactionEvent>{

    private Map<String,workflowResponse> preDatas;
    private String oids;
    private List<String> peersIp;
    private boolean isTest;

    private final Logger logger=LoggerFactory.getLogger(flushConsumer.class);

    public flushConsumer(List<String> peersIp,Map<String, workflowResponse> preDatas,String oids,boolean isTest) {
        this.peersIp=peersIp;
        this.preDatas=preDatas;
        this.oids=oids;
        this.isTest=isTest;
    }

    
    @Override
    public void accept(TransactionEvent arg0) {
        // TODO Auto-generated method stub
        boolean vaild=arg0.isValid();


        //测试用
        // Iterator<workflowResponse> workflowResponses= preDatas.values().iterator();
        // Map<String,StringBuilder> serviceUrlToOids=new HashMap<>();
        // long flushStartTime=System.currentTimeMillis();
        // while (workflowResponses.hasNext()) {
        //     workflowResponse workflowResponse=workflowResponses.next();
        //     workflowResponse.setFlushStartTime(flushStartTime);
        //     List<String> SeviceUrls=workflowResponse.getServiceUrls();
        //     for (String serviceUrl:SeviceUrls) {
        //         serviceUrlToOids.computeIfAbsent(serviceUrl, k->new StringBuilder())
        //                                                 .append(workflowResponse.getOid())
        //                                                 .append('|');
        //     }
        // }
        
        try {
            Map<String,Object> data=new HashMap<String,Object>(){{
                put("oidsString", oids);
                put("vaild",vaild);
            }};
            List<Future<SimpleHttpResponse>> futures=new ArrayList<>();
            String jsonString=jsonTransfer.mapToJsonString(data);
            for (String ip:peersIp) {
                futures.add(httpUtil.doPost("http://"+ip+":8888/wfEngine/flush", jsonString));
            }
            //List<Future<SimpleHttpResponse>> releaseFutures=new ArrayList<>();
            // for(Entry<String,StringBuilder> entry:serviceUrlToOids.entrySet()) {
            //     String url=entry.getKey();
            //     String oids=entry.getValue().deleteCharAt(entry.getValue().length()-1).toString();
            //     Map<String,Object> jsonMap=new HashMap<String,Object>(){{
            //         put("oids",oids);
            //     }};
            //     Map<String,Object> postMap=new HashMap<>();
            //     postMap.put("s-consumerName","");
            //     postMap.put("s-serviceName",url);
            //     postMap.put("headers","{}");
            //     postMap.put("s-url","/unLock");
            //     postMap.put("s-method","POST");
            //     postMap.put("body",jsonTransfer.mapToJsonString(jsonMap));
            //     postMap.put("s-group","WORKFLOW");
            //     releaseFutures.add(httpUtil.doPost("http://127.0.0.1:8999/grafana/run?loadBalance=enabled", jsonTransfer.mapToJsonString(postMap)));
            // }
            // for (Future<SimpleHttpResponse> f:releaseFutures) {
            //     String res=f.get().getBodyText();
            //     logger.info(f.get().getHeaders().toString()+","+"releaseLock "+res);
            // }
            boolean success=true;
            int count=0;
            for (Future<SimpleHttpResponse> future:futures) {
                if (!future.get().getBodyText().equals("ok")) {
                    count--;
                    //logger.error("flush error oids:"+oids+" error:"+future.get().getBodyText());
                } else {
                    count++;
                    //这里差一个出错的处理方式
                    /*
                    
                    */
                }
            }
            if (count<=futures.size()/3) {
                success=false;
            }
            if (success) {
                long flushEndTime=System.currentTimeMillis();
                if (isTest) {
                    StringBuilder sb=new StringBuilder();
                    for (workflowResponse response:preDatas.values()) {
                        response.setFlushEndTime(flushEndTime);
                        sb.append(jsonTransfer.mapToJsonString(response.getViewMap())).append('|');
                    }
                    sb.deleteCharAt(sb.length()-1);
                    data.put("oidsResponse",sb.toString());
                    httpUtil.doPost("http://10.77.110.222:9988/informResponses/true",jsonTransfer.mapToJsonString(data));
                    // sb.append("http://10.77.70.124:8080/informResponse/");
                    // int length=sb.length();
                    // for (String oid:preDatas.keySet()) {
                    //     sb.append(oid).append('/').append(URLEncoder.encode(preDatas.get(oid).getViewString(),"UTF-8"))
                    //       .append('/').append(true);
                    //     httpUtil.doGet(sb.toString());
                    //     sb.setLength(length);
                    // }
                } else {
                    for (workflowResponse response:preDatas.values()) {
                        response.setFlushEndTime(flushEndTime);
                    }
                    resForUsers.addSuccessRes(preDatas);
                }
            }
            logger.info("wfRequest flush success");
        } catch (Exception e) {
            logger.error("error in flush Activiti Status");
            System.out.println(e.getMessage());
        }
        //如果为true,说明写块成功
        // flushActivitiStatus flush=new flushActivitiStatus(peersIp, preDatas, oids, isTest,arg0.isValid());
        // activitiChangeExecutor.flush(flush);
        
    }

    
}
