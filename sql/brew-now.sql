-- MySQL dump 10.13  Distrib 9.6.0, for macos26.3 (arm64)
--
-- Host: localhost    Database: brew-now
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `address_id` int NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '市',
  `district` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区',
  `detail_address` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tag` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址标签',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`address_id`),
  KEY `idx_addresses_user` (`user_id`),
  KEY `idx_addresses_user_default` (`user_id`,`is_default`),
  KEY `idx_addresses_user_deleted` (`user_id`,`deleted_at`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (1,2,'李先齐','18075950460','北京市','北京市','朝阳区','望京街道望京SOHO塔三 1208','公司',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(2,2,'李先齐','18075950460','北京市','北京市','海淀区','中关村软件园二期 8 号楼','家',0,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(3,20,'陈明','13811110001','北京市','北京市','西城区','金融大街15号证券大厦702','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(4,20,'陈明','13811110001','北京市','北京市','朝阳区','三里屯路19号','公司',0,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(5,21,'刘芳','13811110002','上海市','上海市','静安区','南京西路1266号恒隆广场B座','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(6,22,'赵磊','13811110003','浙江省','杭州市','西湖区','文三路477号','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(7,23,'孙媛','13811110004','四川省','成都市','锦江区','东大街139号中粮广场','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(8,24,'吴浩','13811110005','湖北省','武汉市','洪山区','珞喻路1037号华中科技大学','公司',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(9,25,'郑霞','13811110006','广东省','广州市','天河区','体育西路103号维多利广场','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(10,26,'杨波','13811110007','广东省','深圳市','南山区','科技园南区TCL大厦','公司',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(11,27,'许静','13811110008','江苏省','南京市','鼓楼区','中山路汉中门大街交叉口','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(12,28,'何鹏','13811110009','重庆市','重庆市','渝中区','解放碑步行街1号','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(13,29,'罗燕','13811110010','陕西省','西安市','雁塔区','科技路南段99号','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL),(14,30,'唐伟','13811110011','江苏省','苏州市','工业园区','星湖街328号苏州文化艺术中心','家',1,'2026-03-31 12:30:18','2026-03-31 12:30:18',NULL);
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admins` (
  `admin_id` int NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('SUPER_ADMIN','ADMIN','OPERATOR') COLLATE utf8mb4_unicode_ci DEFAULT 'ADMIN' COMMENT '角色',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `mobile_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_admins_role` (`role`),
  KEY `idx_admins_status` (`status`),
  KEY `idx_admins_last_login` (`last_login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admins`
--

LOCK TABLES `admins` WRITE;
/*!40000 ALTER TABLE `admins` DISABLE KEYS */;
INSERT INTO `admins` VALUES (1,'admin','123456','SUPER_ADMIN','系统管理员','13800138000','admin@brewnow.tea','ACTIVE','2025-06-07 09:31:02','2026-03-31 12:00:00'),(2,'manager','123456','ADMIN','商品管理员','13800138001','manager@brewnow.tea','ACTIVE','2025-06-07 09:31:02',NULL),(3,'operator','123456','OPERATOR','运营专员','13800138002','operator@brewnow.tea','ACTIVE','2025-06-07 09:31:02',NULL);
/*!40000 ALTER TABLE `admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `cart_item_id` int NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
  `cart_id` int NOT NULL COMMENT '购物车ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `add_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`cart_item_id`),
  UNIQUE KEY `uk_cart_product` (`cart_id`,`product_id`),
  KEY `fk_cart_items_product_id` (`product_id`),
  KEY `idx_cart_items_cart_product` (`cart_id`,`product_id`),
  KEY `idx_cart_items_add_time` (`add_time`),
  CONSTRAINT `fk_cart_items_cart_id` FOREIGN KEY (`cart_id`) REFERENCES `shopping_carts` (`cart_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_items_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (2,2,1,1,'2025-06-07 09:31:42'),(3,2,5,2,'2025-06-07 09:31:42'),(4,2,8,1,'2025-06-07 09:31:42'),(5,3,2,1,'2025-06-07 09:31:42'),(6,3,6,1,'2025-06-07 09:31:42'),(7,3,12,2,'2025-06-07 09:31:42'),(8,4,3,2,'2025-06-07 09:31:42'),(9,4,10,1,'2025-06-07 09:31:42'),(10,4,16,1,'2025-06-07 09:31:42'),(50,50,35,1,'2026-03-31 12:33:56');
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchants`
--

DROP TABLE IF EXISTS `merchants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchants` (
  `merchant_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家唯一标识',
  `user_id` int NOT NULL COMMENT '关联的用户ID',
  `company_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业名称',
  `business_license` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业执照号',
  `contact_person` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `business_address` text COLLATE utf8mb4_unicode_ci COMMENT '经营地址',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '商家描述',
  `status` enum('PENDING','APPROVED','REJECTED','SUSPENDED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '商家状态',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `approve_time` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `admin_id` int DEFAULT NULL COMMENT '管理员ID（审批该商家的管理员）',
  PRIMARY KEY (`merchant_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_merchants_admin_id` (`admin_id`),
  CONSTRAINT `fk_merchants_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchants`
--

LOCK TABLES `merchants` WRITE;
/*!40000 ALTER TABLE `merchants` DISABLE KEYS */;
INSERT INTO `merchants` VALUES ('BREW001',11,'沏刻茶业有限公司','123531234123','张经理','13800138888','北京市朝阳区科技园区A座','专注原产地茶叶与茶具零售','APPROVED','2025-06-07 13:49:51','2025-06-08 09:43:22'),('BREW002',12,'云岭茶坊有限公司','213522312321','李经理','13800138777','上海市浦东新区科技路123号','新申请入驻的茶叶品牌商家','REJECTED','2025-06-07 13:50:06','2025-06-08 07:02:53'),('BREW003',40,'云南茗源茶业有限公司','530100202301001234','王茗','13800138666','云南省普洱市思茅区茶源路88号','深耕云南普洱茶原产地，提供高品质古树茶与山头茶','APPROVED','2026-01-10 02:00:00','2026-01-12 06:30:00');
/*!40000 ALTER TABLE `merchants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` int NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '产品数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '产品单价',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计',
  `product_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名快照',
  `brand_snapshot` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌快照',
  `category_snapshot` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类快照',
  `image_url_snapshot` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片快照',
  PRIMARY KEY (`order_item_id`),
  KEY `idx_order_items_order_product` (`order_id`,`product_id`),
  KEY `idx_order_items_product_quantity` (`product_id`,`quantity`),
  CONSTRAINT `fk_order_items_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_items_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=250 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (18,8,1,1,199.00,199.00,NULL,NULL,NULL,NULL),(19,8,5,2,99.00,198.00,NULL,NULL,NULL,NULL),(20,8,8,1,149.00,149.00,NULL,NULL,NULL,NULL),(21,9,2,1,159.00,159.00,NULL,NULL,NULL,NULL),(22,9,6,1,79.00,79.00,NULL,NULL,NULL,NULL),(23,9,12,2,59.00,118.00,NULL,NULL,NULL,NULL),(24,10,3,2,89.00,178.00,NULL,NULL,NULL,NULL),(25,10,10,1,179.00,179.00,NULL,NULL,NULL,NULL),(26,10,16,1,699.00,699.00,NULL,NULL,NULL,NULL),(31,13,6,3,89.00,267.00,NULL,NULL,NULL,NULL),(32,13,7,3,129.00,387.00,NULL,NULL,NULL,NULL),(33,14,7,1,129.00,129.00,NULL,NULL,NULL,NULL),(34,14,6,1,89.00,89.00,NULL,NULL,NULL,NULL),(35,15,8,2,99.00,198.00,NULL,NULL,NULL,NULL),(36,15,7,2,129.00,258.00,NULL,NULL,NULL,NULL),(37,15,6,1,89.00,89.00,NULL,NULL,NULL,NULL),(200,20,1,1,139.00,139.00,'明前西湖龙井 100g','沏刻','绿茶','http://127.0.0.1:9000/brew-now/products/tea_001.jpg'),(201,20,17,1,238.00,238.00,'大红袍 岩茶特级 100g','沏刻','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_017.jpg'),(202,20,16,1,128.00,128.00,'陈皮普洱 150g','云岭','普洱茶','http://127.0.0.1:9000/brew-now/products/tea_016.jpg'),(203,21,2,1,118.00,118.00,'祁门红茶 特级 150g','山岚','红茶','http://127.0.0.1:9000/brew-now/products/tea_002.jpg'),(204,21,18,1,268.00,268.00,'金骏眉 红茶 100g','山岚','红茶','http://127.0.0.1:9000/brew-now/products/tea_018.jpg'),(205,22,20,2,176.00,352.00,'碧螺春 特级 100g','沏刻','绿茶','http://127.0.0.1:9000/brew-now/products/tea_020.jpg'),(206,22,21,1,298.00,298.00,'太平猴魁 礼盒 80g','山岚','绿茶','http://127.0.0.1:9000/brew-now/products/tea_021.jpg'),(207,22,15,1,108.00,108.00,'桂花乌龙 125g','山岚','花茶','http://127.0.0.1:9000/brew-now/products/tea_015.jpg'),(208,22,11,1,-29.00,-29.00,'折扣优惠',NULL,NULL,NULL),(209,23,7,1,268.00,268.00,'云南古树普洱熟茶饼 357g','茗源','普洱茶','http://127.0.0.1:9000/brew-now/products/tea_007.jpg'),(210,24,3,1,168.00,168.00,'安溪铁观音 兰花香 125g','云岭','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_003.jpg'),(211,24,9,1,188.00,188.00,'凤凰单丛 鸭屎香 100g','云岭','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_009.jpg'),(212,24,15,1,108.00,108.00,'桂花乌龙 125g','山岚','花茶','http://127.0.0.1:9000/brew-now/products/tea_015.jpg'),(213,25,4,1,229.00,229.00,'福鼎白毫银针 一级 100g','山岚','白茶','http://127.0.0.1:9000/brew-now/products/tea_004.jpg'),(214,25,11,1,146.00,146.00,'白牡丹 2022春茶 125g','沏刻','白茶','http://127.0.0.1:9000/brew-now/products/tea_011.jpg'),(215,26,33,1,268.00,268.00,'东方美人 乌龙 100g','沏刻','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_033.jpg'),(216,26,10,2,208.00,416.00,'岩韵花香乌龙 150g','山岚','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_010.jpg'),(217,26,8,2,88.00,176.00,'茉莉花茶 绿雪芽 100g','沏刻','花茶','http://127.0.0.1:9000/brew-now/products/tea_008.jpg'),(218,27,3,1,168.00,168.00,'安溪铁观音 兰花香 125g','云岭','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_003.jpg'),(219,28,18,1,268.00,268.00,'金骏眉 红茶 100g','山岚','红茶','http://127.0.0.1:9000/brew-now/products/tea_018.jpg'),(220,28,2,1,118.00,118.00,'祁门红茶 特级 150g','山岚','红茶','http://127.0.0.1:9000/brew-now/products/tea_002.jpg'),(221,28,34,2,86.00,172.00,'阿萨姆奶茶红茶基底 200g','山岚','红茶','http://127.0.0.1:9000/brew-now/products/tea_034.jpg'),(222,29,21,1,298.00,298.00,'太平猴魁 礼盒 80g','山岚','绿茶','http://127.0.0.1:9000/brew-now/products/tea_021.jpg'),(223,30,28,1,218.00,218.00,'老寿眉 饼茶 300g','云岭','白茶','http://127.0.0.1:9000/brew-now/products/tea_028.jpg'),(224,30,27,2,138.00,276.00,'福鼎白牡丹 一级 100g','山岚','白茶','http://127.0.0.1:9000/brew-now/products/tea_027.jpg'),(225,30,29,2,69.00,138.00,'胎菊花茶 80g','沏刻','花茶','http://127.0.0.1:9000/brew-now/products/tea_029.jpg'),(226,31,22,1,152.00,152.00,'信阳毛尖 春茶 100g','云岭','绿茶','http://127.0.0.1:9000/brew-now/products/tea_022.jpg'),(227,31,13,1,159.00,159.00,'六安瓜片 春茶 120g','云岭','绿茶','http://127.0.0.1:9000/brew-now/products/tea_013.jpg'),(228,31,30,1,76.00,76.00,'桂圆红枣花茶 120g','山岚','花茶','http://127.0.0.1:9000/brew-now/products/tea_030.jpg'),(229,32,7,1,268.00,268.00,'云南古树普洱熟茶饼 357g','茗源','普洱茶','http://127.0.0.1:9000/brew-now/products/tea_007.jpg'),(230,32,31,1,236.00,236.00,'普洱生茶 饼 357g','茗源','普洱茶','http://127.0.0.1:9000/brew-now/products/tea_031.jpg'),(231,33,17,1,238.00,238.00,'大红袍 岩茶特级 100g','沏刻','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_017.jpg'),(232,34,25,2,156.00,312.00,'水仙岩茶 100g','云岭','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_025.jpg'),(233,34,26,1,178.00,178.00,'单丛蜜兰香 100g','沏刻','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_026.jpg'),(234,35,35,1,189.00,189.00,'雨前龙井 200g 家庭装','云岭','绿茶','http://127.0.0.1:9000/brew-now/products/tea_035.jpg'),(235,35,30,1,76.00,76.00,'桂圆红枣花茶 120g','山岚','花茶','http://127.0.0.1:9000/brew-now/products/tea_030.jpg'),(236,36,33,1,268.00,268.00,'东方美人 乌龙 100g','沏刻','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_033.jpg'),(237,37,17,1,238.00,238.00,'大红袍 岩茶特级 100g','沏刻','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_017.jpg'),(238,37,4,1,229.00,229.00,'福鼎白毫银针 一级 100g','山岚','白茶','http://127.0.0.1:9000/brew-now/products/tea_004.jpg'),(239,38,20,1,176.00,176.00,'碧螺春 特级 100g','沏刻','绿茶','http://127.0.0.1:9000/brew-now/products/tea_020.jpg'),(240,39,1,1,139.00,139.00,'明前西湖龙井 100g','沏刻','绿茶','http://127.0.0.1:9000/brew-now/products/tea_001.jpg'),(241,39,12,1,198.00,198.00,'安吉白茶 明前 100g','山岚','绿茶','http://127.0.0.1:9000/brew-now/products/tea_012.jpg'),(242,40,5,2,98.00,196.00,'武夷山正山小种 120g','沏刻','红茶','http://127.0.0.1:9000/brew-now/products/tea_005.jpg'),(243,40,19,1,116.00,116.00,'滇红松针 125g','云岭','红茶','http://127.0.0.1:9000/brew-now/products/tea_019.jpg'),(244,40,24,1,126.00,126.00,'坦洋工夫 红茶 150g','山岚','红茶','http://127.0.0.1:9000/brew-now/products/tea_024.jpg'),(245,41,10,1,208.00,208.00,'岩韵花香乌龙 150g','山岚','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_010.jpg'),(246,41,3,1,168.00,168.00,'安溪铁观音 兰花香 125g','云岭','乌龙茶','http://127.0.0.1:9000/brew-now/products/tea_003.jpg'),(247,42,4,1,229.00,229.00,'福鼎白毫银针 一级 100g','山岚','白茶','http://127.0.0.1:9000/brew-now/products/tea_004.jpg'),(248,43,20,1,176.00,176.00,'碧螺春 特级 100g','沏刻','绿茶','http://127.0.0.1:9000/brew-now/products/tea_020.jpg'),(249,43,22,1,152.00,152.00,'信阳毛尖 春茶 100g','云岭','绿茶','http://127.0.0.1:9000/brew-now/products/tea_022.jpg');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_order_item_subtotal_insert` BEFORE INSERT ON `order_items` FOR EACH ROW BEGIN
    SET NEW.subtotal = NEW.quantity * NEW.unit_price;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_update_order_total_insert` AFTER INSERT ON `order_items` FOR EACH ROW BEGIN
    UPDATE `orders`
    SET `total_amount` = (SELECT COALESCE(SUM(subtotal), 0) FROM `order_items` WHERE `order_id` = NEW.order_id)
    WHERE `order_id` = NEW.order_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_order_item_subtotal_update` BEFORE UPDATE ON `order_items` FOR EACH ROW BEGIN
    SET NEW.subtotal = NEW.quantity * NEW.unit_price;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_update_order_total_update` AFTER UPDATE ON `order_items` FOR EACH ROW BEGIN
    UPDATE `orders`
    SET `total_amount` = (SELECT COALESCE(SUM(subtotal), 0) FROM `order_items` WHERE `order_id` = NEW.order_id)
    WHERE `order_id` = NEW.order_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_update_order_total_delete` AFTER DELETE ON `order_items` FOR EACH ROW BEGIN
    UPDATE `orders`
    SET `total_amount` = (SELECT COALESCE(SUM(subtotal), 0) FROM `order_items` WHERE `order_id` = OLD.order_id)
    WHERE `order_id` = OLD.order_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `order_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
  `order_status` enum('PENDING','PAID','SHIPPED','DELIVERED','CANCELLED','REFUNDED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '订单状态',
  `payment_method` enum('ALIPAY','WECHAT','CASH','CREDIT_CARD') COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式',
  `order_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单日期',
  `shipping_address` text COLLATE utf8mb4_unicode_ci COMMENT '配送地址',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `remark` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `stock_deducted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已扣减库存',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间（软删除）',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `order_number` (`order_number`),
  KEY `idx_orders_user_date` (`user_id`,`order_date`),
  KEY `idx_orders_status` (`order_status`),
  KEY `idx_orders_payment_method` (`payment_method`),
  KEY `idx_orders_date` (`order_date`),
  KEY `idx_orders_amount` (`total_amount`),
  KEY `idx_orders_user_status` (`user_id`,`order_status`),
  CONSTRAINT `fk_orders_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (8,2,'ORD20240607001',546.00,'PAID','ALIPAY','2025-06-07 09:32:00','北京市朝阳区xxx街道xxx号','13900139001','请尽快发货',1,NULL),(9,3,'ORD20240607002',356.00,'PAID','WECHAT','2025-06-07 09:32:00','上海市浦东新区xxx路xxx号','13900139002','工作日配送',1,NULL),(10,4,'ORD20240607003',1056.00,'PENDING','CREDIT_CARD','2025-06-07 09:32:00','广州市天河区xxx大道xxx号','13900139003','',0,NULL),(13,10,'ORD20250608203608335',654.00,'PAID','ALIPAY','2025-06-08 12:36:09','asdasdasda','15720801803','asdasd',1,NULL),(14,10,'ORD20250608205650502',218.00,'PAID','WECHAT','2025-06-08 12:56:51','aasda asdasdasddsdasdasd','15297364836','asdasdad',1,NULL),(15,10,'ORD20250608221239371',545.00,'PAID','ALIPAY','2025-06-08 14:12:40','啊啊啊啊啊大苏打撒旦','13229837321','设定',1,NULL),(20,20,'ORD20260110001',505.00,'DELIVERED','ALIPAY','2026-01-10 02:30:00','北京市西城区金融大街15号证券大厦702','13811110001','上班时间送货',1,NULL),(21,21,'ORD20260112001',386.00,'DELIVERED','WECHAT','2026-01-12 06:00:00','上海市静安区南京西路1266号恒隆广场B座','13811110002','轻放门口',1,NULL),(22,22,'ORD20260115001',729.00,'DELIVERED','ALIPAY','2026-01-15 01:20:00','浙江省杭州市西湖区文三路477号','13811110003','',1,NULL),(23,23,'ORD20260118001',268.00,'DELIVERED','CREDIT_CARD','2026-01-18 08:45:00','四川省成都市锦江区东大街139号中粮广场','13811110004','礼品包装',1,NULL),(24,24,'ORD20260120001',464.00,'DELIVERED','WECHAT','2026-01-20 03:10:00','湖北省武汉市洪山区珞喻路1037号','13811110005','',1,NULL),(25,25,'ORD20260122001',375.00,'DELIVERED','ALIPAY','2026-01-22 07:30:00','广东省广州市天河区体育西路103号','13811110006','',1,NULL),(26,26,'ORD20260125001',860.00,'DELIVERED','CREDIT_CARD','2026-01-25 05:00:00','广东省深圳市南山区科技园南区TCL大厦','13811110007','公司地址',1,NULL),(27,27,'ORD20260128001',168.00,'DELIVERED','WECHAT','2026-01-28 02:00:00','江苏省南京市鼓楼区中山路','13811110008','',1,NULL),(28,28,'ORD20260201001',558.00,'DELIVERED','ALIPAY','2026-02-01 06:20:00','重庆市渝中区解放碑步行街1号','13811110009','',1,NULL),(29,29,'ORD20260205001',298.00,'DELIVERED','WECHAT','2026-02-05 01:50:00','陕西省西安市雁塔区科技路南段99号','13811110010','',1,NULL),(30,30,'ORD20260210001',632.00,'SHIPPED','ALIPAY','2026-02-10 08:00:00','江苏省苏州市工业园区星湖街328号','13811110011','',1,NULL),(31,31,'ORD20260212001',387.00,'SHIPPED','WECHAT','2026-02-12 03:30:00','福建省厦门市思明区中山路','13811110012','',1,NULL),(32,32,'ORD20260215001',504.00,'SHIPPED','CREDIT_CARD','2026-02-15 06:00:00','湖南省长沙市岳麓区麓山南路','13811110013','',1,NULL),(33,33,'ORD20260220001',238.00,'PAID','ALIPAY','2026-02-20 02:10:00','福建省福州市鼓楼区五四路','13811110014','',1,NULL),(34,34,'ORD20260225001',490.00,'PAID','WECHAT','2026-02-25 07:20:00','安徽省合肥市蜀山区翡翠路','13811110015','',1,NULL),(35,35,'ORD20260301001',265.00,'PAID','ALIPAY','2026-03-01 01:30:00','山东省济南市历下区解放路','13811110016','',1,NULL),(36,36,'ORD20260310001',268.00,'PENDING',NULL,'2026-03-10 06:00:00','河南省郑州市金水区花园路','13811110017','',0,NULL),(37,37,'ORD20260315001',467.00,'PENDING',NULL,'2026-03-15 03:00:00','天津市南开区白堤路','13811110018','',0,NULL),(38,38,'ORD20260320001',176.00,'CANCELLED','ALIPAY','2026-03-20 02:00:00','浙江省宁波市海曙区灵桥路','13811110019','不要了',0,NULL),(39,2,'ORD20260325001',337.00,'DELIVERED','ALIPAY','2026-03-25 06:30:00','北京市朝阳区xxx街道xxx号1','13900139001','',1,NULL),(40,3,'ORD20260325002',438.00,'DELIVERED','WECHAT','2026-03-26 02:00:00','上海市浦东新区xxx路xxx号','13900139002','',1,NULL),(41,20,'ORD20260328001',376.00,'PAID','ALIPAY','2026-03-28 08:00:00','北京市西城区金融大街15号','13811110001','',1,NULL),(42,21,'ORD20260329001',229.00,'SHIPPED','WECHAT','2026-03-29 01:00:00','上海市静安区南京西路1266号','13811110002','',1,NULL),(43,22,'ORD20260330001',328.00,'PAID','CREDIT_CARD','2026-03-30 03:30:00','浙江省杭州市西湖区文三路477号','13811110003','',1,NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_manage_stock_on_payment` AFTER UPDATE ON `orders` FOR EACH ROW BEGIN
    IF OLD.order_status != 'PAID' AND NEW.order_status = 'PAID' THEN
        UPDATE `products` p INNER JOIN `order_items` oi ON p.product_id = oi.product_id
        SET p.stock_quantity = p.stock_quantity - oi.quantity WHERE oi.order_id = NEW.order_id;
    END IF;
    IF OLD.order_status = 'PAID' AND (NEW.order_status = 'CANCELLED' OR NEW.order_status = 'REFUNDED') THEN
        UPDATE `products` p INNER JOIN `order_items` oi ON p.product_id = oi.product_id
        SET p.stock_quantity = p.stock_quantity + oi.quantity WHERE oi.order_id = NEW.order_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `product_reviews`
--

DROP TABLE IF EXISTS `product_reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_reviews` (
  `review_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `order_item_id` int NOT NULL COMMENT '订单项ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `rating` int NOT NULL COMMENT '评分1-5',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评论内容',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uk_product_reviews_order_item` (`order_item_id`),
  KEY `idx_product_reviews_product_time` (`product_id`,`created_at`),
  KEY `idx_product_reviews_user_time` (`user_id`,`created_at`),
  KEY `fk_product_reviews_order` (`order_id`),
  CONSTRAINT `fk_product_reviews_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_product_reviews_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`order_item_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_product_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_product_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_reviews`
--

LOCK TABLES `product_reviews` WRITE;
/*!40000 ALTER TABLE `product_reviews` DISABLE KEYS */;
INSERT INTO `product_reviews` VALUES (100,20,200,20,1,5,'明前龙井，豆香浓郁，茶汤清亮！买了好几次了，每次都很满意，包装也很精致。','2026-01-15 06:00:00','2026-03-31 12:30:18',NULL),(101,20,201,20,17,5,'大红袍名不虚传，岩韵十足，焙火恰到好处，泡了七八泡还有味道！','2026-01-15 06:10:00','2026-03-31 12:30:18',NULL),(102,21,203,21,2,4,'祁门红茶蜜香明显，口感比较顺滑，就是感觉分量少了一点。','2026-01-17 02:00:00','2026-03-31 12:30:18',NULL),(103,21,204,21,18,5,'金骏眉真的是买过最好喝的红茶！送礼收到的人也很喜欢，以后还会回购。','2026-01-17 02:15:00','2026-03-31 12:30:18',NULL),(104,22,205,22,20,5,'碧螺春花果香真的绝了，刚开罐香气就扑鼻，性价比超高，推荐！','2026-01-20 08:00:00','2026-03-31 12:30:18',NULL),(105,22,206,22,21,4,'太平猴魁外形漂亮，兰花香突出，就是价格稍高，但确实是好茶。','2026-01-20 08:20:00','2026-03-31 12:30:18',NULL),(106,23,209,23,7,5,'古树普洱！陈香醇厚，汤感顺滑如丝，已经是第三次购买了，必须五星！','2026-01-22 03:00:00','2026-03-31 12:30:18',NULL),(107,24,210,24,3,4,'铁观音兰花香不错，但感觉这批的火候比上次偏轻了一些。总体还好。','2026-01-25 01:00:00','2026-03-31 12:30:18',NULL),(108,24,211,24,9,5,'鸭屎香名字吓到我了，但喝完真的香！高扬花果蜜香，回甘超快！','2026-01-25 01:20:00','2026-03-31 12:30:18',NULL),(109,25,213,25,4,5,'白毫银针颜值超高！芽头壮实，毫香鲜甜，汤色浅金剔透，非常喜欢。','2026-01-27 06:00:00','2026-03-31 12:30:18',NULL),(110,26,215,26,33,5,'东方美人是我见过最美的乌龙茶！蜜香果香层次丰富，每一泡都有惊喜。','2026-01-30 07:00:00','2026-03-31 12:30:18',NULL),(111,26,216,26,10,4,'岩韵乌龙不错，焙火香与花香搭配得好，就是库存显示紧张，希望保持供货。','2026-01-30 07:15:00','2026-03-31 12:30:18',NULL),(112,27,218,27,3,5,'安溪铁观音正宗！兰花香清晰，三泡四泡依然保持甜感，非常耐泡。','2026-02-02 02:00:00','2026-03-31 12:30:18',NULL),(113,28,219,28,18,5,'金骏眉品质稳定，每次买都一样好，蜜甜香久久不散，值得回购。','2026-02-06 03:00:00','2026-03-31 12:30:18',NULL),(114,28,220,28,2,4,'祁门红茶整体不错，香气馥郁，但包装可以再用心一些。','2026-02-06 03:20:00','2026-03-31 12:30:18',NULL),(115,29,222,29,21,5,'太平猴魁这次买来送领导，领导很满意！外形挺直漂亮，香气独特。','2026-02-10 01:00:00','2026-03-31 12:30:18',NULL),(116,39,240,2,1,5,'又来买龙井了，老店家品质稳定，明前的豆香每年都让人期待。','2026-03-27 06:00:00','2026-03-31 12:30:18',NULL),(117,40,242,3,5,4,'正山小种松烟香刚入口有点不习惯，但越喝越有味道，做奶茶底也很棒。','2026-03-28 02:00:00','2026-03-31 12:30:18',NULL),(118,40,244,3,24,5,'坦洋工夫味道很正！花果香柔和，家里人也很喜欢。','2026-03-28 02:30:00','2026-03-31 12:30:18',NULL);
/*!40000 ALTER TABLE `product_reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属商家ID',
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品牌',
  `category` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '种类/分类',
  `tea_tags` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origin_place` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产地',
  `flavor_profile` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock_quantity` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `warning_stock` int NOT NULL DEFAULT '10' COMMENT '库存预警阈值',
  `compatible_devices` text COLLATE utf8mb4_unicode_ci COMMENT '规格',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片URL',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  `status` enum('ACTIVE','INACTIVE','DISCONTINUED') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '商品状态',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `admin_id` int DEFAULT NULL COMMENT '管理员ID（审核/管理该商品的管理员）',
  PRIMARY KEY (`product_id`),
  KEY `idx_products_category_brand` (`category`,`brand`),
  KEY `idx_products_price` (`price`),
  KEY `idx_products_stock` (`stock_quantity`),
  KEY `idx_products_status` (`status`),
  KEY `idx_products_status_deleted` (`status`,`is_deleted`),
  KEY `idx_products_name` (`product_name`),
  KEY `idx_products_create_time` (`create_time`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_products_origin_place` (`origin_place`),
  KEY `idx_products_warning_stock` (`warning_stock`,`stock_quantity`),
  KEY `idx_products_category_status` (`category`,`status`),
  KEY `idx_products_status_stock` (`status`,`stock_quantity`),
  KEY `idx_products_admin_id` (`admin_id`),
  CONSTRAINT `fk_products_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'BREW001','明前西湖龙井 100g','沏刻','绿茶','鲜爽,豆香,清饮','浙江杭州','鲜爽,豆香,回甘',139.00,120,10,'100g礼盒','豆香清扬，鲜爽回甘，适合日常清饮。','http://127.0.0.1:9000/brew-now/products/tea_001.jpg',0,'ACTIVE','2025-06-07 09:28:13','2026-03-25 06:25:00'),(2,'BREW001','祁门红茶 特级 150g','山岚','红茶','醇厚,甜润,暖饮','安徽祁门','蜜香,醇厚,顺滑',118.00,160,10,'150g罐装','蜜糖香明显，口感醇厚顺滑，适合冬季热饮。','http://127.0.0.1:9000/brew-now/products/tea_002.jpg',0,'ACTIVE','2025-06-07 09:28:13','2026-03-25 06:25:00'),(3,'BREW001','安溪铁观音 兰花香 125g','云岭','乌龙茶','兰花香,焙火,回甜','福建安溪','兰花香,焙火轻柔,回甜',168.00,132,10,'125g罐装','兰花香显，焙火轻柔，三泡后仍有回甜。','http://127.0.0.1:9000/brew-now/products/tea_003.jpg',0,'ACTIVE','2025-06-07 09:28:13','2026-03-25 06:25:00'),(4,'BREW001','福鼎白毫银针 一级 100g','山岚','白茶','毫香,鲜甜,轻发酵','福建福鼎','毫香,鲜甜,柔和',229.00,58,10,'100g罐装','毫香鲜甜，汤色浅金，适合轻发酵茶爱好者。','http://127.0.0.1:9000/brew-now/products/tea_004.jpg',0,'ACTIVE','2025-06-07 09:28:13','2026-03-25 06:25:00'),(5,'BREW002','武夷山正山小种 120g','沏刻','红茶','松烟香,果香,奶茶','福建武夷山','松烟香,果香,醇厚',98.00,175,10,'120g袋装','松烟香与果香平衡，适合奶茶基底与纯饮。','http://127.0.0.1:9000/brew-now/products/tea_005.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(6,'BREW002','福鼎寿眉 2019年陈化 200g','云岭','白茶','陈香,甜润,入门','福建福鼎','陈香,甜润,顺口',156.00,89,10,'200g饼茶','陈香初显，甜润顺口，适合老白茶入门。','http://127.0.0.1:9000/brew-now/products/tea_006.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(7,'BREW002','云南古树普洱熟茶饼 357g','茗源','普洱茶','陈香,醇厚,耐泡','云南普洱','陈香,厚实,耐泡',268.00,74,10,'357g饼茶','陈香醇厚，汤感顺滑，耐泡度高。','http://127.0.0.1:9000/brew-now/products/tea_007.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(8,'BREW002','茉莉花茶 绿雪芽 100g','沏刻','花茶','花香,清雅,下午茶','福建福州','花香,清雅,鲜爽',88.00,142,10,'100g铁罐','七窨工艺，花香清雅，适合下午茶。','http://127.0.0.1:9000/brew-now/products/tea_008.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(9,'BREW002','凤凰单丛 鸭屎香 100g','云岭','乌龙茶','花果蜜香,回甘,盖碗','广东潮州','花果蜜香,回甘快',188.00,91,10,'100g牛皮纸袋','花果蜜香突出，回甘快，适合盖碗冲泡。','http://127.0.0.1:9000/brew-now/products/tea_009.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(10,'BREW003','岩韵花香乌龙 150g','山岚','乌龙茶','焙火,花香,层次','福建武夷山','焙火香,花香,层次丰富',208.00,68,10,'150g罐装','焙火香与花香并存，层次丰富。','http://127.0.0.1:9000/brew-now/products/tea_010.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(11,'BREW003','白牡丹 2022春茶 125g','沏刻','白茶','花香,甜感,办公','福建福鼎','花香,甜感,柔和',146.00,115,10,'125g袋装','花香细腻，甜感明显，适合办公室冲泡。','http://127.0.0.1:9000/brew-now/products/tea_011.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(12,'BREW003','安吉白茶 明前 100g','山岚','绿茶','鲜爽,豆香,清饮','浙江安吉','鲜爽,豆香,回甘',198.00,88,10,'100g礼盒','芽叶嫩匀，汤感清鲜，豆香明显。','http://127.0.0.1:9000/brew-now/products/tea_012.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(13,'BREW003','六安瓜片 春茶 120g','云岭','绿茶','鲜爽,栗香,耐泡','安徽六安','栗香,清亮,回甘',159.00,97,10,'120g罐装','栗香清晰，汤色清亮，回甘持久。','http://127.0.0.1:9000/brew-now/products/tea_013.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(14,'BREW003','玫瑰花茶组合 120g','沏刻','花茶','花香,女性,冷泡','云南昆明','花香,浓郁,轻柔',96.00,108,10,'独立袋泡30包','玫瑰香气浓郁，适合冷泡热泡双场景。','http://127.0.0.1:9000/brew-now/products/tea_014.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(15,'BREW001','桂花乌龙 125g','山岚','花茶','桂香,清雅,女性','福建漳州','桂香,清雅,干净',108.00,101,10,'125g罐装','桂香清雅，茶底干净，适合女性用户。','http://127.0.0.1:9000/brew-now/products/tea_015.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(16,'BREW001','陈皮普洱 150g','云岭','普洱茶','柑橘香,暖胃,日常','广东新会','柑橘香,陈香,暖胃',128.00,84,10,'150g罐装','柑橘清香与熟普陈香融合，暖胃顺口。','http://127.0.0.1:9000/brew-now/products/tea_016.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(17,'BREW001','大红袍 岩茶特级 100g','沏刻','乌龙茶','岩骨花香,礼赠,层次','福建武夷山','岩骨花香,层次感强',238.00,56,10,'100g罐装','岩骨花香突出，冲泡后层次感强。','http://127.0.0.1:9000/brew-now/products/tea_017.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(18,'BREW001','金骏眉 红茶 100g','山岚','红茶','蜜甜,礼赠,高端','福建武夷山','蜜甜香,细嫩,甜润',268.00,48,10,'100g礼盒','芽头细嫩，蜜甜香显，适合礼赠。','http://127.0.0.1:9000/brew-now/products/tea_018.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(19,'BREW001','滇红松针 125g','云岭','红茶','甜润,红亮,柔和','云南凤庆','甜润,红亮,柔和',116.00,124,10,'125g袋装','香气甜润，茶汤红亮，口感柔和。','http://127.0.0.1:9000/brew-now/products/tea_019.jpg',0,'ACTIVE','2025-06-07 09:31:33','2026-03-25 06:25:00'),(20,'BREW002','碧螺春 特级 100g','沏刻','绿茶','花果香,鲜爽,回甘','江苏苏州','花果香,鲜爽,回甘快',176.00,106,10,'100g铁罐','花果香馥郁，入口鲜爽，回甘快。','http://127.0.0.1:9000/brew-now/products/tea_020.jpg',0,'ACTIVE','2026-03-01 02:00:00','2026-03-25 06:25:00'),(21,'BREW002','太平猴魁 礼盒 80g','山岚','绿茶','兰花香,礼赠,商务','安徽黄山','兰花香,清甜,回甘',298.00,42,10,'80g礼盒','外形挺直，兰花香显，商务送礼首选。','http://127.0.0.1:9000/brew-now/products/tea_021.jpg',0,'ACTIVE','2026-03-01 02:05:00','2026-03-25 06:25:00'),(22,'BREW002','信阳毛尖 春茶 100g','云岭','绿茶','栗香,鲜爽,清饮','河南信阳','板栗香,鲜爽,回甘',152.00,96,10,'100g罐装','板栗香明显，鲜爽度高，适合清饮。','http://127.0.0.1:9000/brew-now/products/tea_022.jpg',0,'ACTIVE','2026-03-01 02:10:00','2026-03-25 06:25:00'),(23,'BREW002','正山小种 烟熏款 100g','沏刻','红茶','松烟香,浓烈,特色','福建武夷山','松烟香,浓郁,重口',108.00,119,10,'100g袋装','松烟风味浓郁，适合重口味用户。','http://127.0.0.1:9000/brew-now/products/tea_023.jpg',0,'ACTIVE','2026-03-01 02:15:00','2026-03-25 06:25:00'),(24,'BREW002','坦洋工夫 红茶 150g','山岚','红茶','花果香,厚实,家庭','福建福安','花果香,柔和,厚实',126.00,111,10,'150g罐装','花果香柔和，汤感厚实，适合家庭茶饮。','http://127.0.0.1:9000/brew-now/products/tea_024.jpg',0,'ACTIVE','2026-03-01 02:20:00','2026-03-25 06:25:00'),(25,'BREW002','水仙岩茶 100g','云岭','乌龙茶','木质香,花香,岩韵','福建武夷山','木质香,花香,岩韵明显',156.00,86,10,'100g罐装','木质香与花香并存，岩韵明显。','http://127.0.0.1:9000/brew-now/products/tea_025.jpg',0,'ACTIVE','2026-03-01 02:25:00','2026-03-25 06:25:00'),(26,'BREW003','单丛蜜兰香 100g','沏刻','乌龙茶','蜜兰香,甜润,多泡','广东潮州','蜜兰香,甜润,多泡',178.00,76,10,'100g袋装','蜜兰香浓郁，入口甜润，适配多泡。','http://127.0.0.1:9000/brew-now/products/tea_026.jpg',0,'ACTIVE','2026-03-01 02:30:00','2026-03-25 06:25:00'),(27,'BREW003','福鼎白牡丹 一级 100g','山岚','白茶','毫香,清甜,柔和','福建福鼎','毫香,清甜,柔和',138.00,102,10,'100g罐装','毫香清甜，花香细腻，口感柔和。','http://127.0.0.1:9000/brew-now/products/tea_027.jpg',0,'ACTIVE','2026-03-01 02:35:00','2026-03-25 06:25:00'),(28,'BREW003','老寿眉 饼茶 300g','云岭','白茶','陈香,煮饮,收藏','福建福鼎','陈香,甜润,顺口',218.00,54,10,'300g饼茶','转化陈香明显，适合煮饮。','http://127.0.0.1:9000/brew-now/products/tea_028.jpg',0,'ACTIVE','2026-03-01 02:40:00','2026-03-25 06:25:00'),(29,'BREW003','胎菊花茶 80g','沏刻','花茶','花香,清润,日常','浙江桐乡','花香,清润,明亮',69.00,140,10,'80g袋装','汤色清亮，花香明显，适合清润冲泡。','http://127.0.0.1:9000/brew-now/products/tea_029.jpg',0,'ACTIVE','2026-03-01 02:45:00','2026-03-25 06:25:00'),(30,'BREW003','桂圆红枣花茶 120g','山岚','花茶','甜香,温润,女性','福建莆田','甜香,温润,柔和',76.00,133,10,'120g袋装','甜香温润，适合女性与秋冬季饮用。','http://127.0.0.1:9000/brew-now/products/tea_030.jpg',0,'ACTIVE','2026-03-01 02:50:00','2026-03-25 06:25:00'),(31,'BREW001','普洱生茶 饼 357g','茗源','普洱茶','花香,回甘,收藏','云南勐海','花香,回甘,持久',236.00,63,10,'357g饼茶','花香高扬，回甘持久，适合长期存放。','http://127.0.0.1:9000/brew-now/products/tea_031.jpg',0,'ACTIVE','2026-03-01 02:55:00','2026-03-25 06:25:00'),(32,'BREW001','普洱熟茶 金砖 250g','云岭','普洱茶','陈香,厚实,口粮茶','云南临沧','陈香,厚实,浓郁',188.00,72,10,'250g砖茶','陈香浓郁，汤感厚实，适合重口粮茶。','http://127.0.0.1:9000/brew-now/products/tea_032.jpg',0,'ACTIVE','2026-03-01 03:00:00','2026-03-25 06:25:00'),(33,'BREW001','东方美人 乌龙 100g','沏刻','乌龙茶','蜜香,果香,礼赠','台湾新竹','蜜香,果香,层次丰富',268.00,39,10,'100g礼盒','蜜香明显，果香层次丰富，礼赠友好。','http://127.0.0.1:9000/brew-now/products/tea_033.jpg',0,'ACTIVE','2026-03-01 03:05:00','2026-03-25 06:25:00'),(34,'BREW001','阿萨姆奶茶红茶基底 200g','山岚','红茶','奶茶,浓烈,调配','印度阿萨姆','浓烈,饱满,调配',86.00,150,10,'200g袋装','适合搭配牛奶冲泡，风味饱满。','http://127.0.0.1:9000/brew-now/products/tea_034.jpg',0,'ACTIVE','2026-03-01 03:10:00','2026-03-25 06:25:00'),(35,'BREW001','雨前龙井 200g 家庭装','云岭','绿茶','鲜爽,豆香,日常','浙江杭州','鲜爽,豆香,稳定',189.00,80,10,'200g家庭装','性价比高，鲜爽口感稳定，适合日常囤货。','http://127.0.0.1:9000/brew-now/products/tea_035.jpg',0,'ACTIVE','2026-03-01 03:15:00','2026-03-25 06:25:00');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_carts`
--

DROP TABLE IF EXISTS `shopping_carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_carts` (
  `cart_id` int NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_shopping_carts_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_carts`
--

LOCK TABLES `shopping_carts` WRITE;
/*!40000 ALTER TABLE `shopping_carts` DISABLE KEYS */;
INSERT INTO `shopping_carts` VALUES (2,2,'2026-03-31 12:30:17','2026-03-31 12:30:17'),(3,3,'2026-03-31 12:30:17','2026-03-31 12:30:17'),(4,4,'2026-03-31 12:30:17','2026-03-31 12:30:17'),(10,10,'2026-03-31 12:30:17','2026-03-31 12:30:17'),(11,11,'2026-03-31 12:30:17','2026-03-31 12:30:17'),(12,12,'2026-03-31 12:30:17','2026-03-31 12:30:17'),(50,50,'2026-03-31 12:33:26','2026-03-31 12:33:26');
/*!40000 ALTER TABLE `shopping_carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_behavior_logs`
--

DROP TABLE IF EXISTS `user_behavior_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_behavior_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `behavior_type` enum('VIEW','FAVORITE','CART','PURCHASE') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行为类型',
  `behavior_weight` decimal(10,4) NOT NULL COMMENT '行为权重',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_behavior_user_time` (`user_id`,`created_at`),
  KEY `idx_behavior_product_time` (`product_id`,`created_at`),
  KEY `idx_behavior_user_product` (`user_id`,`product_id`),
  CONSTRAINT `fk_behavior_logs_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_behavior_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=600 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_behavior_logs`
--

LOCK TABLES `user_behavior_logs` WRITE;
/*!40000 ALTER TABLE `user_behavior_logs` DISABLE KEYS */;
INSERT INTO `user_behavior_logs` VALUES (500,20,1,'VIEW',1.0000,'2026-01-05 02:00:00'),(501,20,1,'FAVORITE',2.0000,'2026-01-06 02:00:00'),(502,20,1,'CART',3.0000,'2026-01-08 01:00:00'),(503,20,1,'PURCHASE',4.0000,'2026-01-10 02:30:00'),(504,20,17,'VIEW',1.0000,'2026-01-05 02:05:00'),(505,20,17,'FAVORITE',2.0000,'2026-01-06 02:05:00'),(506,20,17,'PURCHASE',4.0000,'2026-01-10 02:30:00'),(507,20,16,'VIEW',1.0000,'2026-01-07 03:00:00'),(508,20,16,'PURCHASE',4.0000,'2026-01-10 02:30:00'),(509,20,20,'VIEW',1.0000,'2026-01-09 06:00:00'),(510,20,20,'FAVORITE',2.0000,'2026-01-10 07:30:00'),(511,20,10,'VIEW',1.0000,'2026-01-10 08:00:00'),(512,20,3,'VIEW',1.0000,'2026-01-12 02:00:00'),(513,20,10,'CART',3.0000,'2026-03-28 07:00:00'),(514,20,3,'CART',3.0000,'2026-03-28 07:05:00'),(515,20,10,'PURCHASE',4.0000,'2026-03-28 08:00:00'),(516,20,3,'PURCHASE',4.0000,'2026-03-28 08:00:00'),(517,21,2,'VIEW',1.0000,'2026-01-07 03:00:00'),(518,21,2,'FAVORITE',2.0000,'2026-01-08 03:00:00'),(519,21,2,'PURCHASE',4.0000,'2026-01-12 06:00:00'),(520,21,18,'VIEW',1.0000,'2026-01-07 03:10:00'),(521,21,18,'FAVORITE',2.0000,'2026-01-08 03:10:00'),(522,21,18,'PURCHASE',4.0000,'2026-01-12 06:00:00'),(523,21,4,'VIEW',1.0000,'2026-01-10 01:00:00'),(524,21,4,'FAVORITE',2.0000,'2026-01-12 01:00:00'),(525,21,4,'CART',3.0000,'2026-03-29 00:00:00'),(526,21,4,'PURCHASE',4.0000,'2026-03-29 01:00:00'),(527,21,7,'VIEW',1.0000,'2026-01-15 04:00:00'),(528,21,17,'VIEW',1.0000,'2026-01-15 04:10:00'),(529,22,20,'VIEW',1.0000,'2026-01-14 06:00:00'),(530,22,20,'CART',3.0000,'2026-01-14 06:30:00'),(531,22,20,'PURCHASE',4.0000,'2026-01-15 01:20:00'),(532,22,21,'VIEW',1.0000,'2026-01-14 06:05:00'),(533,22,21,'PURCHASE',4.0000,'2026-01-15 01:20:00'),(534,22,12,'VIEW',1.0000,'2026-01-13 06:00:00'),(535,22,12,'FAVORITE',2.0000,'2026-01-13 06:00:00'),(536,22,12,'PURCHASE',4.0000,'2026-03-30 03:30:00'),(537,22,20,'VIEW',1.0000,'2026-03-29 02:00:00'),(538,22,22,'VIEW',1.0000,'2026-03-29 02:05:00'),(539,22,22,'PURCHASE',4.0000,'2026-03-30 03:30:00'),(540,23,7,'VIEW',1.0000,'2026-01-16 02:00:00'),(541,23,7,'FAVORITE',2.0000,'2026-01-16 02:00:00'),(542,23,7,'CART',3.0000,'2026-01-17 01:00:00'),(543,23,7,'PURCHASE',4.0000,'2026-01-18 08:45:00'),(544,23,31,'VIEW',1.0000,'2026-01-17 03:00:00'),(545,23,31,'FAVORITE',2.0000,'2026-01-17 03:00:00'),(546,23,32,'VIEW',1.0000,'2026-01-17 03:10:00'),(547,23,16,'VIEW',1.0000,'2026-01-18 01:00:00'),(548,24,3,'VIEW',1.0000,'2026-01-17 04:00:00'),(549,24,3,'FAVORITE',2.0000,'2026-01-18 04:00:00'),(550,24,3,'PURCHASE',4.0000,'2026-01-20 03:10:00'),(551,24,9,'VIEW',1.0000,'2026-01-18 05:00:00'),(552,24,9,'FAVORITE',2.0000,'2026-01-19 05:00:00'),(553,24,9,'PURCHASE',4.0000,'2026-01-20 03:10:00'),(554,24,25,'VIEW',1.0000,'2026-01-18 05:30:00'),(555,24,26,'VIEW',1.0000,'2026-01-18 05:40:00'),(556,24,15,'VIEW',1.0000,'2026-01-19 02:00:00'),(557,24,15,'PURCHASE',4.0000,'2026-01-20 03:10:00'),(558,25,4,'VIEW',1.0000,'2026-01-19 06:00:00'),(559,25,4,'FAVORITE',2.0000,'2026-01-20 06:00:00'),(560,25,4,'PURCHASE',4.0000,'2026-01-22 07:30:00'),(561,25,28,'VIEW',1.0000,'2026-01-20 07:00:00'),(562,25,28,'FAVORITE',2.0000,'2026-01-21 07:00:00'),(563,25,11,'VIEW',1.0000,'2026-01-21 02:00:00'),(564,25,11,'PURCHASE',4.0000,'2026-01-22 07:30:00'),(565,25,6,'VIEW',1.0000,'2026-01-21 03:00:00'),(566,26,33,'VIEW',1.0000,'2026-01-23 08:00:00'),(567,26,33,'FAVORITE',2.0000,'2026-01-22 08:00:00'),(568,26,33,'PURCHASE',4.0000,'2026-01-25 05:00:00'),(569,26,10,'VIEW',1.0000,'2026-01-22 09:00:00'),(570,26,10,'FAVORITE',2.0000,'2026-01-23 09:00:00'),(571,26,10,'PURCHASE',4.0000,'2026-01-25 05:00:00'),(572,26,8,'VIEW',1.0000,'2026-01-24 01:00:00'),(573,26,8,'PURCHASE',4.0000,'2026-01-25 05:00:00'),(574,26,17,'VIEW',1.0000,'2026-01-24 02:00:00'),(575,2,1,'VIEW',1.0000,'2026-03-24 02:00:00'),(576,2,1,'FAVORITE',2.0000,'2026-03-01 02:00:00'),(577,2,1,'CART',3.0000,'2026-03-25 01:00:00'),(578,2,1,'PURCHASE',4.0000,'2026-03-25 06:30:00'),(579,2,12,'VIEW',1.0000,'2026-03-24 02:10:00'),(580,2,12,'PURCHASE',4.0000,'2026-03-25 06:30:00'),(581,2,20,'VIEW',1.0000,'2026-03-01 03:00:00'),(582,2,20,'FAVORITE',2.0000,'2026-03-02 03:00:00'),(583,2,17,'VIEW',1.0000,'2026-03-03 04:00:00'),(584,2,17,'FAVORITE',2.0000,'2026-03-05 04:00:00'),(585,3,2,'VIEW',1.0000,'2026-03-09 01:00:00'),(586,3,2,'FAVORITE',2.0000,'2026-03-10 01:00:00'),(587,3,2,'PURCHASE',4.0000,'2026-03-26 02:00:00'),(588,3,18,'VIEW',1.0000,'2026-03-10 02:00:00'),(589,3,18,'FAVORITE',2.0000,'2026-03-11 02:00:00'),(590,3,5,'VIEW',1.0000,'2026-03-25 01:00:00'),(591,3,5,'PURCHASE',4.0000,'2026-03-26 02:00:00'),(592,3,19,'VIEW',1.0000,'2026-03-25 01:10:00'),(593,3,19,'PURCHASE',4.0000,'2026-03-26 02:00:00'),(594,3,24,'VIEW',1.0000,'2026-03-25 01:20:00'),(595,3,24,'PURCHASE',4.0000,'2026-03-26 02:00:00'),(596,50,35,'VIEW',1.0000,'2026-03-31 12:33:49'),(597,50,35,'VIEW',1.0000,'2026-03-31 12:33:50'),(598,50,35,'CART',3.0000,'2026-03-31 12:33:56'),(599,50,35,'VIEW',1.0000,'2026-03-31 12:34:07');
/*!40000 ALTER TABLE `user_behavior_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_favorites`
--

DROP TABLE IF EXISTS `user_favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '取消收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_favorites_user_product` (`user_id`,`product_id`),
  KEY `idx_user_favorites_user_deleted` (`user_id`,`deleted_at`),
  KEY `idx_user_favorites_product_deleted` (`product_id`,`deleted_at`),
  CONSTRAINT `fk_user_favorites_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_user_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_favorites`
--

LOCK TABLES `user_favorites` WRITE;
/*!40000 ALTER TABLE `user_favorites` DISABLE KEYS */;
INSERT INTO `user_favorites` VALUES (100,20,1,'2026-01-06 02:00:00',NULL),(101,20,17,'2026-01-06 02:05:00',NULL),(102,20,20,'2026-01-10 07:30:00',NULL),(103,21,2,'2026-01-08 03:00:00',NULL),(104,21,18,'2026-01-08 03:10:00',NULL),(105,21,4,'2026-01-12 01:00:00',NULL),(106,22,12,'2026-01-13 06:00:00',NULL),(107,22,21,'2026-01-15 08:00:00',NULL),(108,23,7,'2026-01-16 02:00:00',NULL),(109,23,31,'2026-01-17 03:00:00',NULL),(110,24,3,'2026-01-18 04:00:00',NULL),(111,24,9,'2026-01-19 05:00:00',NULL),(112,25,4,'2026-01-20 06:00:00',NULL),(113,25,28,'2026-01-21 07:00:00',NULL),(114,26,33,'2026-01-22 08:00:00',NULL),(115,26,10,'2026-01-23 09:00:00',NULL),(116,27,3,'2026-01-24 01:00:00',NULL),(117,28,18,'2026-02-01 02:00:00',NULL),(118,29,21,'2026-02-05 03:00:00',NULL),(119,2,1,'2026-03-01 02:00:00',NULL),(120,2,20,'2026-03-02 03:00:00',NULL),(121,2,17,'2026-03-05 04:00:00',NULL),(122,3,2,'2026-03-10 01:00:00',NULL),(123,3,18,'2026-03-11 02:00:00',NULL);
/*!40000 ALTER TABLE `user_favorites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `gender` enum('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT 'OTHER' COMMENT '性别',
  `register_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `address` text COLLATE utf8mb4_unicode_ci COMMENT '地址',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间（软删除）',
  `role` enum('CONSUMER','MERCHANT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CONSUMER',
  `merchant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家唯一标识',
  `admin_id` int DEFAULT NULL COMMENT '管理员ID（审核/管理该用户的管理员）',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `account` (`account`),
  UNIQUE KEY `merchant_id` (`merchant_id`),
  KEY `idx_users_username` (`username`),
  KEY `idx_users_register_time` (`register_time`),
  KEY `idx_users_admin_id` (`admin_id`),
  CONSTRAINT `fk_users_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'user001','张三','123456','MALE','2025-06-07 09:31:09','13900139001','zhangsan@email.com','北京市朝阳区xxx街道xxx号1',NULL,NULL,'CONSUMER',NULL),(3,'user002','李四','123456','FEMALE','2025-06-07 09:31:09','13900139002','lisi@email.com','上海市浦东新区xxx路xxx号',NULL,NULL,'CONSUMER',NULL),(4,'user003','王五','123456','MALE','2025-06-07 09:31:09','13900139003','wangwu@email.com','广州市天河区xxx大道xxx号',NULL,NULL,'CONSUMER',NULL),(10,'13900139009','手机用户009','123456','MALE','2025-06-07 13:49:33','13900139009','abcc3thb6kg1@outlook.com',NULL,NULL,NULL,'CONSUMER',NULL),(11,'BREW001','商家用户001','123456','MALE','2025-06-07 13:49:41','13800138888','2335319149@qq.com','南京',NULL,NULL,'MERCHANT','BREW001'),(12,'BREW002','商家用户002','123456','OTHER','2025-06-07 13:49:56','13800138777','zhangsan@email.com','aaaaa',NULL,NULL,'MERCHANT','BREW002'),(20,'chen_ming','陈明','123456','MALE','2026-01-05 01:12:00','13811110001','chenming@163.com','北京市西城区金融大街15号',NULL,NULL,'CONSUMER',NULL),(21,'liu_fang','刘芳','123456','FEMALE','2026-01-08 06:30:00','13811110002','liufang@qq.com','上海市静安区南京西路1266号',NULL,NULL,'CONSUMER',NULL),(22,'zhao_lei','赵磊','123456','MALE','2026-01-12 03:05:00','13811110003','zhaolei@sina.com','杭州市西湖区文三路477号',NULL,NULL,'CONSUMER',NULL),(23,'sun_yuan','孙媛','123456','FEMALE','2026-01-15 08:22:00','13811110004','sunyuan@outlook.com','成都市锦江区东大街139号',NULL,NULL,'CONSUMER',NULL),(24,'wu_hao','吴浩','123456','MALE','2026-01-18 00:45:00','13811110005','wuhao@126.com','武汉市洪山区珞喻路1037号',NULL,NULL,'CONSUMER',NULL),(25,'zheng_xia','郑霞','123456','FEMALE','2026-01-20 12:11:00','13811110006','zhengxia@gmail.com','广州市天河区体育西路',NULL,NULL,'CONSUMER',NULL),(26,'yang_bo','杨波','123456','MALE','2026-01-25 05:37:00','13811110007','yangbo@163.com','深圳市南山区科技园南区',NULL,NULL,'CONSUMER',NULL),(27,'xu_jing','许静','123456','FEMALE','2026-02-01 02:20:00','13811110008','xujing@qq.com','南京市鼓楼区中山路',NULL,NULL,'CONSUMER',NULL),(28,'he_peng','何鹏','123456','MALE','2026-02-05 09:55:00','13811110009','hepeng@sina.com','重庆市渝中区解放碑步行街',NULL,NULL,'CONSUMER',NULL),(29,'luo_yan','罗燕','123456','FEMALE','2026-02-08 01:30:00','13811110010','luoyan@126.com','西安市雁塔区科技路',NULL,NULL,'CONSUMER',NULL),(30,'tang_wei','唐伟','123456','MALE','2026-02-12 06:15:00','13811110011','tangwei@163.com','苏州市工业园区星湖街328号',NULL,NULL,'CONSUMER',NULL),(31,'gao_lin','高琳','123456','FEMALE','2026-02-15 03:40:00','13811110012','gaolin@qq.com','厦门市思明区中山路',NULL,NULL,'CONSUMER',NULL),(32,'peng_tao','彭涛','123456','MALE','2026-02-18 08:05:00','13811110013','pengtao@outlook.com','长沙市岳麓区麓山南路',NULL,NULL,'CONSUMER',NULL),(33,'xiao_hui','肖慧','123456','FEMALE','2026-02-22 00:55:00','13811110014','xiaohui@gmail.com','福建省福州市鼓楼区五四路',NULL,NULL,'CONSUMER',NULL),(34,'jiang_chao','蒋超','123456','MALE','2026-03-01 02:30:00','13811110015','jiangchao@163.com','安徽省合肥市蜀山区翡翠路',NULL,NULL,'CONSUMER',NULL),(35,'ma_yue','马悦','123456','FEMALE','2026-03-05 07:20:00','13811110016','mayue@qq.com','山东省济南市历下区解放路',NULL,NULL,'CONSUMER',NULL),(36,'qian_long','钱龙','123456','MALE','2026-03-10 01:45:00','13811110017','qianlong@sina.com','河南省郑州市金水区花园路',NULL,NULL,'CONSUMER',NULL),(37,'hu_mei','胡梅','123456','FEMALE','2026-03-15 06:00:00','13811110018','humei@126.com','天津市南开区白堤路',NULL,NULL,'CONSUMER',NULL),(38,'lin_kai','林凯','123456','MALE','2026-03-20 03:25:00','13811110019','linkai@163.com','浙江省宁波市海曙区灵桥路',NULL,NULL,'CONSUMER',NULL),(39,'fan_xiao','范晓','123456','FEMALE','2026-03-25 08:50:00','13811110020','fanxiao@qq.com','江苏省无锡市梁溪区中山路',NULL,NULL,'CONSUMER',NULL),(40,'BREW003','商家用户003','123456','MALE','2026-01-10 02:00:00','13800138666','brew003@brewnow.tea','云南省普洱市思茅区',NULL,NULL,'MERCHANT','BREW003'),(50,'Echo','Echo','$2a$10$pZbS6fLx07vMuVjB0jAK.Oa.FOGnOb9Mc1RvZYP5X2ObgGdqDxgQK',NULL,'2026-03-31 12:33:26','18075950460','3423554221@qq.com',NULL,'http://127.0.0.1:9000/brew-now/avatars/avatar_50_9ced0516e6c543d583c8e3137c9f40e1.jpg',NULL,'CONSUMER',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_user_create_cart` AFTER INSERT ON `users` FOR EACH ROW BEGIN
    INSERT INTO `shopping_carts` (`user_id`) VALUES (NEW.user_id);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Temporary view structure for view `view_product_rating_stats`
--

DROP TABLE IF EXISTS `view_product_rating_stats`;
/*!50001 DROP VIEW IF EXISTS `view_product_rating_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `view_product_rating_stats` AS SELECT 
 1 AS `product_id`,
 1 AS `product_name`,
 1 AS `price`,
 1 AS `stock_quantity`,
 1 AS `review_count`,
 1 AS `avg_rating`,
 1 AS `positive_count`,
 1 AS `neutral_count`,
 1 AS `negative_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `view_user_purchase_history`
--

DROP TABLE IF EXISTS `view_user_purchase_history`;
/*!50001 DROP VIEW IF EXISTS `view_user_purchase_history`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `view_user_purchase_history` AS SELECT 
 1 AS `user_id`,
 1 AS `username`,
 1 AS `order_id`,
 1 AS `order_number`,
 1 AS `order_date`,
 1 AS `order_status`,
 1 AS `total_amount`,
 1 AS `item_count`,
 1 AS `total_quantity`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'brew-now'
--
/*!50003 DROP PROCEDURE IF EXISTS `cleanup_soft_deleted_data` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `cleanup_soft_deleted_data`()
BEGIN
    DECLARE deleted_count INT DEFAULT 0;

    DELETE FROM addresses WHERE deleted_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    SET deleted_count = deleted_count + ROW_COUNT();

    DELETE FROM user_favorites WHERE deleted_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    SET deleted_count = deleted_count + ROW_COUNT();

    SELECT CONCAT('清理完成, 共删除 ', deleted_count, ' 条记录') AS result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Final view structure for view `view_product_rating_stats`
--

/*!50001 DROP VIEW IF EXISTS `view_product_rating_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `view_product_rating_stats` AS select `p`.`product_id` AS `product_id`,`p`.`product_name` AS `product_name`,`p`.`price` AS `price`,`p`.`stock_quantity` AS `stock_quantity`,count(`pr`.`review_id`) AS `review_count`,round(avg(`pr`.`rating`),1) AS `avg_rating`,count((case when (`pr`.`rating` >= 4) then 1 end)) AS `positive_count`,count((case when (`pr`.`rating` = 3) then 1 end)) AS `neutral_count`,count((case when (`pr`.`rating` <= 2) then 1 end)) AS `negative_count` from (`products` `p` left join `product_reviews` `pr` on((`p`.`product_id` = `pr`.`product_id`))) group by `p`.`product_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `view_user_purchase_history`
--

/*!50001 DROP VIEW IF EXISTS `view_user_purchase_history`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `view_user_purchase_history` AS select `u`.`user_id` AS `user_id`,`u`.`username` AS `username`,`o`.`order_id` AS `order_id`,`o`.`order_number` AS `order_number`,`o`.`order_date` AS `order_date`,`o`.`order_status` AS `order_status`,`o`.`total_amount` AS `total_amount`,count(`oi`.`order_item_id`) AS `item_count`,sum(`oi`.`quantity`) AS `total_quantity` from ((`users` `u` join `orders` `o` on((`u`.`user_id` = `o`.`user_id`))) join `order_items` `oi` on((`o`.`order_id` = `oi`.`order_id`))) group by `u`.`user_id`,`o`.`order_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-31 20:39:13
