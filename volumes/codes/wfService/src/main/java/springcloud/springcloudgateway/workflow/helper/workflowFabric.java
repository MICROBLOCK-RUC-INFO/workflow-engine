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

    private final String channelName="workflowchannel";
    private final String chaincodeName="wfscc";

    
    public void init() throws InvalidArgumentException, TransactionException, ChaincodeEndorsementPolicyParseException, ProposalException, IOException {
        initializeClient();
        initWorkflowChannel(client, channelName);
        initTransactionOptions();
        logger.info("fabric client and workflowChannel init successfully");
    }

    private void initTransactionOptions() {
        opts = new Channel.TransactionOptions();
        Channel.NOfEvents nOfEvents = Channel.NOfEvents.createNofEvents();
        Collection<EventHub> eventHubs = workflowchannel.getEventHubs();
        if (!eventHubs.isEmpty()) {
            nOfEvents.addEventHubs(eventHubs);
        }
        nOfEvents.addPeers(workflowchannel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)));
        nOfEvents.setN(1);
        opts.nOfEvents(nOfEvents);
    }

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
            request.setProposalWaitTime(20*1000L);
            Collection<ProposalResponse> responses=workflowchannel.queryByChaincode(request);
            logger.info("query responses {}",responses);
            return responses;
        } catch (Exception e) {
            logger.error("query exception {}",e.getMessage());
            return null;
        }
    }

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
            request.setProposalWaitTime(20*1000L);
            //System.out.println("ccid:"+request.getChaincodeID().toString());

            //上链sendTransaction-实际执行(写入wfscc链码中-暂时wfscc还需要修改以适配)
            Collection<ProposalResponse> responses=workflowchannel.sendTransactionProposal(request);
            //System.out.println("ccresponse:"+responses.size());
            //System.out.println(new String(responses.iterator().next().getChaincodeActionResponsePayload()));
            boolean success=true;
            for (ProposalResponse response:responses) {
                //链码执行结果判断
                // System.out.println(response.getStatus());
                // System.out.println(response.getChaincodeActionResponsePayload());
                // System.out.println(response.getChaincodeActionResponseReadWriteSetInfo().toString());
                // System.out.println(response.getChaincodeID());
                // System.out.println(response.getChaincodeActionResponseStatus());
                // System.out.println(response.getPeer());
                if (response.isInvalid()) {
                    success=false;
                    System.out.println(response.getMessage());
                }
            }
            //写区块
            CompletableFuture<TransactionEvent> ordererServiceRes= workflowchannel.sendTransaction(responses);
            if (success) return ordererServiceRes;
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
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
            request.setProposalWaitTime(20*1000L);
            //System.out.println("ccid:"+request.getChaincodeID().toString());

            //上链sendTransaction-实际执行(写入wfscc链码中-暂时wfscc还需要修改以适配)
            //需要修改
            //Collection<ProposalResponse> responses=workflowchannel.sendTransactionProposal(request);
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
            //写区块
            CompletableFuture<TransactionEvent> ordererServiceRes= workflowchannel.sendTransaction(responses_workflow,opts);
            if (success) return ordererServiceRes;
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Collection<String> getPeersIp(String channelName) {
        try {
            if (workflowchannel==null) {
                if (client==null) {
                    initializeClient();
                }
                initWorkflowChannel(client,channelName);
            }

            Collection<Peer> peers=workflowchannel.getPeers();
            Collection<String> peersUrl=new HashSet<>();
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



    private void initWorkflowChannel(HFClient client, String channelName) throws InvalidArgumentException, TransactionException {
        //初始化channel需要使用到channel名称、初始化好的peer、
        // 另外如果交易需要共识，则需要初始化orderer
        // String ordererTlsPath=wfConfig.getOrdererTlsPath();
        // String ordererName=wfConfig.getOrdererName();
        // String ordererAddr=wfConfig.getOrdererAddr();
        // String peerTlsPath=wfConfig.getPeerTlsPath();
        // String peerName=wfConfig.getPeerName();
        // String peerAddr=wfConfig.getPeerAddr();
        // Channel channel = client.newChannel(channelName);
        // channel.addOrderer(initWorkflowOrderer(client, ordererTlsPath, ordererName, ordererAddr));
        // channel.addPeer(initWorkflowPeer(client, peerTlsPath, peerName, peerAddr));
        // channel.initialize();
        // this.workflowchannel=channel;
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


}
