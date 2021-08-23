/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50734
 Source Host           : localhost:3306
 Source Schema         : sms

 Target Server Type    : MySQL
 Target Server Version : 50734
 File Encoding         : 65001

 Date: 18/08/2021 14:53:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sms_channel
-- ----------------------------

CREATE DATABASE IF NOT EXISTS sms default charset utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `sms_channel`;
CREATE TABLE `sms_channel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `chan_no` varchar(32) DEFAULT '' COMMENT '通道号',
  `chan_name` varchar(32) DEFAULT '' COMMENT '通道名',
  `sp_type` int(2) DEFAULT '0' COMMENT '运营商类型，0：移动，1：联通，2：电信，3：三网，4：国际',
  `protocol` int(2) DEFAULT '0' COMMENT '协议类型，0：移动cmppv2，1：移动cmppv3，2：sgip联通，3：电信smgp，4：http协议，5：国际smpp',
  `chan_type` smallint(1) DEFAULT '0' COMMENT '通道类型，0：国内通道，1：国际通道',
  `ip_addr` varchar(32) DEFAULT '' COMMENT 'IP 地址',
  `port` int(8) DEFAULT '7890' COMMENT '端口号',
  `username` varchar(32) DEFAULT '' COMMENT '用户名',
  `password` varchar(32) DEFAULT '' COMMENT '密码',
  `sp_number` varchar(32) DEFAULT NULL COMMENT '接入号',
  `sp_id` varchar(32) DEFAULT NULL COMMENT '企业代码',
  `speed` int(11) DEFAULT '500' COMMENT '最大速度',
  `enabled` smallint(1) DEFAULT '1' COMMENT '是否启用，0：禁用，1：启用',
  `max_connect` int(2) DEFAULT '5' COMMENT '最大连接数',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  `gmt_created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `modified_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_chan_no` (`chan_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='短信通道信息表';

-- ----------------------------
-- Records of sms_channel
-- ----------------------------
BEGIN;
INSERT INTO `sms_channel` VALUES (1, '100', '模拟通道', 3, 0, 0, '172.25.174.91', 7890, 'v2tes2', 'v2tes2', '1060001', 'v2tes2', 500, 1, 5, NULL, '2021-08-18 05:51:24', NULL, '2021-08-18 05:52:02', NULL);
COMMIT;

-- ----------------------------
-- Table structure for sms_send_msg
-- ----------------------------
DROP TABLE IF EXISTS `sms_send_msg`;
CREATE TABLE `sms_send_msg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `mid` bigint(20) NOT NULL COMMENT '短信唯一id',
  `chan_no` varchar(32) DEFAULT '' COMMENT '通道号',
  `mobile` varchar(20) DEFAULT '' COMMENT '手机号',
  `content` varchar(2056) DEFAULT '' COMMENT '短信内容',
  `extend` varchar(20) DEFAULT '' COMMENT '扩展码',
  `count` int(2) DEFAULT '1' COMMENT '短信拆分条数',
  `status` smallint(1) NOT NULL DEFAULT '0' COMMENT '短信状态，0：未知，1：成功，2：失败',
  `status_code` varchar(20) DEFAULT '' COMMENT '错误码',
  `send_date` int(8) NOT NULL COMMENT '发送日期，yyyyMMdd',
  `gmt_created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `modified_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_mid` (`mid`),
  KEY `idx_send_date_mobile` (`send_date`,`mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='短信记录表';


SET FOREIGN_KEY_CHECKS = 1;
