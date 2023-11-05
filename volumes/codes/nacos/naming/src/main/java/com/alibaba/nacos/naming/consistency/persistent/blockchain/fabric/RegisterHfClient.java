package com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

/**
 * @author: 李浩然
 * @date: 2021/4/25 11:03 下午
 */
@Component
public class RegisterHfClient {


    private static volatile HFClient hfClient = null;

    public static HFClient getInstance(String userName,String mspId,String keyPath,String crtPath) throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {
        if (hfClient == null) {
            synchronized (HFClient.class) {
                if (hfClient == null) {
                    hfClient = HFClient.createNewInstance();
                    FabricUserBean fabricUserBean = new FabricUserBean(userName, mspId, crtPath, keyPath);
                    hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
                    hfClient.setUserContext(fabricUserBean);
                }
            }
        }
        return hfClient;
    }
}
