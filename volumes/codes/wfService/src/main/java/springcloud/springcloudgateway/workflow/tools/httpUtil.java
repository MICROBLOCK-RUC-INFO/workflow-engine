package springcloud.springcloudgateway.workflow.tools;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;




//封装的httpclient连接池初始化，get请求与post请求
public class httpUtil {
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

    public static String uploadFile(String fileContent,String ChannelName) {
        try {
            new BasicClassicHttpRequest("POST",new HttpHost("127.0.0.1", 5001),"/api/v0/add?quieter=true");
            ClassicHttpRequest request=new BasicClassicHttpRequest("POST",new HttpHost("127.0.0.1", 5001),"/api/v0/add?quieter=true");
            HttpEntity entity=MultipartEntityBuilder.create().addBinaryBody("file",fileContent.getBytes(), 
                                ContentType.APPLICATION_OCTET_STREAM,ChannelName+"-"+UUID.randomUUID().toString()+".txt").build();

            request.setEntity(entity);
            

            CloseableHttpResponse response= syncClient.execute(request);
            InputStream resultInputStream=response.getEntity().getContent();
            String result=new String(resultInputStream.readAllBytes());
            resultInputStream.close();
            String hash=String.valueOf(jsonTransfer.jsonToMap(result).get("Hash"));
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }        
    }


    public static String getHashContent(String hash) {
        try {
            ClassicHttpRequest request=new BasicClassicHttpRequest("POST", new HttpHost("127.0.0.1", 5001),
                                    "/api/v0/cat?arg="+hash);
            CloseableHttpResponse response= syncClient.execute(request);
            InputStream resultInputStream=response.getEntity().getContent();
            String result=new String(resultInputStream.readAllBytes());
            resultInputStream.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Future<SimpleHttpResponse>> mutilGet(List<String> urls) throws InterruptedException, ExecutionException {
        List<Future<SimpleHttpResponse>> lists=new ArrayList<Future<SimpleHttpResponse>>();
        for (String url:urls) {
            lists.add(doGet(url));
        }
        return lists;
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

    public static List<Future<SimpleHttpResponse>> multiPost(Iterator<String> urls,String json_data) throws InterruptedException, ExecutionException {
        List<Future<SimpleHttpResponse>> futures=new ArrayList<Future<SimpleHttpResponse>>();
        while (urls.hasNext()) {
            futures.add(doPost(urls.next(), json_data));
        }
        return futures;
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

    public static boolean isPortOpen(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout); // 设置超时时间
            socket.close(); // 连接成功后立即关闭Socket
            return true; // 如果能够连接，则认为端口开放
        } catch (Exception e) {
            return false; // 如果发生异常，则认为端口不开放
        }
    }

}