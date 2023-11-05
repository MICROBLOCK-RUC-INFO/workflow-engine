package springcloud.springcloudgateway.config.fabricConfig;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springcloud.springcloudgateway.service.fabric.FabricUserBean;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author: 李浩然
 * @Date: 2021/4/25 11:03 下午
 */
// @Component
// public class MonitorHfClient {

//     private static String userName;
//     private static String mspId;
//     private static String keyPath;
//     private static String crtPath;

//     @Value(value = "${fabric.username}")
//     public void setUserName(String userName) {
//         MonitorHfClient.userName = userName;
//     }

//     @Value(value = "${fabric.mspid}")
//     public void setMspId(String mspId) {
//         MonitorHfClient.mspId = mspId;
//     }

//     @Value(value = "${fabric.keypath}")
//     public void setKeyPath(String keyPath) {
//         MonitorHfClient.keyPath = keyPath;
//     }

//     @Value(value = "${fabric.crtpath}")
//     public void setCrtPath(String crtPath) {
//         MonitorHfClient.crtPath = crtPath;
//     }

//     private static volatile HFClient hfClient = null;

//     public static HFClient getInstance() throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {
//         if (hfClient == null) {
//             synchronized (HFClient.class) {
//                 if (hfClient == null) {
//                     hfClient = HFClient.createNewInstance();
//                     FabricUserBean fabricUserBean = new FabricUserBean(userName, mspId, crtPath, keyPath);
//                     hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
//                     hfClient.setUserContext(fabricUserBean);
//                 }
//             }
//         }
//         return hfClient;
//     }
// }
