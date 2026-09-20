package com.trade.virtual_trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionLog {
    private Long id;
    private Integer userId;          // 用户ID
    private BigDecimal amount;       // 变动金额（正为增加，负为减少）
    private String type;             // recharge / pay / income / refund
    private Long orderId;            // 关联订单ID（充值订单或交易订单）
    private BigDecimal balanceBefore; // 变动前余额
    private BigDecimal balanceAfter;  // 变动后余额
    private String remark;           // 备注
    private LocalDateTime createTime;
}