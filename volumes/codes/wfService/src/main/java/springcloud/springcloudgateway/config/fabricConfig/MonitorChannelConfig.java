package springcloud.springcloudgateway.config.fabricConfig;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: 李浩然
 * @Date: 2021/4/26 9:12 下午
 */
// @Component
// public class MonitorChannelConfig {
//     @Value(value = "${fabric.consensus.nodeNum}")
//     private int nodeNum;
//     @Value(value = "${fabric.consensus.nodeSum}")
//     private int nodeSum;
//     private int timeLimit = 7;
//     @Value("#{'${fabric.consensus.ipList:}'.empty ? null : '${fabric.consensus.ipList:}'.split(',')}")
//     private List<String> ipList;


//     private static volatile Channel monitorChannel = null;

//     public int getNodeNum() {
//         return nodeNum;
//     }

//     public int getNodeSum() {
//         return nodeSum;
//     }

//     public void setNodeSum(int nodeSum) {
//         this.nodeSum = nodeSum;
//     }

//     public int getTimeLimit() {
//         return timeLimit;
//     }

//     public void setTimeLimit(int timeLimit) {
//         this.timeLimit = timeLimit;
//     }

//     public List<String> getIpList() {
//         return ipList;
//     }

//     public static Channel getMonitorChannel(HFClient client, String channelName) throws Exception {
//         if (monitorChannel == null) {
//             synchronized (MonitorChannelConfig.class) {
//                 if (monitorChannel == null) {
//                     monitorChannel = client.newChannel(channelName);
//                 }
//             }
//         }
//         return monitorChannel;
//     }
// }
