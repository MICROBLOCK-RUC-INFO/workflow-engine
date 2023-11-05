package springcloud.springcloudgateway.workflow.thread.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import springcloud.springcloudgateway.workflow.tools.httpUtil;
import springcloud.springcloudgateway.workflow.userRequestResult.commonUseResult;

public class commonUseFlushConsumer implements Consumer<TransactionEvent>{
    private List<List<String>> args;
    private List<String> flushUrls;
    private int index=0;

    private final Logger logger=LoggerFactory.getLogger(commonUseFlushConsumer.class);

    public commonUseFlushConsumer(Collection<String> peersIp,List<List<String>> args) {
        this.args=args;
        this.flushUrls=peersIp.stream().map(new Function<String,String>() {
                            @Override
                            public String apply(String s) {
                                return "http://"+s+":8888/wfEngine/bindFlush";
                            }
                        }).collect(Collectors.toList());
    }

    
    @Override
    public void accept(TransactionEvent arg0) {
        if (!arg0.isValid()) return;
        List<Pair<Pair<String,String>,List<Future<SimpleHttpResponse>>>> allResult=new ArrayList<>();
        args.get(1).forEach(rwSet -> {
            try {
                allResult.add(Pair.of(Pair.of(args.get(0).get(index++),rwSet),httpUtil.multiPost(flushUrls.iterator(), rwSet)));
            } catch (Exception e) {
                logger.warn("commonUse flush http request error, rwSet "+rwSet+" ,cause by "+e.getMessage());
            }
        });
        allResult.forEach(resultPair -> {
            try {
                int count=0;
                for (Future<SimpleHttpResponse> future:resultPair.getValue()) {
                    if (future.get().getBodyText().equals("ok")) count++;
                    else count--;
                }
                if (count<=resultPair.getValue().size()/3) {
                    commonUseResult.addResult(resultPair.getKey().getKey(),"{\"code\":500,\"body\":\"flush error\"}");
                    logger.warn("flush error,cause by same results less than 2/3");
                    //失败的操作
                } else {
                    commonUseResult.addResult(resultPair.getKey().getKey(), "{\"code\":200,\"body\":\"rwSet: "+resultPair.getKey().getValue()+" flush success\"}");
                    //成功的操作
                }
            } catch (Exception e) {
                logger.warn("commonUse flush request future.get() error, oid "+resultPair.getKey().getKey()+" ,cause by "+e.getMessage());
            }
        });
    }
}
