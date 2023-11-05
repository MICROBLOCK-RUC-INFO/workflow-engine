// package springcloud.springcloudgateway.config.fabricConfig;

// import org.hyperledger.fabric.sdk.Channel;
// import org.hyperledger.fabric.sdk.HFClient;
// import org.springframework.stereotype.Component;

// /**
//  * @Author: 李浩然
//  * @Date: 2021/4/26 9:12 下午
//  */
// @Component
// public class RegisterChannelConfig {
//     private static volatile Channel registerChannel = null;

//     public RegisterChannelConfig() {
//     }


//     public static Channel getRegisterChannel(HFClient client, String channelName) throws Exception {
//         if (registerChannel == null) {
//             synchronized (RegisterChannelConfig.class) {
//                 if (registerChannel == null) {
//                     registerChannel = client.newChannel(channelName);
//                 }
//             }
//         }
//         return registerChannel;
//     }
// }
