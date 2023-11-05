/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.naming.consistency.persistent.blockchain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.naming.consistency.ApplyAction;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.KeyBuilder;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric.FabricCrudBySdk;
import com.alibaba.nacos.naming.core.Instance;
import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;


/**
 * @author nacos
 */
@Component
public class BlockchainStore {

    private Properties meta = new Properties();

    private String metaFileName = UtilsAndCommons.DATA_BASE_DIR + File.separator + "meta.properties";

    private String cacheDir = UtilsAndCommons.DATA_BASE_DIR + File.separator + "data";

    @Autowired
    private FabricCrudBySdk fabricCrud;


    public synchronized void loadDatums(BlockchainCore.Notifier notifier, ConcurrentMap<String, Datum> datums) throws Exception {

        Datum datum;
        long start = System.currentTimeMillis();
        for (String cache : listCaches()) {
            datum = readDatum(cache);
            Loggers.RAFT.info("cache is:" + cache);
            if (datum != null) {
                Loggers.RAFT.info("datum is:" + datum.value.toString());
                datums.put(datum.key, datum);
                Loggers.RAFT.info("DDDD" + datums.values().toString());
                notifier.addTask(datum.key, ApplyAction.CHANGE);
            }

        }
        Loggers.RAFT.info("finish loading all datums, size: {} cost {} ms.", datums.size(), (System.currentTimeMillis() - start));
    }


    public synchronized Datum load(String key) throws Exception {
        long start = System.currentTimeMillis();
        // load data
        Loggers.RAFT.info("load key is" + key);
        String data = fabricCrud.fabricQueryByKey(key);
        Loggers.RAFT.info("load data is " + data);
        Loggers.RAFT.info("finish loading datum, key: {} cost {} ms.",
                key, (System.currentTimeMillis() - start));
        return readDatumByKey(key, data);
    }

    public synchronized Datum readDatumByKey(String namingKey, String json) throws IOException {
        try {
            if (StringUtils.isBlank(json)) {
                return null;
            }
            Loggers.RAFT.info("read by key old: " + json);
            Loggers.RAFT.info("read by key new: " + json);
            if (KeyBuilder.matchSwitchKey(namingKey)) {
                return JSON.parseObject(json, new TypeReference<Datum<SwitchDomain>>() {
                });
            }

            if (KeyBuilder.matchServiceMetaKey(namingKey)) {

                Datum<Service> serviceDatum;

                try {
                    serviceDatum = JSON.parseObject(json.replace("\\", ""), new TypeReference<Datum<Service>>() {
                    });
                } catch (Exception e) {
                    JSONObject jsonObject = JSON.parseObject(json);

                    serviceDatum = new Datum<>();
                    serviceDatum.timestamp.set(jsonObject.getLongValue("timestamp"));
                    serviceDatum.key = jsonObject.getString("key");
                    serviceDatum.value = JSON.parseObject(jsonObject.getString("value"), Service.class);
                }

                if (StringUtils.isBlank(serviceDatum.value.getGroupName())) {
                    serviceDatum.value.setGroupName(Constants.DEFAULT_GROUP);
                }
                if (!serviceDatum.value.getName().contains(Constants.SERVICE_INFO_SPLITER)) {
                    serviceDatum.value.setName(Constants.DEFAULT_GROUP
                            + Constants.SERVICE_INFO_SPLITER + serviceDatum.value.getName());
                }

                return serviceDatum;
            }

            if (KeyBuilder.matchInstanceListKey(namingKey)) {

                Datum<Instances> instancesDatum;

                try {
                    instancesDatum = JSON.parseObject(json, new TypeReference<Datum<Instances>>() {
                    });
                } catch (Exception e) {
                    JSONObject jsonObject = JSON.parseObject(json);
                    instancesDatum = new Datum<>();
                    instancesDatum.timestamp.set(jsonObject.getLongValue("timestamp"));

                    String key = jsonObject.getString("key");
                    String serviceName = KeyBuilder.getServiceName(key);
                    key = key.substring(0, key.indexOf(serviceName)) +
                            Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName;

                    instancesDatum.key = key;
                    instancesDatum.value = new Instances();
                    instancesDatum.value.setInstanceList(JSON.parseObject(jsonObject.getString("value"),
                            new TypeReference<List<Instance>>() {
                            }));
                    if (!instancesDatum.value.getInstanceList().isEmpty()) {
                        for (Instance instance : instancesDatum.value.getInstanceList()) {
                            instance.setEphemeral(false);
                        }
                    }
                }

                return instancesDatum;
            }

            return JSON.parseObject(json, Datum.class);

        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to deserialize : {}", json);
            throw e;
        }
    }

    public synchronized Datum readDatum(String jsondata) throws IOException {
        try {
            if (StringUtils.isBlank(jsondata)) {
                return null;
            }
            Loggers.RAFT.info("old: " + jsondata);
            JSONObject jsonObject1 = JSON.parseObject(jsondata);
            String json = jsonObject1.getString("Record");
            Loggers.RAFT.info("new: " + json);
            String namingKey = jsonObject1.getString("Key");
            if (KeyBuilder.matchSwitchKey(namingKey)) {
                return JSON.parseObject(json, new TypeReference<Datum<SwitchDomain>>() {
                });
            }

            if (KeyBuilder.matchServiceMetaKey(namingKey)) {

                Datum<Service> serviceDatum;

                try {
                    serviceDatum = JSON.parseObject(json.replace("\\", ""), new TypeReference<Datum<Service>>() {
                    });
                } catch (Exception e) {
                    JSONObject jsonObject = JSON.parseObject(json);

                    serviceDatum = new Datum<>();
                    serviceDatum.timestamp.set(jsonObject.getLongValue("timestamp"));
                    serviceDatum.key = jsonObject.getString("key");
                    serviceDatum.value = JSON.parseObject(jsonObject.getString("value"), Service.class);
                }

                if (StringUtils.isBlank(serviceDatum.value.getGroupName())) {
                    serviceDatum.value.setGroupName(Constants.DEFAULT_GROUP);
                }
                if (!serviceDatum.value.getName().contains(Constants.SERVICE_INFO_SPLITER)) {
                    serviceDatum.value.setName(Constants.DEFAULT_GROUP
                            + Constants.SERVICE_INFO_SPLITER + serviceDatum.value.getName());
                }

                return serviceDatum;
            }

            if (KeyBuilder.matchInstanceListKey(namingKey)) {

                Datum<Instances> instancesDatum;

                try {
                    instancesDatum = JSON.parseObject(json, new TypeReference<Datum<Instances>>() {
                    });
                } catch (Exception e) {
                    JSONObject jsonObject = JSON.parseObject(json);
                    instancesDatum = new Datum<>();
                    instancesDatum.timestamp.set(jsonObject.getLongValue("timestamp"));

                    String key = jsonObject.getString("key");
                    String serviceName = KeyBuilder.getServiceName(key);
                    key = key.substring(0, key.indexOf(serviceName)) +
                            Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName;

                    instancesDatum.key = key;
                    instancesDatum.value = new Instances();
                    instancesDatum.value.setInstanceList(JSON.parseObject(jsonObject.getString("value"),
                            new TypeReference<List<Instance>>() {
                            }));
                    if (!instancesDatum.value.getInstanceList().isEmpty()) {
                        for (Instance instance : instancesDatum.value.getInstanceList()) {
                            instance.setEphemeral(false);
                        }
                    }
                }

                return instancesDatum;
            }

            return JSON.parseObject(json, Datum.class);

        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to deserialize : {}", jsondata);
            throw e;
        }

    }

    public synchronized void write(final Datum datum) throws Exception {

        try {
            ByteBuffer dataPre;
            String key = encodeFileName(datum.key);
            dataPre = ByteBuffer.wrap(JSON.toJSONString(datum).getBytes(StandardCharsets.UTF_8));
            String data = new String(dataPre.array(), StandardCharsets.UTF_8);
            Loggers.RAFT.info(fabricCrud.fabricPut(key, data));
        } catch (Exception e) {
            Loggers.RAFT.error("write failed" + datum.key);
            throw e;
        }
    }

    private String[] listCaches() throws Exception {
        String str = fabricCrud.fabricQueryAllNamingData();
        String res = str.replaceAll("^(\\[)", "");
        res = res.replaceAll("(\\])$", "");
        String[] s = res.split("(?<=}),(?=\\{\"Key\":\"com\\.alibaba\\.nacos\\.naming)");
        return s;
    }

    public void delete(String key) throws Exception {
        // datum key contains namespace info:
        key = encodeFileName(key);
        Loggers.RAFT.info(fabricCrud.fabricDelete(key));
    }

    private static String encodeFileName(String fileName) {
        return fileName.replace(':', '#');
    }

    private static String decodeFileName(String fileName) {
        return fileName.replace("#", ":");
    }

}
