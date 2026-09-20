package com.trade.virtual_trade.service;

public interface BalanceService {
    // 扣减余额（支付）
    boolean deductBalance(Integer userId, Double amount);
    // 增加余额（充值、确认收货）
    boolean addBalance(Integer userId, Double amount);
    // 冻结余额（支付时冻结，确认收货时解冻）
    boolean freezeBalance(Integer userId, Double amount);
    // 解冻余额并转到卖家
    boolean unfreezeToSeller(Integer buyerId, Integer sellerId, Double amount);
}