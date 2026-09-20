package com.trade.virtual_trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeOrder {
    private Long id;
    private String orderNo;          // 充值订单号
    private Integer userId;          // 用户ID
    private BigDecimal amount;       // 充值金额
    private Integer status;          // 0-待支付 1-支付成功 2-已取消
    private String paymentMethod;    // alipay / balance
    private String tradeNo;          // 支付宝交易号
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}