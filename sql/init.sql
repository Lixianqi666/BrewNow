-- 沏刻茶叶电商平台数据库初始化脚本
-- 创建数据库用户（如果不存在）
CREATE USER IF NOT EXISTS 'brewnow_user'@'%' IDENTIFIED BY 'brewnow_pass';
GRANT ALL PRIVILEGES ON `brew-now`.* TO 'brewnow_user'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, EXECUTE, CREATE VIEW, SHOW VIEW, CREATE ROUTINE, ALTER ROUTINE, EVENT, TRIGGER ON `brew-now`.* TO 'brewnow_user'@'%';
FLUSH PRIVILEGES;

-- 使用数据库
USE `brew-now`;

-- 创建基础表结构（示例）
-- 注意：实际的表结构应该从现有数据库导出
-- 这里只创建一些基础的表结构

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `role` varchar(20) DEFAULT 'CONSUMER' COMMENT '角色',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 产品表
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '产品名称',
  `description` text COMMENT '产品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 插入示例数据
INSERT INTO `users` (`username`, `phone`, `password`, `role`, `status`) VALUES
('管理员', '18075950460', '$2a$10$YourHashedPassword', 'ADMIN', 1),
('测试用户', '13800138000', '$2a$10$YourHashedPassword', 'CONSUMER', 1)
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;

INSERT INTO `products` (`name`, `description`, `price`, `stock`, `category`, `image_url`) VALUES
('龙井茶', '正宗杭州龙井，清香甘醇', 128.00, 100, '绿茶', '/images/longjing.jpg'),
('普洱熟茶', '云南普洱，陈香浓郁', 89.00, 200, '黑茶', '/images/puer.jpg'),
('铁观音', '安溪铁观音，香气独特', 156.00, 150, '乌龙茶', '/images/tieguanyin.jpg'),
('大红袍', '武夷岩茶，岩韵明显', 268.00, 80, '乌龙茶', '/images/dahongpao.jpg')
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;

-- 创建索引
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(status);

-- 输出初始化完成信息
SELECT '数据库初始化完成！' AS message;