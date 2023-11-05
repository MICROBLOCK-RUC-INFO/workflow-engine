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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.naming.cluster.ServerStatus;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.RecordListener;
import com.alibaba.nacos.naming.consistency.persistent.PersistentConsistencyService;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Use simplified Blockchain protocol to maintain the consistency status of Nacos cluster.
 *
 * @author nkorange
 * @since 1.0.0
 */
@Service
public class BlockchainConsistencyServiceImpl implements PersistentConsistencyService {

    @Autowired
    private BlockchainCore blockchainCore;

    @Autowired
    private SwitchDomain switchDomain;

    @Override
    public void put(String key, Record value) throws NacosException {
        try {
            blockchainCore.signalPublish(key, value);
        } catch (Exception e) {
            Loggers.RAFT.error("Blockchain put failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Blockchain put failed, key:" + key + ", value:" + value, e);
        }
    }

    @Override
    public void remove(String key) throws NacosException {
        try {
            blockchainCore.signalDelete(key);
            blockchainCore.unlistenAll(key);
        } catch (Exception e) {
            Loggers.RAFT.error("Blockchain remove failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Blockchain remove failed, key:" + key, e);
        }
    }

    @Override
    public Datum get(String key) throws NacosException {
        return blockchainCore.getDatum(key);
    }

    @Override
    public void listen(String key, RecordListener listener) throws NacosException {
        blockchainCore.listen(key, listener);
    }

    @Override
    public void unlisten(String key, RecordListener listener) throws NacosException {
        blockchainCore.unlisten(key, listener);
    }

    @Override
    public boolean isAvailable() {
        return blockchainCore.isInitialized() || ServerStatus.UP.name().equals(switchDomain.getOverriddenServerStatus());
    }

    public void onPut(Datum datum) throws NacosException {
        try {
            blockchainCore.onRepublish(datum);
        } catch (Exception e) {
            Loggers.RAFT.error("Blockchain onPut failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Blockchain onPut failed, datum:" + datum + e);
        }
    }

    public void onRemove(Datum datum) throws NacosException {
        try {
            blockchainCore.onRedelete(datum.key);
        } catch (Exception e) {
            Loggers.RAFT.error("Blockchain onRemove failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Blockchain onRemove failed, datum:" + datum + e);
        }
    }
}
