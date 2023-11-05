package springcloud.springcloudgateway.service.utils;

import okhttp3.*;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class HttpClientUtil {
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(20000, 60L, TimeUnit.SECONDS))
            .connectTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).build();

    public static class HttpResult {
        final public int code;
        final public String content;
        final private Map<String, String> respHeaders;

        public HttpResult(int code, String content, Map<String, String> respHeaders) {
            this.code = code;
            this.content = content;
            this.respHeaders = respHeaders;
        }

        public String getHeader(String name) {
            return respHeaders.get(name);
        }
    }

    public HttpClientUtil() {
    }

    public static HttpResult fabricPostLarge(String url, Map<String, String> headers, String content) {
        HttpClientBuilder builder = HttpClients.custom();
        CloseableHttpClient httpClient = builder.build();
        HttpResponse response = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(30000).build();
            builder.setConnectionTimeToLive(2000, TimeUnit.MILLISECONDS);
            HttpPost httpost = new HttpPost(url);
            httpost.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpost.setHeader(entry.getKey(), entry.getValue());
            }
            httpost.setEntity(new StringEntity(content, ContentType.create("application/json", "UTF-8")));
            response = httpClient.execute(httpost);
            HttpEntity entity = response.getEntity();
            HeaderElement[] headerElements = entity.getContentType().getElements();
            String charset = headerElements[0].getParameterByName("charset").getValue();
            return new HttpResult(response.getStatusLine().getStatusCode(),
                    entity.getContent().toString(), Collections.<String, String>emptyMap());
        } catch (Exception e) {
            return new HttpResult(500, e.toString(), Collections.<String, String>emptyMap());
        }
    }

    public static Response redirectHttpRequest(String ipaddr, Map<String, String> params, String body, String method) throws IOException {
        Headers headers = Headers.of(params);
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = null;
        if (method.equals("GET")) {
            request = new Request.Builder().url(ipaddr).headers(headers).build();
        } else if (method.equals("DELETE")) {
            if (body == null) {
                request = new Request.Builder().url(ipaddr).delete().headers(headers).build();
            } else {
                RequestBody requestBody = RequestBody.create(mediaType, body);
                request = new Request.Builder().url(ipaddr).delete(requestBody).headers(headers).build();
            }
        } else {
            RequestBody requestBody = RequestBody.create(mediaType, body);
            request = new Request.Builder().url(ipaddr).method(method, requestBody).headers(headers).build();
        }
        Response response = okHttpClient.newCall(request).execute();
        return response;
    }

    public static Response httpRequest(String ipaddr, Map<String, String> params, String body, String method) throws IOException {
        Headers headers = Headers.of(params);
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = null;
        if (method.equals("GET")) {
            request = new Request.Builder().url(ipaddr).headers(headers).build();
        } else if (method.equals("DELETE")) {
            if (body == null) {
                request = new Request.Builder().url(ipaddr).delete().headers(headers).build();
            } else {
                RequestBody requestBody = RequestBody.create(mediaType, body);
                request = new Request.Builder().url(ipaddr).delete(requestBody).headers(headers).build();
            }
        } else {
            RequestBody requestBody = RequestBody.create(mediaType, body);
            request = new Request.Builder().url(ipaddr).method(method, requestBody).headers(headers).build();
        }
        Response response = okHttpClient.newCall(request).execute();
        return response;
    }

    /*
     * 工作流引擎用
     * 返回call
     */
    public static Call callHttpRequest(String ipaddr, String body) {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = null;
        RequestBody requestBody = RequestBody.create(mediaType, body);
        request = new Request.Builder().url(ipaddr).post(requestBody).build();
        return okHttpClient.newCall(request);
    }



    public static void asyncHttpRequest(String ipaddr, Map<String, String> params, String body, String method) throws IOException {
        Headers headers = Headers.of(params);
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = null;
        if (method.equals("GET")) {
            request = new Request.Builder().url(ipaddr).headers(headers).build();
        } else if (method.equals("DELETE")) {
            if (body == null) {
                request = new Request.Builder().url(ipaddr).delete().headers(headers).build();
            } else {
                RequestBody requestBody = RequestBody.create(mediaType, body);
                request = new Request.Builder().url(ipaddr).delete(requestBody).headers(headers).build();
            }
        } else {
            RequestBody requestBody = RequestBody.create(mediaType, body);
            request = new Request.Builder().url(ipaddr).method(method, requestBody).headers(headers).build();
        }
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });
    }

    public void asyncGetMasterNode(String server, Map<String, String> params, String body, String method, String data) throws IOException {
        String ipaddr = "http://" + server + "/grafana/monitor/isMaster";
        Headers headers = Headers.of(params);
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = null;
        if (method.equals("GET")) {
            request = new Request.Builder().url(ipaddr).headers(headers).build();
        } else if (method.equals("DELETE")) {
            if (body == null) {
                request = new Request.Builder().url(ipaddr).delete().headers(headers).build();
            } else {
                RequestBody requestBody = RequestBody.create(mediaType, body);
                request = new Request.Builder().url(ipaddr).delete(requestBody).headers(headers).build();
            }
        } else {
            RequestBody requestBody = RequestBody.create(mediaType, body);
            request = new Request.Builder().url(ipaddr).method(method, requestBody).headers(headers).build();
        }
        Call call = okHttpClient.newCall(request);
        call.enqueue(new GetMasterCallback(server, data));
    }

    public static HttpResponse couchDBPostLarge(String url, Map<String, String> headers, String content) {
        HttpClientBuilder builder = HttpClients.custom();
        CloseableHttpClient httpClient = builder.build();
        HttpResponse response = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(30000).build();
            builder.setConnectionTimeToLive(2000, TimeUnit.MILLISECONDS);
            HttpPost httpost = new HttpPost(url);
            httpost.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpost.setHeader(entry.getKey(), entry.getValue());
            }
            httpost.setEntity(new StringEntity(content, ContentType.create("application/json", "UTF-8")));
            response = httpClient.execute(httpost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static HttpResponse couchDBPostLarge(String url, Map<String, String> headers, String content, String Auth) {
        HttpClientBuilder builder = HttpClients.custom();
        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("AuthSession", Auth);
        cookieStore.addCookie(cookie);
        CloseableHttpClient httpClient = builder.setDefaultCookieStore(cookieStore).build();
        HttpResponse response = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(10000).build();
            builder.setConnectionTimeToLive(2000, TimeUnit.MILLISECONDS);
            HttpPost httpost = new HttpPost(url);
            httpost.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpost.setHeader(entry.getKey(), entry.getValue());
            }
            httpost.setEntity(new StringEntity(content, ContentType.create("application/json", "UTF-8")));
            httpost.setProtocolVersion(HttpVersion.HTTP_1_1);
            response = httpClient.execute(httpost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}

