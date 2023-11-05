-- 创建监控数据表
CREATE TABLE `sentinel_metric1` (
  `app` VARCHAR(100) NOT NULL COMMENT '应用名称',
  `reqtime` timestamp NOT NULL COMMENT '统计时间',
  `resource` VARCHAR(500) NOT NULL COMMENT '资源名称',
  `totalRequest` INT COMMENT '全部请求数',
  `totalSuccess` INT COMMENT '成功请求数',
  `totalException` INT COMMENT '异常请求数',
  `rt_avg` DOUBLE COMMENT '所有successQps的rt的平均值',
  `rt_min` DOUBLE COMMENT '所有successQps的rt的最小值',
  `success_qps` INT COMMENT '成功qps',
  `exception_qps` INT COMMENT '异常qps',
  INDEX app_idx(`app`) USING BTREE,
  INDEX resource_idx(`resource`) USING BTREE,
  INDEX reqtime_idx(`reqtime`) USING BTREE,
  PRIMARY KEY (`app`,`reqtime`,`resource`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;