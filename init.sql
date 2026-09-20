-- 创建数据库
CREATE DATABASE IF NOT EXISTS trade_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE trade_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色：user/admin',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 游戏类型表
CREATE TABLE IF NOT EXISTS `game_type` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '游戏类型ID',
  `type_name` VARCHAR(50) NOT NULL COMMENT '游戏类型名称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏类型表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
  `seller_id` INT NOT NULL COMMENT '卖家ID',
  `game_type_id` INT COMMENT '游戏类型ID',
  `game_type` VARCHAR(50) COMMENT '游戏类型名称',
  `title` VARCHAR(200) NOT NULL COMMENT '商品标题',
  `game_server` VARCHAR(50) COMMENT '区服',
  `role_level` INT DEFAULT 0 COMMENT '角色等级',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `description` TEXT COMMENT '商品描述',
  `detail_desc` TEXT COMMENT '商品详情描述（富文本）',
  `detail_images` TEXT COMMENT '商品详情图片（JSON数组）',
  `game_account` VARCHAR(100) COMMENT '游戏账号',
  `game_password` VARCHAR(100) COMMENT '游戏密码',
  `bind_info` VARCHAR(200) COMMENT '绑定信息',
  `status` INT DEFAULT 0 COMMENT '状态：0待审核 1已上架 2拒绝 3下架',
  `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `product_id` INT NOT NULL COMMENT '商品ID',
  `buyer_id` INT NOT NULL COMMENT '买家ID',
  `seller_id` INT NOT NULL COMMENT '卖家ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `status` INT DEFAULT 1 COMMENT '状态：1待付款 2已付款 3已发货 4已完成 5已取消',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `pay_time` DATETIME COMMENT '支付时间',
  `payment_method` VARCHAR(20) COMMENT '支付方式：balance/alipay',
  `trade_no` VARCHAR(100) COMMENT '支付宝交易号',
  `account_info` TEXT COMMENT '账号信息（卖家发货时填写）',
  FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`buyer_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 交易流水表
CREATE TABLE IF NOT EXISTS `transaction` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '流水ID',
  `order_no` VARCHAR(50) COMMENT '订单号',
  `buyer_id` INT COMMENT '买家ID',
  `seller_id` INT COMMENT '卖家ID',
  `money` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `type` INT COMMENT '类型：1收入 2支出',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`buyer_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易流水表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  `user_id` INT COMMENT '接收用户ID（NULL表示广播）',
  `type` VARCHAR(20) NOT NULL COMMENT '消息类型：order/feedback/broadcast/system',
  `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `order_id` INT COMMENT '关联订单ID',
  `status` INT DEFAULT 0 COMMENT '状态：0未读 1已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '反馈标题',
  `content` TEXT NOT NULL COMMENT '反馈内容',
  `status` INT DEFAULT 0 COMMENT '状态：0待处理 1已回复 2已关闭',
  `reply` TEXT COMMENT '管理员回复',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `reply_time` DATETIME COMMENT '回复时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

-- 插入初始游戏类型数据
INSERT INTO `game_type` (`type_name`) VALUES 
('王者荣耀'),
('英雄联盟'),
('和平精英'),
('我的世界'),
('原神'),
('崩坏')
ON DUPLICATE KEY UPDATE `type_name`=VALUES(`type_name`);

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `phone`, `role`) VALUES 
('admin', '123456', '13800138000', 'admin'),
('user1', '123456', '13800138001', 'user'),
('user2', '123456', '13800138002', 'user')
ON DUPLICATE KEY UPDATE `username`=VALUES(`username`);
