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
package com.alibaba.nacos.naming.raft;

import com.alibaba.nacos.naming.BaseTest;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.KeyBuilder;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainCore;
import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainStore;
import com.alibaba.nacos.naming.core.Instance;
import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.misc.Loggers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

/**
 * @author nkorange
 * @author jifengnan 2019-05-18
 */
public class RaftStoreTest extends BaseTest {

    @InjectMocks
    @Spy
    public BlockchainCore blockchainCore;

    @Spy
    public BlockchainStore blockchainStore;

    @Test
    public void wrietDatum() throws Exception {
        Datum<Instances> datum = new Datum<>();
        String key = KeyBuilder.buildInstanceListKey(TEST_NAMESPACE, TEST_SERVICE_NAME, false);
        datum.key = key;
        datum.timestamp.getAndIncrement();
        datum.value = new Instances();
        Instance instance = new Instance("1.1.1.1", 1, TEST_CLUSTER_NAME);
        datum.value.getInstanceList().add(instance);
        instance = new Instance("2.2.2.2", 2, TEST_CLUSTER_NAME);
        datum.value.getInstanceList().add(instance);
        blockchainStore.write(datum);
        blockchainCore.init();
        Datum result = blockchainCore.getDatum(key);
        System.out.println("DATUM=" + datum.value.toString());
        System.out.println("RESULT-KEY=" + result.key.toString());
        Assert.assertEquals(1, result.timestamp.intValue());
        Assert.assertEquals(datum.value.toString(), result.value.toString());
    }
}
