package com.trade.virtual_trade.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.trade.virtual_trade.config.AlipayConfig;
import com.trade.virtual_trade.entity.RechargeOrder;
import com.trade.virtual_trade.mapper.RechargeOrderMapper;
import com.trade.virtual_trade.service.RechargeService;
import com.trade.virtual_trade.service.BalanceService;
import com.trade.virtual_trade.service.TransactionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RechargeServiceImpl implements RechargeService {

    private static final Logger logger = LoggerFactory.getLogger(RechargeServiceImpl.class);

    @Autowired
    private RechargeOrderMapper rechargeOrderMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private TransactionLogService transactionLogService;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    public String createRecharge(Integer userId, Double amount) throws Exception {
        String orderNo = "RECHARGE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0,8);
        logger.info("创建充值订单，用户ID：{}，金额：{}，订单号：{}", userId, amount, orderNo);

        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAmount(BigDecimal.valueOf(amount));
        order.setStatus(0);
        order.setPaymentMethod("alipay");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        rechargeOrderMapper.insert(order);
        logger.info("充值订单创建成功，订单ID：{}", order.getId());

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 使用后端中转页，避免支付宝参数破坏 Vue 路由
        request.setReturnUrl("http://localhost:8083/recharge/redirect");
        request.setNotifyUrl(alipayConfig.getRechargeNotifyUrl());

        String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%.2f\",\"subject\":\"账户充值\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                orderNo, amount
        );
        request.setBizContent(bizContent);

        return alipayClient.pageExecute(request).getBody();
    }

    @Override
    @Transactional
    public boolean handleAlipayNotify(String orderNo, String tradeNo) {
        logger.info("处理充值支付宝回调，订单号：{}，交易号：{}", orderNo, tradeNo);
        
        RechargeOrder order = rechargeOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            logger.warn("充值订单不存在，订单号：{}", orderNo);
            return false;
        }
        
        if (order.getStatus() != 0) {
            logger.info("充值订单状态不是待支付，订单号：{}，当前状态：{}", orderNo, order.getStatus());
            return false;
        }

        rechargeOrderMapper.updateSuccess(orderNo, 1, tradeNo);
        boolean success = balanceService.addBalance(order.getUserId(), order.getAmount().doubleValue());
        
        if (success) {
            logger.info("充值成功，用户ID：{}，金额：{}", order.getUserId(), order.getAmount());
        } else {
            logger.error("充值失败，增加余额失败，用户ID：{}，金额：{}", order.getUserId(), order.getAmount());
        }

        return success;
    }

    @Override
    public RechargeOrder getOrderByNo(String orderNo) {
        return rechargeOrderMapper.findByOrderNo(orderNo);
    }
}