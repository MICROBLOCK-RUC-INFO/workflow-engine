package com.alibaba.nacos.naming.consistency.persistent.blockchain.fabric;

import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author: 李浩然
 * @date: 2021/1/3 9:14 下午
 */
public interface FabricService {

    /**
     * 创建通道
     *
     * @param channelName 通道名称
     * @param tx64String  tx文件字符
     * @return
     * @throws IOException
     * @throws InvalidArgumentException
     * @throws TransactionException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     */
    String createChannel(String channelName, String tx64String) throws IOException, InvalidArgumentException, TransactionException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException;

    /**
     * 加入通道
     *
     * @param channel64String
     * @param peerName
     * @return
     * @throws ProposalException
     * @throws TransactionException
     * @throws InvalidArgumentException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    boolean joinChannel(String channel64String, String peerName) throws ProposalException, TransactionException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, IOException;


    /**
     * 安装链码
     *
     * @param chainCodeName
     * @param chainCodeVersion
     * @param chainCodePath
     * @param projectName
     * @param language
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     * @throws TransactionException
     */
    Collection<ProposalResponse> installChainCode(String chainCodeName, String chainCodeVersion, String chainCodePath, String projectName, TransactionRequest.Type language) throws InvalidArgumentException, ProposalException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException;

    /**
     * 实例化链码
     *
     * @param channelName
     * @param chainCodeName
     * @param chainCodePath
     * @param chainCodeVersion
     * @param language
     * @param args
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     * @throws TransactionException
     */
    Collection<ProposalResponse> instantiantChainCode(String channelName, String chainCodeName, String chainCodePath, String chainCodeVersion, TransactionRequest.Type language, ArrayList<String> args) throws InvalidArgumentException, ProposalException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException;

    /**
     * 获取当前区块高度
     *
     * @param channelName
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InvalidArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     * @throws TransactionException
     * @throws ProposalException
     */
    Long getHeight(String channelName) throws NoSuchMethodException, InvocationTargetException, InvalidArgumentException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException, ProposalException;

    /**
     * 查询该节点已经安装的链码
     *
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InvalidArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     * @throws ProposalException
     */
    ArrayList<String> queryInstalledChaincodes() throws NoSuchMethodException, InvocationTargetException, InvalidArgumentException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, ProposalException;

    /**
     * 查询该通道已实例化的链码
     *
     * @param channelName
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InvalidArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws CryptoException
     * @throws ClassNotFoundException
     * @throws ProposalException
     * @throws TransactionException
     */
    ArrayList<String> queryInstantiatedChaincodes(String channelName) throws NoSuchMethodException, InvocationTargetException, InvalidArgumentException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, ProposalException, TransactionException;

    /**
     * 查询链码
     *
     * @param channelName
     * @param chaincodeName
     * @param func
     * @param args
     * @return
     * @throws Exception
     */
    Collection<ProposalResponse> query(String channelName, String chaincodeName, String func, ArrayList<String> args) throws Exception;

    /**
     * 调用链码
     *
     * @param channelName
     * @param chaincodeName
     * @param func
     * @param args
     * @return
     * @throws Exception
     */
    Collection<ProposalResponse> invoke(String channelName, String chaincodeName, String func, ArrayList<String> args) throws Exception;

    /**
     * 数字签名
     *
     * @param plainText
     * @return
     * @throws InvalidArgumentException
     * @throws CryptoException
     */
    byte[] sign(String plainText) throws InvalidArgumentException, CryptoException;

    /**
     * 验证签名
     *
     * @param sign
     * @param plainText
     * @return
     * @throws CryptoException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     */
    boolean verify(byte[] sign, String plainText) throws CryptoException, IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException;
}

