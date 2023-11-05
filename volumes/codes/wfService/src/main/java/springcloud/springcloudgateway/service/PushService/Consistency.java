// package springcloud.springcloudgateway.service.PushService;

// import springcloud.springcloudgateway.config.BlockchainConfig;

// import java.sql.*;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// public class Consistency {


//     public static Timestamp getTime() {
//         Date date = new Date();//获得系统时间.
//         String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);//将时间格式转换成符合Timestamp要求的格式.
//         Timestamp goodsC_date = Timestamp.valueOf(nowTime);//把时间转换
//         return goodsC_date; // 输出已经格式化的现在时间（24小时制）
//     }

//     public static void insert(String app, Timestamp reqtime, String resource, int totalRequest, int totalSuccess, int totalException, double rt_avg, double rt_min, int success_qps, int exception_qps) {
//         try {
//             //1.加载数据库驱动程序
//             Class.forName("com.mysql.jdbc.Driver");
//             //2.获取数据库的连接
//             Connection conn = DriverManager.getConnection(BlockchainConfig.URL, BlockchainConfig.USERNAME, BlockchainConfig.PASSWORD);
//             //3.构造SQL语句

//             String sql = "insert into sentinel_metric1 (app,reqtime,resource,totalRequest,totalSuccess,totalException,rt_avg,rt_min,success_qps,exception_qps) values('" + app + "','" + reqtime + "','" + resource + "'," + totalRequest + "," + totalSuccess + "," + totalException + "," + rt_avg + "," + rt_avg + "," + success_qps + "," + exception_qps + ") on DUPLICATE key update totalRequest=" + totalRequest + ",totalSuccess=" + totalSuccess + ",totalException=" + totalException + ",rt_avg=" + rt_avg + ",rt_min=" + rt_min + ",success_qps=" + success_qps + ",exception_qps=" + exception_qps;
// //            sql="INSERT INTO NEW_TEST(id,age,retime) VALUES('GUOSHUAI',13,'"+getTime()+"')";
//             System.out.println(getTime().toString());
//             //4.构造一个STATEMENT实例，用来发送SQL语句
//             Statement state = conn.createStatement();
//             //5.执行SQL语句
//             state.executeUpdate(sql);
//             //6.关闭连接
//             conn.close();
//             state.close();
//             System.out.println("insert success!");
//         }//加载驱动器时发生的异常
//         catch (ClassNotFoundException e) {
//             e.printStackTrace();
//             //获取数据连接时出现的异常
//         } catch (SQLException e) {
//             e.printStackTrace();
//         }
//     }

//     public static void insertMonitorData(Timestamp clickTime, Timestamp serviceTime, Timestamp blockchainTime, long clickTimeInt, long consumerTime, String service, String ipaddr, String host, String port, double serviceDelay, int blockchainDelay, int delay, int nums, String serviceCompleted, String blockchainCompleted, int clusterExpect, int clusterResult, int totalRequest, int totalException, int totalSuccess, double avgRt, int trueTotalRequest, int trueTotalException, int trueTotalSuccess, double trueAvgRt, String uuid) throws SQLException, ClassNotFoundException {
//         Connection conn = null;
//         Statement state = null;
//         try {
//             //1.加载数据库驱动程序
//             Class.forName("com.mysql.jdbc.Driver");
//             //2.获取数据库的连接
//             conn = DriverManager.getConnection(BlockchainConfig.URL, BlockchainConfig.USERNAME, BlockchainConfig.PASSWORD);
//             //3.构造SQL语句

//             String sql = "insert into monitor (clickTime,serviceTime,blockchainTime,clickTimeInt,consumerTime,service,ipaddr,host,port,serviceDelay,blockchainDelay,delay,nums,serviceCompleted,blockchainCompleted,clusterExpect,clusterResult,totalRequest,totalException,totalSuccess,avgRt,trueTotalRequest,trueTotalException,trueTotalSuccess,trueAvgRt,uuid) values('" + clickTime + "','" + serviceTime + "','" + blockchainTime + "','" + clickTimeInt + "','" + consumerTime + "','" + service + "','" + ipaddr + "','" + host + "','" + port + "'," + serviceDelay + "," + blockchainDelay + "," + delay + "," + nums + ",'" + serviceCompleted + "','" + blockchainCompleted + "','" + clusterExpect + "','" + clusterResult + "','" + totalRequest + "','" + totalException + "','" + totalSuccess + "','" + avgRt + "','" + trueTotalRequest + "','" + trueTotalException + "','" + trueTotalSuccess + "','" + trueAvgRt + "','" + uuid + "')";
// //            sql="INSERT INTO NEW_TEST(id,age,retime) VALUES('GUOSHUAI',13,'"+getTime()+"')";
//             //4.构造一个STATEMENT实例，用来发送SQL语句
//             state = conn.createStatement();
//             //5.执行SQL语句
//             state.executeUpdate(sql);
//         }//加载驱动器时发生的异常
//         catch (ClassNotFoundException e) {
//             e.printStackTrace();
//             //获取数据连接时出现的异常
//         } catch (SQLException e) {
//             e.printStackTrace();
//         } finally {
//             if (conn != null) {
//                 try {
//                     conn.close();
//                 } catch (SQLException e) {
//                     e.printStackTrace();
//                     throw new RuntimeException(e);
//                 }
//             }
//             if (state != null) {
//                 try {
//                     state.close();
//                 } catch (SQLException e) {
//                     e.printStackTrace();
//                     throw new RuntimeException(e);
//                 }
//             }
//         }
//     }

// }