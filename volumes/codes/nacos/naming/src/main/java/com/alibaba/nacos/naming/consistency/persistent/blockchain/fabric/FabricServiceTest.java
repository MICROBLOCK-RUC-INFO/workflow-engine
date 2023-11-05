package com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric;

import com.alibaba.nacos.naming.misc.Loggers;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author: 李浩然
 * @date: 2021/1/3 9:14 下午
 */
public class FabricServiceTest {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private FabricServiceImpl fabricService = new FabricServiceImpl();
    String txFilePath = "/channelTxs/";
    String channelStringPath = "/channelString/";


    void createChannel() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidArgumentException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException {
        String channelName = "softwarechannel";
        String tx64String = Base64.getEncoder().encodeToString(FileUtils.readFile(txFilePath + channelName + ".tx"));
        String channel64String = fabricService.createChannel(channelName, tx64String);
        FileUtils.writeFile(channel64String, channelStringPath + channelName + ".txt", false);
        Loggers.RAFT.info("成功创建通道=【{}】", channel64String);
    }

    void joinChannel() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, ProposalException, InvalidArgumentException, IOException, CryptoException, ClassNotFoundException, TransactionException {
        String channelName = "softwarechannel";
//        String channelName = "channel202005231256";
        String channel64String = new String(FileUtils.readFile(channelStringPath + channelName + ".txt"));
        String peerName = "peer1.fabric.gfe.com";
        boolean result = fabricService.joinChannel(channel64String, peerName);
        Loggers.RAFT.info("通道加入=【{}】", result);
    }

    void installChainCode() throws IllegalAccessException, InstantiationException, ProposalException, NoSuchMethodException, InvalidArgumentException, InvocationTargetException, CryptoException, ClassNotFoundException, TransactionException, IOException {
        String channelName = "softwarechannel";
        String chainCodeName = "nacos";
        String chainCodeVersion = "1.0.0";
        String chainCodePath = "/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/fabric-nodejs/artifacts";
        String projectName = "github.com/nacos";
        TransactionRequest.Type language = TransactionRequest.Type.GO_LANG;

        Collection<ProposalResponse> proposals = fabricService.installChainCode(chainCodeName, chainCodeVersion, chainCodePath, projectName, language);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("链码安装成功 Txid: {} from peer {}}", response.getTransactionID(), response.getPeer().getName());
            } else {
                Loggers.RAFT.info("链码安装失败");
            }
        }

    }

    void instantiantChainCode() throws IllegalAccessException, InstantiationException, ProposalException, NoSuchMethodException, InvalidArgumentException, InvocationTargetException, CryptoException, ClassNotFoundException, TransactionException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException {
        String channelName = "softwarechannel";
        String chainCodeName = "nacos";
        String chainCodePath = "/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/fabric-nodejs/artifacts";
        String chainCodeVersion = "1.0.0";
        String projectName = "github.com/nacos";
        TransactionRequest.Type language = TransactionRequest.Type.GO_LANG;
        ArrayList<String> args = new ArrayList<>();
        Collection<ProposalResponse> proposals = fabricService.instantiantChainCode(channelName, chainCodeName, chainCodePath, chainCodeVersion, language, args);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("链码成功实例化 Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), response.getChaincodeActionResponsePayload());
            } else {
                Loggers.RAFT.info("链码实例化失败 , reason: {}", response.getMessage());
            }
        }
    }

    void getHeight() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidArgumentException, CryptoException, ClassNotFoundException, TransactionException, ProposalException {
        String channelName = "softwarechannel";
        Long height = fabricService.getHeight(channelName);
        Loggers.RAFT.info("getHeight result= 【{}】", height);

    }

    void queryInstalledChaincodes() throws NoSuchMethodException, InvalidArgumentException, InstantiationException, ClassNotFoundException, ProposalException, IllegalAccessException, InvocationTargetException, CryptoException {
        ArrayList<String> arrayList = fabricService.queryInstalledChaincodes();
        arrayList.forEach(Loggers.RAFT::info);

    }

    void queryInstantiatedChaincodes() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidArgumentException, CryptoException, ClassNotFoundException, TransactionException, ProposalException {
        String channelName = "softwarechannel";
        ArrayList<String> arrayList = fabricService.queryInstantiatedChaincodes(channelName);
        arrayList.forEach(Loggers.RAFT::info);
    }

    void query() throws Exception {
        String channelName = "softwarechannel";
        String chainCodeName = "nacos";
        String func = "QueryByKey";
        ArrayList<String> params = new ArrayList<>();
        params.add("dsfdsf");


        Collection<ProposalResponse> proposals = fabricService.query(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("Query成功 Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
                Loggers.RAFT.info("response:::" + response.getMessage());
            } else {
                Loggers.RAFT.info("Query失败, reason: {}", response.getMessage());
            }
        }
    }

    void invoke() throws Exception {
        String channelName = "registerchannel";
        String chainCodeName = "nacos";
        String func = "Put";
        ArrayList<String> params = new ArrayList<>();
        params.add("dsfdsf");
        params.add("1234");

        Collection<ProposalResponse> proposals = fabricService.invoke(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("Successful transaction proposal response Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
            } else {
                Loggers.RAFT.info("Faild tracsaction proposal response, reason: {}", response.getMessage());
            }
        }
    }
}
