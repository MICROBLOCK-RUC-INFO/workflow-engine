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
package com.alibaba.nacos.naming.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.core.utils.WebUtils;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.KeyBuilder;
import com.alibaba.nacos.naming.consistency.RecordListener;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainConsistencyServiceImpl;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainCore;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainPeer;
import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.core.ServiceManager;
import com.alibaba.nacos.naming.misc.NetUtils;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.web.NeedAuth;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Methods for Raft consistency protocol. These methods should only be invoked by Nacos server itself.
 *
 * @author nkorange
 * @since 1.0.0
 */
@RestController
@RequestMapping({UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft",
        UtilsAndCommons.NACOS_SERVER_CONTEXT + UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft"})
public class BlockChainController {

    @Autowired
    private BlockchainConsistencyServiceImpl blockchainConsistencyService;

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private BlockchainCore blockchainCore;


    @NeedAuth
    @GetMapping("/peer")
    public JSONObject getPeer(HttpServletRequest request, HttpServletResponse response) {
        List<BlockchainPeer> peers = blockchainCore.getPeers();
        BlockchainPeer peer = null;

        for (BlockchainPeer peer1 : peers) {
            if (StringUtils.equals(peer1.ip, NetUtils.localServer())) {
                peer = peer1;
            }
        }

        if (peer == null) {
            peer = new BlockchainPeer();
            peer.ip = NetUtils.localServer();
        }

        return JSON.parseObject(JSON.toJSONString(peer));
    }

    @NeedAuth
    @PutMapping("/datum/reload")
    public String reloadDatum(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String key = WebUtils.required(request, "key");
        blockchainCore.loadDatum(key);
        return "ok";
    }

    @NeedAuth
    @PostMapping("/datum")
    public String publish(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Type", "application/json; charset=" + getAcceptEncoding(request));
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Encode", "gzip");

        String entity = IoUtils.toString(request.getInputStream(), "UTF-8");
        String value = URLDecoder.decode(entity, "UTF-8");
        JSONObject json = JSON.parseObject(value);

        String key = json.getString("key");
        if (KeyBuilder.matchInstanceListKey(key)) {
            blockchainConsistencyService.put(key, JSON.parseObject(json.getString("value"), Instances.class));
            return "ok";
        }

        if (KeyBuilder.matchSwitchKey(key)) {
            blockchainConsistencyService.put(key, JSON.parseObject(json.getString("value"), SwitchDomain.class));
            return "ok";
        }

        if (KeyBuilder.matchServiceMetaKey(key)) {
            blockchainConsistencyService.put(key, JSON.parseObject(json.getString("value"), Service.class));
            return "ok";
        }

        throw new NacosException(NacosException.INVALID_PARAM, "unknown type publish key: " + key);
    }

    @NeedAuth
    @DeleteMapping("/datum")
    public String delete(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Type", "application/json; charset=" + getAcceptEncoding(request));
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Encode", "gzip");
        blockchainConsistencyService.remove(WebUtils.required(request, "key"));
        return "ok";
    }

    @NeedAuth
    @GetMapping("/datum")
    public String get(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Type", "application/json; charset=" + getAcceptEncoding(request));
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Encode", "gzip");
        String keysString = WebUtils.required(request, "keys");
        keysString = URLDecoder.decode(keysString, "UTF-8");
        String[] keys = keysString.split(",");
        List<Datum> datums = new ArrayList<Datum>();

        for (String key : keys) {
            Datum datum = blockchainCore.getDatum(key);
            datums.add(datum);
        }

        return JSON.toJSONString(datums);
    }

    @PostMapping("/loadAllDatums")
    public String loadAllDatumsFromBlockchain() throws Exception {
        return blockchainCore.fabricCrud.fabricQueryAllNamingData();
    }

    @PostMapping("/loadDatum")
    public String loadDatumFromBlockchain(@RequestParam(value = "key", required = true) String key) throws Exception {
        return blockchainCore.fabricCrud.fabricQueryByKey(key);
    }

    @GetMapping("/state")
    public JSONObject state(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Type", "application/json; charset=" + getAcceptEncoding(request));
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Encode", "gzip");

        JSONObject result = new JSONObject();
        result.put("services", serviceManager.getServiceCount());
        result.put("peers", blockchainCore.getPeers());

        return result;
    }

    @NeedAuth
    @PostMapping("/datum/commit")
    public String onPublish(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Type", "application/json; charset=" + getAcceptEncoding(request));
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Encode", "gzip");

        String entity = IoUtils.toString(request.getInputStream(), "UTF-8");
        String value = URLDecoder.decode(entity, "UTF-8");
        JSONObject jsonObject = JSON.parseObject(value);
        String key = "key";

        BlockchainPeer source = JSON.parseObject(jsonObject.getString("source"), BlockchainPeer.class);
        JSONObject datumJson = jsonObject.getJSONObject("datum");

        Datum datum = null;
        if (KeyBuilder.matchInstanceListKey(datumJson.getString(key))) {
            datum = JSON.parseObject(jsonObject.getString("datum"), new TypeReference<Datum<Instances>>() {
            });
        } else if (KeyBuilder.matchSwitchKey(datumJson.getString(key))) {
            datum = JSON.parseObject(jsonObject.getString("datum"), new TypeReference<Datum<SwitchDomain>>() {
            });
        } else if (KeyBuilder.matchServiceMetaKey(datumJson.getString(key))) {
            datum = JSON.parseObject(jsonObject.getString("datum"), new TypeReference<Datum<Service>>() {
            });
        }
        blockchainConsistencyService.onPut(datum);
        return "ok";
    }

    @NeedAuth
    @DeleteMapping("/datum/commit")
    public String onDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Type", "application/json; charset=" + getAcceptEncoding(request));
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Encode", "gzip");

        String entity = IoUtils.toString(request.getInputStream(), "UTF-8");
        String value = URLDecoder.decode(entity, "UTF-8");
        value = URLDecoder.decode(value, "UTF-8");
        JSONObject jsonObject = JSON.parseObject(value);

        Datum datum = JSON.parseObject(jsonObject.getString("datum"), Datum.class);
        BlockchainPeer source = JSON.parseObject(jsonObject.getString("source"), BlockchainPeer.class);

        blockchainConsistencyService.onRemove(datum);
        return "ok";
    }


    @GetMapping("/listeners")
    public JSONObject getAllListeners(HttpServletRequest request, HttpServletResponse response) {

        JSONObject result = new JSONObject();
        Map<String, List<RecordListener>> listeners = blockchainCore.getListeners();

        JSONArray listenerArray = new JSONArray();
        for (String key : listeners.keySet()) {
            listenerArray.add(key);
        }
        result.put("listeners", listenerArray);

        return result;
    }

    public static String getAcceptEncoding(HttpServletRequest req) {
        String encode = StringUtils.defaultIfEmpty(req.getHeader("Accept-Charset"), "UTF-8");
        encode = encode.contains(",") ? encode.substring(0, encode.indexOf(",")) : encode;
        return encode.contains(";") ? encode.substring(0, encode.indexOf(";")) : encode;
    }
}
