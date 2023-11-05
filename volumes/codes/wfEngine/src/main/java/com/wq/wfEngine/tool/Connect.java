package com.wq.wfEngine.tool;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.wq.wfEngine.entity.NetWorkRes;
import com.wq.wfEngine.entity.WfTask;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ContentType;
import org.activiti.engine.impl.db.workflowClass.proto.bigPackage.wfPackage.bigPackage;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

//封装的httpclient连接池初始化，get请求与post请求
public class Connect {
    private static CloseableHttpAsyncClient client = null;
    private static CloseableHttpClient syncClient=null;
    static {

        PoolingHttpClientConnectionManager syncPaccm=new PoolingHttpClientConnectionManager();
        syncPaccm.setMaxTotal(200);
        syncPaccm.setDefaultMaxPerRoute(10);
        syncClient=HttpClients.custom().setConnectionManager(syncPaccm).setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy()).build();

        PoolingAsyncClientConnectionManager paccm = new PoolingAsyncClientConnectionManager();
        paccm.setMaxTotal(10000);
        paccm.setDefaultMaxPerRoute(400);
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSoTimeout(Timeout.ofSeconds(20)).build();// 设置超时20秒
        client = HttpAsyncClients.custom().setIOReactorConfig(ioReactorConfig).setConnectionManager(paccm).setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy()).build();
    }



    // get方法
    public static Future<SimpleHttpResponse> doGet(String url)
            throws InterruptedException, ExecutionException {

        client.start();
        SimpleHttpRequest request = SimpleHttpRequest.create("GET", url);// 创建一个get请求

        Future<SimpleHttpResponse> future = client.execute(request, new FutureCallback<SimpleHttpResponse>() {

            @Override
            public void completed(final SimpleHttpResponse response) {
            }

            @Override
            public void failed(final Exception ex) {
                System.out.println(request + "->" + ex);
            }

            @Override
            public void cancelled() {
            }

        });
        return future;
    }

    public static bigPackage getHashContent(String hash) {
        try {
            ClassicHttpRequest request=new BasicClassicHttpRequest("POST", new HttpHost("127.0.0.1", 5001),"/api/v0/cat?arg="+hash);
            CloseableHttpResponse response= syncClient.execute(request);
            InputStream resultInputStream=response.getEntity().getContent();
            String result=new String(resultInputStream.readAllBytes());
            resultInputStream.close();
            //System.out.println("catResult:"+result);
            bigPackage bPackage=bigPackage.parseFrom(result.getBytes("ISO-8859-1"));
            return bPackage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // post方法
    public static Future<SimpleHttpResponse> doPost(String url,String json_data)
            throws InterruptedException, ExecutionException {

        client.start();
        SimpleHttpRequest request = SimpleHttpRequest.create("POST", url);

        request.setBody(json_data, ContentType.APPLICATION_JSON);

        Future<SimpleHttpResponse> future = client.execute(request, new FutureCallback<SimpleHttpResponse>() {

            @Override
            public void completed(final SimpleHttpResponse response) {
            }

            @Override
            public void failed(final Exception ex) {
                System.out.println(request + "->" + ex);
            }

            @Override
            public void cancelled() {
            }

        });
        return future;
    }
}