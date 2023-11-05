// package springcloud.springcloudgateway.service.fabric;

// import groovy.util.logging.Slf4j;
// import org.bouncycastle.jce.provider.BouncyCastleProvider;
// import org.hyperledger.fabric.protos.peer.Query;
// import org.hyperledger.fabric.sdk.*;
// import org.hyperledger.fabric.sdk.exception.CryptoException;
// import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
// import org.hyperledger.fabric.sdk.exception.ProposalException;
// import org.hyperledger.fabric.sdk.exception.TransactionException;
// import org.hyperledger.fabric.sdk.security.CryptoSuite;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import springcloud.springcloudgateway.config.fabricConfig.RegisterChannelConfig;
// import springcloud.springcloudgateway.config.fabricConfig.RegisterHfClient;

// import java.io.File;
// import java.io.IOException;
// import java.lang.reflect.InvocationTargetException;
// import java.nio.file.Paths;
// import java.security.Security;
// import java.util.*;

// import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author: 李浩然
 * @date: 2021/1/3 9:14 下午
 */
// @Slf4j
// @Service
// public class FabricServiceNacosImpl implements FabricService {
//     static {
//         Security.addProvider(new BouncyCastleProvider());
//     }

//     //region application.yaml
//     @Value(value = "${fabric.nacos.username}")
//     private String userName;
//     @Value(value = "${fabric.nacos.mspid}")
//     private String mspId;
//     @Value(value = "${fabric.nacos.keypath}")
//     private String keyPath;
//     @Value(value = "${fabric.nacos.crtpath}")
//     private String crtPath;
//     @Value(value = "${fabric.nacos.peertlspath}")
//     private String peerTlsPath;
//     @Value(value = "${fabric.nacos.peername}")
//     private String peerName;
//     @Value(value = "${fabric.nacos.peeraddr}")
//     private String peerAddr;
//     @Value(value = "${fabric.nacos.orderertlspath}")
//     private String ordererTlsPath;
//     @Value(value = "${fabric.nacos.orderername}")
//     private String ordererName;
//     @Value(value = "${fabric.nacos.ordereraddr}")
//     private String ordererAddr;
//     private String channelBlockPath = "/channelBlock/";

// /*    private String channelBlockPath="/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/springcloud-gateway-monitor/src/main/docker/channelBlock/";
//     private String userName="admin_cc_gfe";
//     private String mspId="GfeMSP";
//     private String crtPath= "/Users/lihaoran/Desktop/project/SCASverify/fabric-nodejs/artifacts/channel/crypto-config/peerOrganizations/fabric.gfe.com/users/Admin@fabric.gfe.com/msp/signcerts/Admin@fabric.gfe.com-cert.pem";
//     private String keyPath= "/Users/lihaoran/Desktop/project/SCASverify/fabric-nodejs/artifacts/channel/crypto-config/peerOrganizations/fabric.gfe.com/users/Admin@fabric.gfe.com/msp/keystore/bfcf0c59d4ccec10f52bdaea0eddfc7246a3964c368058453eaea813e27f3f28_sk";
//     private String peerTlsPath="/Users/lihaoran/Desktop/project/SCASverify/fabric-nodejs/artifacts/channel/crypto-config/peerOrganizations/fabric.gfe.com/users/User1@fabric.gfe.com/tls/ca.crt";
//     private String peerName="peer0.fabric.gfe.com";
//     private String peerAddr= "grpcs://127.0.0.1:7051";
//     private String ordererName= "gfe.orderer.com";
//     private String ordererAddr="grpcs://127.0.0.1:9050";
//     private String ordererTlsPath= "/Users/lihaoran/Desktop/project/SCASverify/fabric-nodejs/artifacts/channel/crypto-config/ordererOrganizations/orderer.com/users/Admin@orderer.com/tls/ca.crt";
//     */
//     /**
//      * 日志文件
//      */
//     Logger logger = LoggerFactory.getLogger(FabricServiceNacosImpl.class);

//     /**
//      * peer channel create -o orderer地址 -c 通道名称 -f 通道配置文件 --tls true --cafile orderer的tls证书
//      *
//      * @param channelName 通道名称
//      * @param txFile
//      * @return
//      * @throws IOException
//      * @throws InvalidArgumentException
//      * @throws TransactionException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      */
//     @Override
//     public String createChannel(String channelName, String txFile) throws IOException, InvalidArgumentException, TransactionException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException {
//         //1. 初始化客户端
//         HFClient hfClient = initializeClient();
//         //2. 获取通道配置信息
//         ChannelConfiguration configuration = new ChannelConfiguration(Base64.getDecoder().decode(txFile.getBytes()));
//         //3. 获取签名
//         byte[] signData = hfClient.getChannelConfigurationSignature(configuration, hfClient.getUserContext());
//         //4. 获取orderer相关信息
//         Orderer orderer = initializeOrderer(hfClient, ordererTlsPath, ordererName, ordererAddr);
//         //5. 创建通道、实例化通道
//         Channel channel = hfClient.newChannel(channelName, orderer, configuration, signData);
//         channel.initialize();
//         //6. 保存序列化后的通道信息  将生成的channel文件保存到本地进行备份，防止丢失
//         channel.serializeChannel(new File(channelBlockPath + channelName + ".block"));
//         String result = Base64.getEncoder().encodeToString(channel.serializeChannel());
//         //7. 关闭通道
//         if (!channel.isShutdown()) {
//             channel.shutdown(true);
//         }
//         return result;
//     }

//     /**
//      * peer channel join -b 通道创世区块文件 --tls true --cafile orderer的tls证书
//      *
//      * @param channel64String
//      * @param peerName
//      * @return
//      * @throws ProposalException
//      * @throws TransactionException
//      * @throws InvalidArgumentException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws IOException
//      */
//     @Override
//     public boolean joinChannel(String channel64String, String peerName) throws ProposalException, TransactionException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, IOException {
//         //1. 初始化客户端
//         HFClient hfClient = initializeClient();

//         //2. 反序列化通道信息
//         Channel channel = hfClient.deSerializeChannel(Base64.getDecoder().decode(channel64String.getBytes()));

//         //3. 实例化通道

//         //4. 初始化节点信息
//         Peer peer = initializePeer(hfClient, peerTlsPath, peerName, peerAddr);
//         //5. 将节点加入通道
//         channel = channel.joinPeer(peer);
//         channel.initialize();

//         boolean result = channel != null;

//         //6. 关闭通道
//         if (channel != null && !channel.isShutdown()) {
//             channel.shutdown(true);
//         }
//         return result;
//     }

//     /**
//      * peer chaincode install -n 链码名称 -v 链码版本 -p 链码路径 -l 链码语言
//      *
//      * @param chainCodeName
//      * @param chainCodeVersion
//      * @param chainCodePath
//      * @param projectName
//      * @param language
//      * @return
//      * @throws InvalidArgumentException
//      * @throws ProposalException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws TransactionException
//      */
//     @Override
//     public Collection<ProposalResponse> installChainCode(
//             String chainCodeName, String chainCodeVersion, String chainCodePath,
//             String projectName, TransactionRequest.Type language) throws InvalidArgumentException, ProposalException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 实例化通道
//         ArrayList<Peer> peerArrayList = new ArrayList<>();
//         Peer peer = initializePeer(client, peerTlsPath, peerName, peerAddr);
//         peerArrayList.add(peer);
//         //3. 设置链码相关信息
//         InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
//         ChaincodeID chainCode = ChaincodeID.newBuilder().setName(chainCodeName).setVersion(chainCodeVersion).build();
//         installProposalRequest.setChaincodeID(chainCode);
//         installProposalRequest.setChaincodeSourceLocation(Paths.get(chainCodePath).toFile());
//         installProposalRequest.setChaincodePath(projectName);
//         installProposalRequest.setChaincodeVersion(chainCodeVersion);
//         installProposalRequest.setChaincodeLanguage(language);

//         //4. 向需要安装的节点发送安装链码请求
//         return client.sendInstallProposal(installProposalRequest, peerArrayList);
//     }

//     /**
//      * peer chaincode instantiate -o orderer地址  -C 通道名称 -n 链码名称 -v 链码版本 -c '{"Args":["init"]}'
//      * --tls true --cafile orderer的tls证书
//      *
//      * @param channelName
//      * @param chainCodeName
//      * @param chainCodePath
//      * @param chainCodeVersion
//      * @param language
//      * @param args
//      * @return
//      * @throws InvalidArgumentException
//      * @throws ProposalException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws TransactionException
//      */
//     @Override
//     public Collection<ProposalResponse> instantiantChainCode(
//             String channelName, String chainCodeName, String chainCodePath, String chainCodeVersion,
//             TransactionRequest.Type language, ArrayList<String> args) throws InvalidArgumentException, ProposalException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 实例化通道
//         Channel channel = initializeChannel(client, channelName);
//         //3. 设置链码相关信息
//         InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
//         instantiateProposalRequest.setProposalWaitTime(120000);
//         ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chainCodeName)
//                 .setPath(chainCodePath).setVersion(chainCodeVersion).build();
//         instantiateProposalRequest.setChaincodeID(chaincodeID);
//         instantiateProposalRequest.setChaincodeLanguage(language);
//         instantiateProposalRequest.setFcn("init");
//         instantiateProposalRequest.setArgs(args);
//         Map<String, byte[]> tm = new HashMap<>(2);
//         tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
//         tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
//         instantiateProposalRequest.setTransientMap(tm);
//         //4. 向需要实例化的节点发送实例化链码请求模拟交易
//         Collection<ProposalResponse> proposalResponses =
//                 channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
//         //5. 发送真正的实例化请求
//         channel.sendTransaction(proposalResponses);
//         //6. 关闭通道
//         if (!channel.isShutdown()) {
//             channel.shutdown(true);
//         }
//         return proposalResponses;
//     }

//     /**
//      * peer channel getinfo -c channel202005231219
//      *
//      * @param channelName
//      * @return
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InvalidArgumentException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws TransactionException
//      * @throws ProposalException
//      */
//     @Override
//     public Long getHeight(String channelName) throws NoSuchMethodException, InvocationTargetException, InvalidArgumentException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException, ProposalException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 初始化通道
//         Channel channel = initializeChannel(client, channelName);
//         //3. 调用获取区块信息请求
//         BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
//         Long height = blockchainInfo.getBlockchainInfo().getHeight();
//         //4. 关闭通道
//         if (!channel.isShutdown()) {
//             channel.shutdown(true);
//         }
//         logger.info("getHeight height {}", height);
//         return height;
//     }

//     /**
//      * peer chaincode list --installed
//      *
//      * @return
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InvalidArgumentException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws ProposalException
//      */
//     @Override
//     public ArrayList<String> queryInstalledChaincodes() throws NoSuchMethodException, InvocationTargetException, InvalidArgumentException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, ProposalException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 初始化节点
//         Peer peer = initializePeer(client, peerTlsPath, peerName, peerAddr);
//         //3. 调用获取已安装链码请求
//         List<Query.ChaincodeInfo> chaincodeInfoList = client.queryInstalledChaincodes(peer);
//         ArrayList<String> installedChainCodesList = new ArrayList<>();
//         chaincodeInfoList.forEach(chaincodeInfo -> {
//             installedChainCodesList.add(chaincodeInfo.getName());
//         });
//         logger.info("queryInstalledChaincodes installedChainCodesList {}", installedChainCodesList);

//         return installedChainCodesList;
//     }

//     /**
//      * peer chaincode list --instantiated -C channel202005231219
//      *
//      * @param channelName
//      * @return
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InvalidArgumentException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws ProposalException
//      * @throws TransactionException
//      */
//     @Override
//     public ArrayList<String> queryInstantiatedChaincodes(String channelName) throws NoSuchMethodException, InvocationTargetException, InvalidArgumentException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, ProposalException, TransactionException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 初始化通道
//         Channel channel = initializeChannel(client, channelName);
//         //3. 调用获取已实例化链码请求
//         List<Query.ChaincodeInfo> chaincodeInfoList = channel.queryInstantiatedChaincodes(
//                 channel.getPeers().iterator().next());
//         ArrayList<String> instantiatedChaincodesList = new ArrayList<>();
//         chaincodeInfoList.forEach(chaincodeInfo -> {
//             instantiatedChaincodesList.add(chaincodeInfo.getName());
//         });
//         //4. 关闭通道
//         if (!channel.isShutdown()) {
//             channel.shutdown(true);
//         }
//         logger.info("queryInstantiatedChaincodes instantiatedChaincodesList {}", instantiatedChaincodesList);

//         return instantiatedChaincodesList;
//     }

//     /**
//      * peer chaincode query -o orderer地址 -C 通道名称 -n 链码名称 -c '{"Args":["方法名称","方法参数"]}'
//      *
//      * @param channelName
//      * @param chaincodeName
//      * @param func
//      * @param args
//      * @return
//      * @throws InvalidArgumentException
//      * @throws ProposalException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws TransactionException
//      */
//     @Override
//     public Collection<ProposalResponse> query(String channelName, String chaincodeName,
//                                               String func, ArrayList<String> args) throws Exception {
//         //1. 初始化客户端
//         HFClient client = RegisterHfClient.getInstance();
//         //2. 拼装query chaincode请求参数
//         QueryByChaincodeRequest request = QueryByChaincodeRequest.newInstance(client.getUserContext());
//         ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
//         request.setChaincodeID(ccid);
//         request.setFcn(func);
//         request.setArgs(args);
//         request.setProposalWaitTime(3000);
//         //3. 初始化通道
//         Channel channel = RegisterChannelConfig.getRegisterChannel(client, channelName);
//         if (channel.getOrderers().size() == 0 || channel.getPeers().size() == 0) {
//             synchronized (FabricServiceImpl.class) {
//                 if (channel.getOrderers().size() == 0) {
//                     channel.addOrderer(initializeOrderer(client, ordererTlsPath, ordererName, ordererAddr));
//                 }
//                 if (channel.getPeers().size() == 0) {
//                     channel.addPeer(initializePeer(client, peerTlsPath, peerName, peerAddr));
//                 }
//                 if (!channel.isInitialized()) {
//                     channel.initialize();
//                 }
//             }
//         }
//         //4. 调用链码请求
//         Collection<ProposalResponse> responses = channel.queryByChaincode(request);
//         //5. 关闭通道
// //        if (!channel.isShutdown()) {
// //            channel.shutdown(true);
// //        }
//         logger.info("query responses {}", responses);
//         return responses;
//     }

//     public Collection<ProposalResponse> query_test(String channelName, String chaincodeName,
//                                                    String func, ArrayList<String> args) throws InvalidArgumentException, ProposalException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 拼装query chaincode请求参数
//         QueryByChaincodeRequest request = QueryByChaincodeRequest.newInstance(client.getUserContext());
//         ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
//         request.setChaincodeID(ccid);
//         request.setFcn(func);
//         request.setArgs(args);
//         request.setProposalWaitTime(60000);
//         //3. 初始化通道
//         Channel channel = initializeChannel(client, channelName);
//         //4. 调用链码请求
//         Collection<ProposalResponse> responses = channel.queryByChaincode(request);
//         //5. 关闭通道
//         if (!channel.isShutdown()) {
//             channel.shutdown(true);
//         }
//         logger.info("query responses {}", responses);
//         return responses;
//     }

//     /**
//      * peer chaincode invoke -o orderer地址 -C 通道名称 -n 链码名称 -c '{"Args":["方法名称","方法参数"]}'
//      * --tls true --cafile orderer的tls证书
//      *
//      * @param channelName
//      * @param chaincodeName
//      * @param func
//      * @param args
//      * @return
//      * @throws InvalidArgumentException
//      * @throws ProposalException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      * @throws CryptoException
//      * @throws ClassNotFoundException
//      * @throws TransactionException
//      */
//     @Override
//     public Collection<ProposalResponse> invoke(String channelName, String chaincodeName,
//                                                String func, ArrayList<String> args) throws InvalidArgumentException, ProposalException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException {
//         //1. 初始化客户端
//         HFClient client = initializeClient();
//         //2. 拼装chaincode请求参数
//         TransactionProposalRequest request = client.newTransactionProposalRequest();
//         ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
//         request.setChaincodeID(ccid);
//         request.setFcn(func);
//         request.setArgs(args);
//         request.setProposalWaitTime(30000);
//         //3. 初始化通道
//         Channel channel = initializeChannel(client, channelName);
//         //4. 模拟调用链码请求
//         Collection<ProposalResponse> responses = channel.sendTransactionProposal(request);
//         //5. 发送交易到排序节点进行排序
//         channel.sendTransaction(responses);
//         //6. 关闭通道
//         if (!channel.isShutdown()) {
//             channel.shutdown(true);
//         }

//         logger.info("invoke responses {}", responses);
//         return responses;
//     }


//     /**
//      * 创建客户端
//      *
//      * @return Hfclient
//      * @throws CryptoException
//      * @throws InvalidArgumentException
//      * @throws ClassNotFoundException
//      * @throws NoSuchMethodException
//      * @throws InvocationTargetException
//      * @throws InstantiationException
//      * @throws IllegalAccessException
//      */
//     private HFClient initializeClient() throws CryptoException, InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//         //初始化客户端需要用到  用户名称、MSPID、用户公私钥
//         FabricUserBean fabricUserBean = new FabricUserBean(userName, mspId, crtPath, keyPath);
//         HFClient client = HFClient.createNewInstance();
//         client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
//         client.setUserContext(fabricUserBean);
//         return client;
//     }

//     /**
//      * 初始化peer
//      *
//      * @param client
//      * @param peerTlsPath
//      * @param peerName
//      * @param peerAddr
//      * @return
//      * @throws InvalidArgumentException
//      */
//     private Peer initializePeer(HFClient client, String peerTlsPath, String peerName,
//                                 String peerAddr)
//             throws InvalidArgumentException {
//         //初始化peer配置需要用到peer的 tls证书、peer名称、peer地址
//         Properties peerProp = new Properties();
//         peerProp.setProperty("pemFile", peerTlsPath);
//         peerProp.setProperty("negotiationType", "TLS");
//         peerProp.setProperty("hostnameOverride", peerName);
//         peerProp.setProperty("trustServerCertificate", "true");
//         //用以解决grpc通讯交易长度限制 设置为10M
//         peerProp.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 10 * 1024 * 1024);
//         return client.newPeer(peerName, peerAddr, peerProp);
//     }

//     /**
//      * 初始化orderer
//      *
//      * @param client
//      * @param ordererTlsPath
//      * @param ordererName
//      * @param ordererAddr
//      * @return
//      * @throws InvalidArgumentException
//      */
//     private Orderer initializeOrderer(HFClient client, String ordererTlsPath,
//                                       String ordererName, String ordererAddr)
//             throws InvalidArgumentException {
//         //初始化peer配置需要用到orderer的 tls证书、orderer名称、orderer地址
//         Properties ordererProp = new Properties();
//         ordererProp.setProperty("pemFile", ordererTlsPath);
//         ordererProp.setProperty("negotiationType", "TLS");
//         ordererProp.setProperty("hostnameOverride", ordererName);
//         ordererProp.setProperty("trustServerCertificate", "true");
//         //用以解决grpc通讯交易长度限制 设置为10M
//         ordererProp.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 10 * 1024 * 1024);
//         return client.newOrderer(ordererName, ordererAddr, ordererProp);
//     }


//     /**
//      * 初始化已存在的channel
//      *
//      * @param client
//      * @param channelName
//      * @return
//      * @throws InvalidArgumentException
//      * @throws TransactionException
//      */
//     private Channel initializeChannel(HFClient client, String channelName) throws InvalidArgumentException, TransactionException {
//         //初始化channel需要使用到channel名称、初始化好的peer、
//         // 另外如果交易需要共识，则需要初始化orderer
//         Channel channel = client.newChannel(channelName);
//         channel.addOrderer(initializeOrderer(client, ordererTlsPath, ordererName, ordererAddr));
//         channel.addPeer(initializePeer(client, peerTlsPath, peerName, peerAddr));
//         channel.initialize();
//         return channel;
//     }
// }
