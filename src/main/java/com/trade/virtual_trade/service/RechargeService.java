package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.RechargeOrder;

public interface RechargeService {
    // 创建充值订单，返回支付宝支付表单HTML
    String createRecharge(Integer userId, Double amount) throws Exception;

    // 处理支付宝异步通知，更新订单状态并增加用户余额
    boolean handleAlipayNotify(String orderNo, String tradeNo);

    RechargeOrder getOrderByNo(String orderNo);
}