package com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainCrud;
import com.alibaba.nacos.naming.misc.HttpClient;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhr
 */

@Deprecated
@Component
public class FabricCrud implements BlockchainCrud {
    private String port;
    private String host;

    private static String token;

    public void fabricGetToken() throws Exception {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("content-type", "application/x-www-form-urlencoded");
        HttpClient.HttpResult res = null;
        try {

            final String url = "http://" + host + ":" + port + "/admins";
            String data = "username=admin_cc_gfe&orgname=Gfe";
            res = HttpClient.fabricPostLarge(url, headers, data);
            JSONObject jsonObject = JSON.parseObject(res.content);
            token = jsonObject.getString("token");
        } catch (Exception e) {
            Loggers.RAFT.warn("Failed to request Enroll");
            throw e;
        }
//        token=res;
        Loggers.RAFT.info("token:" + token);
    }

    @Override
    public String fabricPut(String key, String value) throws Exception {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("authorization", "Bearer " + token);
        headers.put("content-type", "application/json");
        HttpClient.HttpResult res = null;
        try {

            final String url = "http://" + host + ":" + port + "/channels/softwarechannel/chaincodes/nacos";
            String data = "{" +
                    "\"peers\": [\"peer0.fabric.gfe.com\"],\n" +
                    "\t\"chaincodeName\":\"nacos\",\n" +
                    "\t\"chaincodeVersion\":\"v0\",\n" +
                    "\t\"chaincodeType\": \"go\",\n" +
                    "\t\"fcn\":\"Put\",\n" +
                    "\t\"args\":[\"" + key + "\",\"" + value + "\"]\n" +
                    "}";
            res = HttpClient.fabricPostLarge(url, headers, data);
        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to put to fabric : {}", key + value);
            throw e;
        }
        return res.content;

    }

    @Override
    public String fabricQueryByKey(String key) throws Exception {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("authorization", "Bearer " + token);
        headers.put("content-type", "application/json");
        HttpClient.HttpResult res = null;
        try {

            final String url = "http://" + host + ":" + port + "/channels/softwarechannel/chaincodes/nacos";
            String data = "{" +
                    "\"peers\": [\"peer0.fabric.gfe.com\"],\n" +
                    "\t\"chaincodeName\":\"nacos\",\n" +
                    "\t\"chaincodeVersion\":\"v0\",\n" +
                    "\t\"chaincodeType\": \"go\",\n" +
                    "\t\"fcn\":\"QueryByKey\",\n" +
                    "\t\"args\":[\"" + key + "\"]\n" +
                    "}";
            res = HttpClient.fabricPostLarge(url, headers, data);
        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to queryByKey in fabric : {}", key);
            throw e;
        }
        return res.content;
    }

    @Override
    public String fabricDelete(String key) throws Exception {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("authorization", "Bearer " + token);
        headers.put("content-type", "application/json");
        HttpClient.HttpResult res = null;
        try {

            final String url = "http://" + host + ":" + port + "/channels/softwarechannel/chaincodes/nacos";
            String data = "{" +
                    "\"peers\": [\"peer0.fabric.gfe.com\"],\n" +
                    "\t\"chaincodeName\":\"nacos\",\n" +
                    "\t\"chaincodeVersion\":\"v0\",\n" +
                    "\t\"chaincodeType\": \"go\",\n" +
                    "\t\"fcn\":\"Delete\",\n" +
                    "\t\"args\":[\"" + key + "\"]\n" +
                    "}";
            res = HttpClient.fabricPostLarge(url, headers, data);
        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to delete data in  fabric : {}", key);
            throw e;
        }
        return res.content;
    }

    @Override
    public String fabricQueryAllNamingData() throws Exception {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("authorization", "Bearer " + token);
        headers.put("content-type", "application/json");
        HttpClient.HttpResult res = null;
        try {

            final String url = "http://" + host + ":" + port + "/channels/softwarechannel/chaincodes/nacos";
            String data = "{" +
                    "\"peers\": [\"peer0.fabric.gfe.com\"],\n" +
                    "\t\"chaincodeName\":\"nacos\",\n" +
                    "\t\"chaincodeVersion\":\"v0\",\n" +
                    "\t\"chaincodeType\": \"go\",\n" +
                    "\t\"fcn\":\"RangeQuery\",\n" +
                    "\t\"args\":[\"com.alibaba.nacos.naming\",\"com.alibaba.nacos.naminh\"]\n" +
                    "}";
            res = HttpClient.fabricPostLarge(url, headers, data);
        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to queryAlldata in  fabric : {}");
            throw e;
        }
        return res.content;
    }

    private static String encodeFileName(String fileName) {
        return fileName.replace(':', '#');
    }

}
