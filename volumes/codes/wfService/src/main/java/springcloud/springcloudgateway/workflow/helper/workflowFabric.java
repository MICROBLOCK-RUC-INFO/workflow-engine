package springcloud.springcloudgateway.workflow.helper;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hyperledger.fabric.protos.peer.FabricTransaction.Transaction;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.Channel.PeerOptions;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import springcloud.springcloudgateway.service.fabric.FabricUserBean;

/*
 *2025/4/7
 */
@Service
public class workflowFabric {
    private HFClient client=null;
    private volatile Channel workflowchannel=null;
    private volatile Channel.TransactionOptions opts=null;


    private Logger logger = LoggerFactory.getLogger(workflowFabric.class);

    @Resource
    wfConfig wfConfig;
    @Resource
    readChannelConfig readChannelConfig;

    //通道名
    private final String channelName="workflowchannel";
    //调用链码名
    private final String chaincodeName="wfscc";

    
    public void init() throws InvalidArgumentException, TransactionException, ChaincodeEndorsementPolicyParseException, ProposalException, IOException {
        //创建client端
        initializeClient();
        //创建通道
        initWorkflowChannel(client, channelName);
        //设定通道内peer节点的Transaction的事件监听
        initTransactionOptions();
        logger.info("fabric client and workflowChannel init successfully");
    }

    //设定通道内peer节点的Transaction的事件监听
    private void initTransactionOptions() {
        opts = new Channel.TransactionOptions();
        Channel.NOfEvents nOfEvents = Channel.NOfEvents.createNofEvents();
        Collection<EventHub> eventHubs = workflowchannel.getEventHubs();
        if (!eventHubs.isEmpty()) {
            nOfEvents.addEventHubs(eventHubs);
        }
        nOfEvents.addPeers(workflowchannel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)));
        //这句最重要，1表示任一节点完成区块链写入就认为Transaction写入成功，能大大提高响应时间。
        nOfEvents.setN(1);
        opts.nOfEvents(nOfEvents);
    }

    //创建client端
    private void initializeClient() {
        //初始化客户端需要用到  用户名称、MSPID、用户公私钥
        try {
            String userName=wfConfig.getUserName();//null
            String mspId=wfConfig.getMspId();//null
            String crtPath=wfConfig.getCrtPath();//null
            String keyPath=wfConfig.getKeyPath();//null
            System.out.println("userName:"+userName);
            System.out.println("mspId:"+mspId);
            System.out.println("crtPath:"+crtPath);
            System.out.println("keyPath:"+keyPath);
            HFClient client = HFClient.createNewInstance();
            FabricUserBean fabricUserBean = new FabricUserBean(userName, mspId, crtPath, keyPath);
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(fabricUserBean);
            this.client=client;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //链码的query操作
    public Collection<ProposalResponse> query(String channelName, String chaincodeName, 
                                                String fcn, ArrayList<String> args) {
        try {
            if (workflowchannel==null) {
                if (client==null) {
                    initializeClient();
                }
                initWorkflowChannel(client,channelName);
            }
            QueryByChaincodeRequest request=QueryByChaincodeRequest.newInstance(client.getUserContext());
            ChaincodeID ccid=ChaincodeID.newBuilder().setName(chaincodeName).build();
            request.setChaincodeID(ccid);
            request.setFcn(fcn);
            request.setArgs(args);
            //设置超时时间
            request.setProposalWaitTime(20*1000L);
            Collection<ProposalResponse> responses=workflowchannel.queryByChaincode(request);
            logger.info("query responses {}",responses);
            return responses;
        } catch (Exception e) {
            logger.error("query exception {}",e.getMessage());
            return null;
        }
    }

    //链码调用
    public CompletableFuture<TransactionEvent> invoke(String channelName, 
    String chaincodeName,String fcn, ArrayList<String> args) {
        try {
            if (workflowchannel==null) {
                if (client==null) {
                    initializeClient();
                }
                initWorkflowChannel(client,channelName);
            }
            //拼装发给区块链的请求内容
            TransactionProposalRequest request=client.newTransactionProposalRequest();
            ChaincodeID ccid=ChaincodeID.newBuilder().setName(chaincodeName).build();
            request.setChaincodeID(ccid);
            request.setFcn(fcn);
            request.setArgs(args);
             //设置超时时间
            request.setProposalWaitTime(20*1000L);
            //System.out.println("ccid:"+request.getChaincodeID().toString());

            //将request发送给背书节点背书
            Collection<ProposalResponse> responses=workflowchannel.sendTransactionProposal(request);
            boolean success=true;
            for (ProposalResponse response:responses) {
                if (response.isInvalid()) {
                    success=false;
                    System.out.println(response.getMessage());
                }
            }
            //写区块，将Transaction发送给orderer节点
            CompletableFuture<TransactionEvent> ordererServiceRes= workflowchannel.sendTransaction(responses);
            if (success) return ordererServiceRes;
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 魔改的链码调用，工作流专用版
     * @param channelName
     * @param chaincodeName
     * @param fcn wfscc只实现了flush这一个功能
     * @param args
     * @return
     */
    public CompletableFuture<TransactionEvent> workflowInvoke(String channelName, 
    String chaincodeName,String fcn, ArrayList<String> args) {
        try {
            if (workflowchannel==null) {
                if (client==null) {
                    initializeClient();
                }
                initWorkflowChannel(client,channelName);
            }
            //拼装发给区块链的请求内容
            TransactionProposalRequest request=client.newTransactionProposalRequest();
            ChaincodeID ccid=ChaincodeID.newBuilder().setName(chaincodeName).build();
            request.setChaincodeID(ccid);
            request.setFcn(fcn);
            request.setArgs(args);
            //设置超时时间
            request.setProposalWaitTime(20*1000L);
            //System.out.println("ccid:"+request.getChaincodeID().toString());

            /*
             * 工作流专用版
             * 原先这里是将请求发送给背书节点背书。但是由于我们已经经过各节点的工作流引擎执行，所以背书显得冗余。
             * 但是又需要构造一个Transaction来写入区块，所以更改了java-fabric-sdk源码，没有经过背书，直接通过request来生成Transaction
             */
            Collection<ProposalResponse> responses_workflow=workflowchannel.sendTransactionProposal_workflow(request);
            //System.out.println("ccresponse:"+responses.size());
            //System.out.println(new String(responses.iterator().next().getChaincodeActionResponsePayload()));
            boolean success=true;



            for (ProposalResponse response:responses_workflow) {
                //链码执行结果判断
                //System.out.println(response.getProposalResponse());
                if (response.isInvalid()) {
                    success=false;
                }
            }
            //写区块，将Transaction发送给orderer节点
            CompletableFuture<TransactionEvent> ordererServiceRes= workflowchannel.sendTransaction(responses_workflow,opts);
            if (success) return ordererServiceRes;
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //取得通道中Peer节点的IP
    public List<String> getPeersIp(String channelName) {
        try {
            if (workflowchannel==null) {
                if (client==null) {
                    initializeClient();
                }
                initWorkflowChannel(client,channelName);
            }

            Collection<Peer> peers=workflowchannel.getPeers();
            List<String> peersUrl=new ArrayList<>();
            for (Peer peer:peers) {
                String peerUrl=peer.getUrl();
                peersUrl.add(peerUrl.split(":")[1].substring(2));
            }
            //System.out.println(peersUrl.toString());
            return peersUrl;            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //创建peer节点
    private Peer initWorkflowPeer(HFClient client, String peerTlsPath, String peerName,
                                String peerAddr)
            throws InvalidArgumentException {
        //初始化peer配置需要用到peer的 tls证书、peer名称、peer地址
        Properties peerProp = new Properties();
        peerProp.setProperty("pemFile", peerTlsPath);
        peerProp.setProperty("negotiationType", "TLS");
        peerProp.setProperty("hostnameOverride", peerName);
        peerProp.setProperty("trustServerCertificate", "true");
        //用以解决grpc通讯交易长度限制 设置为10M
        peerProp.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 10 * 1024 * 1024);
        return client.newPeer(peerName, peerAddr, peerProp);
    }


    //创建orderer节点
    private Orderer initWorkflowOrderer(HFClient client, String ordererTlsPath,
                                      String ordererName, String ordererAddr)
            throws InvalidArgumentException {
        //初始化peer配置需要用到orderer的 tls证书、orderer名称、orderer地址
        Properties ordererProp = new Properties();
        ordererProp.setProperty("pemFile", ordererTlsPath);
        ordererProp.setProperty("negotiationType", "TLS");
        ordererProp.setProperty("hostnameOverride", ordererName);
        ordererProp.setProperty("trustServerCertificate", "true");
        //用以解决grpc通讯交易长度限制 设置为10M
        ordererProp.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 10 * 1024 * 1024);
        return client.newOrderer(ordererName, ordererAddr, ordererProp);
    }


    //创建通道
    private void initWorkflowChannel(HFClient client, String channelName) throws InvalidArgumentException, TransactionException {
        Map<String,List<String>> peersProperties=readChannelConfig.getPeerConfig(wfConfig);
        Map<String,List<String>> orderersProperties=readChannelConfig.getOrdererConfig(wfConfig);
        List<String> peerNameList=peersProperties.get(readChannelConfig.peerName);
        List<String> peerAddressList=peersProperties.get(readChannelConfig.peerAddress);
        List<String> peerTlsCaPathList=peersProperties.get(readChannelConfig.peerTlsCaPath);
        List<String> ordererNameList=orderersProperties.get(readChannelConfig.ordererName);
        List<String> ordererAddressList=orderersProperties.get(readChannelConfig.ordererAddress);
        List<String> ordererTlsCaPathList=orderersProperties.get(readChannelConfig.ordererTlsCaPath);
        Channel channel=client.newChannel(channelName);
        //channel.addPeer(null, PeerOptions.createPeerOptions().addPeerRole(Peer.PeerRole.ENDORSING_PEER));背书节点设置
        
        for (int i=0;i<peerNameList.size();i++) {
            channel.addPeer(initWorkflowPeer(client,peerTlsCaPathList.get(i),peerNameList.get(i),peerAddressList.get(i)));
            // if (peerNameList.get(i).equals(wfConfig.getPeerName())) {
            //     channel.addPeer(initWorkflowPeer(client,peerTlsCaPathList.get(i),peerNameList.get(i),peerAddressList.get(i)));
            // } else {
            //     Channel.PeerOptions peerOptions= Channel.PeerOptions.createPeerOptions();
            //     peerOptions.setPeerRoles(EnumSet.complementOf(EnumSet.of(PeerRole.ENDORSING_PEER,PeerRole.SERVICE_DISCOVERY)));
            //     channel.addPeer(initWorkflowPeer(client,peerTlsCaPathList.get(i),peerNameList.get(i),peerAddressList.get(i)),peerOptions);
            // }
        }
        for (int i=0;i<ordererNameList.size();i++) {
            channel.addOrderer(initWorkflowOrderer(client, ordererTlsCaPathList.get(i), ordererNameList.get(i), ordererAddressList.get(i)));
        }

        // for (int i=0;i<peerNameList.size();i++) {
        //     channel.addPeer(initWorkflowPeer(client,peerTlsCaPathList.get(i),peerNameList.get(i),peerAddressList.get(i)));
        // }
        // for (int i=0;i<ordererNameList.size();i++) {
        //     channel.addOrderer(initWorkflowOrderer(client, ordererTlsCaPathList.get(i), ordererNameList.get(i), ordererAddressList.get(i)));
        // }
        channel.initialize();
        this.workflowchannel=channel;
    }

    //获得区块高度
    public long getBlockHeight() throws ProposalException, InvalidArgumentException {
        return workflowchannel.queryBlockchainInfo().getHeight();
    }
}
