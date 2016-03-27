/*
SQLyog Ultimate v9.01 
MySQL - 5.6.14 : Database - itsboard
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS many_dict_type;
CREATE TABLE many_dict_type (
  c_id varchar(32) NOT NULL  COMMENT '类型编码',
  c_name varchar(40) NOT NULL  COMMENT '类型名称',
  c_config varchar(500) COMMENT '扩展信息',
  c_order int(10)  COMMENT '排序号',
  PRIMARY KEY (c_id)
) ENGINE=InnoDB COMMENT='字典类型';


DROP TABLE IF EXISTS many_dict;
CREATE TABLE many_dict (
  c_id int(10) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  c_type_id varchar(32) NOT NULL  COMMENT '类型',
  c_name varchar(40) NOT NULL  COMMENT '名称',
  c_value varchar(500) NOT NULL  COMMENT '值',
  c_desc varchar(40) COMMENT '描述',
  c_order int(10)  COMMENT '排序号',
  c_json VARCHAR (500) COMMENT 'json格式扩展信息',
  c_tag1 VARCHAR (32) COMMENT '冗余字段1',
  c_tag2 VARCHAR (32) COMMENT '冗余字段2',
  PRIMARY KEY (c_id)
  ,CONSTRAINT con_dict_type FOREIGN KEY (c_type_id) REFERENCES many_dict_type (c_id) ON DELETE RESTRICT ON UPDATE CASCADE 
) ENGINE=InnoDB COMMENT='通用字典';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

