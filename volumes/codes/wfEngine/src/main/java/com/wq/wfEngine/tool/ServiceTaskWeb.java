package com.wq.wfEngine.tool;

import java.net.ConnectException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.HttpHostConnectException;

public class ServiceTaskWeb {
    public static Future<SimpleHttpResponse> doPost(String url,String jsonString) {
        try {
            Future<SimpleHttpResponse> future=Connect.doPost(url, jsonString);
            return future;
        } catch (Exception e) {
            return null;
        }
    }
}
