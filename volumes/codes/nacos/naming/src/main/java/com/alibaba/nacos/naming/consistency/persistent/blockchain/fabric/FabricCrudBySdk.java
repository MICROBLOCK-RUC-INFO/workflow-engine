package com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric;

import com.alibaba.nacos.naming.consistency.persistent.blockchain.BlockchainCrud;
import com.alibaba.nacos.naming.misc.Loggers;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author: 李浩然
 * @date: 2021/1/3 9:14 下午
 */
@Component
public class FabricCrudBySdk implements BlockchainCrud {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Autowired
    private FabricServiceImpl fabricService = new FabricServiceImpl();
    String txFilePath = "/channelTxs/";
    String channelStringPath = "/channelString/";


    @Override
    public String fabricPut(String key, String value) throws Exception {
        String channelName = "workflowchannel";
        String chainCodeName = "nacos";
        String func = "Put";
        ArrayList<String> params = new ArrayList<>();
        params.add(key);
        params.add(value);
        String res = "unknown";
        Collection<ProposalResponse> proposals = fabricService.invoke(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("Successful transaction proposal response Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
                res = new String(response.getChaincodeActionResponsePayload());
            } else {
                Loggers.RAFT.info("Faild tracsaction proposal response, reason: {}", response.getMessage());
                res = response.getMessage();
            }
        }
        return res;
    }

    @Override
    public String fabricQueryByKey(String key) throws Exception {
        String channelName = "workflowchannel";
        String chainCodeName = "nacos";
        String func = "QueryByKey";
        ArrayList<String> params = new ArrayList<>();
        params.add(key);
        String res = "unknown";
        Collection<ProposalResponse> proposals = fabricService.query(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("Successful transaction proposal response Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
                res = new String(response.getChaincodeActionResponsePayload());
            } else {
                Loggers.RAFT.info("Faild tracsaction proposal response, reason: {}", response.getMessage());
                return response.getMessage();
            }
        }
        return res;
    }

    @Override
    public String fabricDelete(String key) throws Exception {
        String channelName = "workflowchannel";
        String chainCodeName = "nacos";
        String func = "Delete";
        ArrayList<String> params = new ArrayList<>();
        params.add(key);
        String res = "unknown";
        Collection<ProposalResponse> proposals = fabricService.invoke(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("Successful transaction proposal response Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
                res = new String(response.getChaincodeActionResponsePayload());
            } else {
                Loggers.RAFT.info("Faild tracsaction proposal response, reason: {}", response.getMessage());
                return response.getMessage();
            }
        }
        return res;
    }

    @Override
    public String fabricQueryAllNamingData() throws Exception {
        String channelName = "workflowchannel";
        String chainCodeName = "nacos";
        String func = "RangeQuery";
        ArrayList<String> params = new ArrayList<>();
        params.add("com.alibaba.nacos.naming");
        params.add("com.alibaba.nacos.naminh");
        String res = "unknown";
        Collection<ProposalResponse> proposals = fabricService.invoke(channelName, chainCodeName, func, params);
        for (ProposalResponse response : proposals) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Loggers.RAFT.info("Successful transaction proposal response Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
                res = new String(response.getChaincodeActionResponsePayload());
            } else {
                Loggers.RAFT.info("Faild tracsaction proposal response, reason: {}", response.getMessage());
                return response.getMessage();
            }
        }
        return res;
    }

    public byte[] sign(String text) throws InvalidArgumentException, CryptoException {
        return fabricService.sign(text);
    }

    public boolean verify(byte[] sign, String plainText) throws CryptoException, IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        return fabricService.verify(sign, plainText);
    }
}
