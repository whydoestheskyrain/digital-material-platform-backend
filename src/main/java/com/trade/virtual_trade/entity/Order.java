package com.trade.virtual_trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Integer id;
    private String orderNo;
    private Integer productId;
    private Integer buyerId;
    private Integer sellerId;
    private BigDecimal price;
    private Integer status; // 1待付款 / 2已付款 / 3已发货 / 4已完成 / 5已取消
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private String accountInfo; // 卖家填写的账号信息
    private String paymentMethod;   // balance / alipay
    private String tradeNo;         // 支付宝交易号（若用支付宝支付）
    
    // 非数据库字段，用于前端展示
    private String statusDesc;      // 状态文字描述（待支付、待发货等）
    private String statusColor;     // 状态颜色（十六进制颜色值）
}