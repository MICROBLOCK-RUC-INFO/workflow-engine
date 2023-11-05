package springcloud.springcloudgateway.service.fabric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

// /**
//  * @Author: 李浩然
//  * @Date: 2021/1/2 12:39 上午
//  */
// public class FabricServiceTest {
//     static {
//         Security.addProvider(new BouncyCastleProvider());
//     }

//     @Autowired
//     private FabricServiceNacosImpl fabricService = new FabricServiceNacosImpl();
//     private Logger logger = LoggerFactory.getLogger(FabricServiceTest.class);
//     String txFilePath = "/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/hello-spring-cloud-alibaba-rocketmq-consumer/src/main/docker/channelTxs/";
//     String channelStringPath = "/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/hello-spring-cloud-alibaba-rocketmq-consumer/src/main/docker/channelString/";


//     void createChannel() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidArgumentException, IllegalAccessException, CryptoException, ClassNotFoundException, TransactionException {
//         String channelName = "softwarechannel";
//         String tx64String = Base64.getEncoder().encodeToString(FileUtils.readFile(txFilePath + channelName + ".tx"));
//         String channel64String = fabricService.createChannel(channelName, tx64String);
//         FileUtils.writeFile(channel64String, channelStringPath + channelName + ".txt", false);
//         logger.info("成功创建通道=【{}】", channel64String);
//     }

//     void joinChannel() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, ProposalException, InvalidArgumentException, IOException, CryptoException, ClassNotFoundException, TransactionException {
//         String channelName = "softwarechannel";
// //        String channelName = "channel202005231256";
//         String channel64String = new String(FileUtils.readFile(channelStringPath + channelName + ".txt"));
//         String peerName = "peer0.fabric.gfe.com";
//         boolean result = fabricService.joinChannel(channel64String, peerName);
//         logger.info("通道加入=【{}】", result);
//     }

//     void installChainCode() throws IllegalAccessException, InstantiationException, ProposalException, NoSuchMethodException, InvalidArgumentException, InvocationTargetException, CryptoException, ClassNotFoundException, TransactionException, IOException {
//         String channelName = "softwarechannel";
//         String chainCodeName = "nacos";
//         String chainCodeVersion = "1.0.0";
//         String chainCodePath = "/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/fabric-nodejs/artifacts";
//         String projectName = "github.com/nacos";
//         TransactionRequest.Type language = TransactionRequest.Type.GO_LANG;

//         Collection<ProposalResponse> proposals = fabricService.installChainCode(chainCodeName, chainCodeVersion, chainCodePath, projectName, language);
//         for (ProposalResponse response : proposals) {
//             if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
//                 logger.info("链码安装成功 Txid: {} from peer {}}", response.getTransactionID(), response.getPeer().getName());
//             } else {
//                 logger.info("链码安装失败");
//             }
//         }

//     }

//     void instantiantChainCode() throws IllegalAccessException, InstantiationException, ProposalException, NoSuchMethodException, InvalidArgumentException, InvocationTargetException, CryptoException, ClassNotFoundException, TransactionException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException {
//         String channelName = "softwarechannel";
//         String chainCodeName = "nacos";
//         String chainCodePath = "/Users/lihaoran/Desktop/project/IdeaProjects/demonacos/fabric-nodejs/artifacts";
//         String chainCodeVersion = "1.0.0";
//         String projectName = "github.com/nacos";
//         TransactionRequest.Type language = TransactionRequest.Type.GO_LANG;
//         ArrayList<String> args = new ArrayList<>();
//         Collection<ProposalResponse> proposals = fabricService.instantiantChainCode(channelName, chainCodeName, chainCodePath, chainCodeVersion, language, args);
//         for (ProposalResponse response : proposals) {
//             if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
//                 logger.info("链码成功实例化 Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), response.getChaincodeActionResponsePayload());
//             } else {
//                 logger.info("链码实例化失败 , reason: {}", response.getMessage());
//             }
//         }
//     }

//     void getHeight() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidArgumentException, CryptoException, ClassNotFoundException, TransactionException, ProposalException {
//         String channelName = "softwarechannel";
//         Long height = fabricService.getHeight(channelName);
//         logger.info("getHeight result= 【{}】", height);

//     }

//     void queryInstalledChaincodes() throws NoSuchMethodException, InvalidArgumentException, InstantiationException, ClassNotFoundException, ProposalException, IllegalAccessException, InvocationTargetException, CryptoException {
//         ArrayList<String> arrayList = fabricService.queryInstalledChaincodes();
//         arrayList.forEach(logger::info);

//     }

//     void queryInstantiatedChaincodes() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidArgumentException, CryptoException, ClassNotFoundException, TransactionException, ProposalException {
//         String channelName = "softwarechannel";
//         ArrayList<String> arrayList = fabricService.queryInstantiatedChaincodes(channelName);
//         arrayList.forEach(logger::info);
//     }

//     void query() throws Exception {
//         String channelName = "registerchannel";
//         String chainCodeName = "nacos";
//         String func = "QueryByKey";
//         ArrayList<String> params = new ArrayList<>();
//         params.add("test");

//         Collection<ProposalResponse> proposals = fabricService.query_test(channelName, chainCodeName, func, params);
//         for (ProposalResponse response : proposals) {
//             if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
//                 System.out.println(response.getChaincodeActionResponseStatus());
//                 for (TxReadWriteSetInfo.NsRwsetInfo info : response.getChaincodeActionResponseReadWriteSetInfo().getNsRwsetInfos()) {
//                     System.out.println("读集：：" + info.getRwset().getReadsList());
//                     System.out.println("写集：：" + info.getRwset().getWritesList());
//                 }
//                 logger.info("Query成功 Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
//                 logger.info("response:::" + response.getMessage());
//             } else {
//                 logger.info("Query失败, reason: {}", response.getMessage());
//             }
//         }
//     }

//     void invoke() throws Exception {
//         String channelName = "monitorchannel";
//         String chainCodeName = "monitor";
//         String func = "RangeQuery";
//         ArrayList<String> params = new ArrayList<>();
//         params.add("springcloud.monitor.");
//         params.add("springcloud.monitor0");

//         Collection<ProposalResponse> proposals = fabricService.invoke(channelName, chainCodeName, func, params);
//         for (ProposalResponse response : proposals) {
//             if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
//                 logger.info("Successful transaction proposal response Txid: {} from peer {}},payload: {}", response.getTransactionID(), response.getPeer().getName(), new String(response.getChaincodeActionResponsePayload()));
//                 System.out.println("response::");
//                 System.out.println(response.getChaincodeActionResponsePayload());
//             } else {
//                 logger.info("Faild tracsaction proposal response, reason: {}", response.getMessage());
//             }
//         }
//     }

//     public static void main(String[] args) throws Exception {
//         FabricServiceTest fabricServiceTest = new FabricServiceTest();
//         try {
// //            fabricServiceTest.invoke();
//             fabricServiceTest.query();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }