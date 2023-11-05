/*
 Navicat MySQL Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3306
 Source Schema         : new_test

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 25/03/2020 11:23:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for NEW_TEST
-- ----------------------------
DROP TABLE IF EXISTS `NEW_TEST`;
CREATE TABLE `NEW_TEST`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `age` int(0) UNSIGNED NOT NULL,
  `retime` timestamp(0) NOT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`retime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of NEW_TEST
-- ----------------------------
INSERT INTO `NEW_TEST` VALUES ('GUOSHUAI', 25, '2020-03-20 21:25:54');

-- ----------------------------
-- Table structure for sentinel_metric1
-- ----------------------------
DROP TABLE IF EXISTS `sentinel_metric1`;
CREATE TABLE `sentinel_metric1`  (
  `app` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '应用名称',
  `reqtime` timestamp(0) NOT NULL COMMENT '统计时间',
  `resource` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源名称',
  `totalRequest` int(0) NULL DEFAULT NULL COMMENT '全部请求数',
  `totalSuccess` int(0) NULL DEFAULT NULL COMMENT '成功请求数',
  `totalException` int(0) NULL DEFAULT NULL COMMENT '异常请求数',
  `rt_avg` double NULL DEFAULT NULL COMMENT '所有successQps的rt的平均值',
  `rt_min` double NULL DEFAULT NULL COMMENT '所有successQps的rt的最小值',
  `success_qps` int(0) NULL DEFAULT NULL COMMENT '成功qps',
  `exception_qps` int(0) NULL DEFAULT NULL COMMENT '异常qps',
  PRIMARY KEY (`app`, `reqtime`, `resource`) USING BTREE,
  INDEX `app_idx`(`app`) USING BTREE,
  INDEX `resource_idx`(`resource`) USING BTREE,
  INDEX `reqtime_idx`(`reqtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sentinel_metric1
-- ----------------------------
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:32:53', 'provider', 2, 2, 0, 4.5, 1, 2, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:32:58', 'provider', 1, 1, 0, 3, 3, 1, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:32:59', 'provider', 4, 4, 0, 3.5, 1, 4, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:00', 'provider', 1, 1, 0, 4, 4, 1, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:05', 'provider', 1, 1, 0, 5, 5, 1, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:06', 'provider', 4, 4, 0, 5, 1, 4, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:07', 'provider', 5, 5, 0, 3.8, 1, 5, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:47', 'provider', 3, 3, 0, 6, 1, 3, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:48', 'provider', 8, 8, 0, 5, 1, 8, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:49', 'provider', 6, 6, 0, 5, 1, 6, 0);
INSERT INTO `sentinel_metric1` VALUES ('null', '2020-03-24 10:33:50', 'provider', 1, 1, 0, 4, 4, 1, 0);

SET FOREIGN_KEY_CHECKS = 1;
