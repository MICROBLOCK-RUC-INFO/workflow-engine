package com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: 李浩然
 * @date: 2021/4/26 9:12 下午
 */
@Component
public class RegisterChannelConfig {
    private static String peerTlsPath;
    private static String peerName;
    private static String peerAddr;
    private static String ordererTlsPath;
    private static String ordererName;
    private static String ordererAddr;

    @Value("${fabric.peertlspath}")
    public static void setPeerTlsPath(String peerTlsPath) {
        RegisterChannelConfig.peerTlsPath = peerTlsPath;
    }

    @Value("${fabric.peername}")
    public static void setPeerName(String peerName) {
        RegisterChannelConfig.peerName = peerName;
    }

    @Value("${fabric.peeraddr}")
    public static void setPeerAddr(String peerAddr) {
        RegisterChannelConfig.peerAddr = peerAddr;
    }

    @Value("${fabric.orderertlspath}")
    public static void setOrdererTlsPath(String ordererTlsPath) {
        RegisterChannelConfig.ordererTlsPath = ordererTlsPath;
    }

    @Value("${fabric.orderername}")
    public static void setOrdererName(String ordererName) {
        RegisterChannelConfig.ordererName = ordererName;
    }

    @Value("${fabric.ordereraddr}")
    public static void setOrdererAddr(String ordererAddr) {
        RegisterChannelConfig.ordererAddr = ordererAddr;
    }

    private static volatile Channel registerChannel = null;

    public RegisterChannelConfig() {
    }


    public static Channel getRegisterChannel(HFClient client, String channelName) throws Exception {
        if (registerChannel == null) {
            synchronized (RegisterChannelConfig.class) {
                if (registerChannel == null) {
                    registerChannel = client.newChannel(channelName);
                }
            }
        }
        return registerChannel;
    }
}
