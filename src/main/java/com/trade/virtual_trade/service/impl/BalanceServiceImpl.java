package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.User;
import com.trade.virtual_trade.mapper.UserMapper;
import com.trade.virtual_trade.service.BalanceService;
import com.trade.virtual_trade.service.TransactionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BalanceServiceImpl implements BalanceService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TransactionLogService transactionLogService;

    @Override
    @Transactional
    public boolean deductBalance(Integer userId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        BigDecimal balance = user.getBalance();
        if (balance == null || balance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            return false;
        }
        BigDecimal beforeBalance = balance;
        BigDecimal afterBalance = beforeBalance.subtract(BigDecimal.valueOf(amount));
        int rows = userMapper.deductBalance(userId, amount);
        if (rows > 0) {
            transactionLogService.recordLog(userId, -amount, "pay", null, beforeBalance.doubleValue(), afterBalance.doubleValue(), "订单支付");
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean addBalance(Integer userId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        BigDecimal beforeBalance = user.getBalance();
        if (beforeBalance == null) {
            beforeBalance = BigDecimal.ZERO;
        }
        BigDecimal afterBalance = beforeBalance.add(BigDecimal.valueOf(amount));
        int rows = userMapper.addBalance(userId, amount);
        if (rows > 0) {
            transactionLogService.recordLog(userId, amount, "income", null, beforeBalance.doubleValue(), afterBalance.doubleValue(), "充值/收入");
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean freezeBalance(Integer userId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        BigDecimal balance = user.getBalance();
        if (balance == null || balance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            return false;
        }
        BigDecimal beforeBalance = balance;
        BigDecimal afterBalance = beforeBalance.subtract(BigDecimal.valueOf(amount));
        int rows = userMapper.freezeBalance(userId, amount);
        if (rows > 0) {
            transactionLogService.recordLog(userId, -amount, "freeze", null, beforeBalance.doubleValue(), afterBalance.doubleValue(), "余额冻结");
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean unfreezeToSeller(Integer buyerId, Integer sellerId, Double amount) {
        User buyer = userMapper.selectById(buyerId);
        User seller = userMapper.selectById(sellerId);
        if (buyer == null || seller == null) {
            return false;
        }
        BigDecimal frozenBalance = buyer.getFrozenBalance();
        if (frozenBalance == null || frozenBalance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            return false;
        }
        BigDecimal beforeSeller = seller.getBalance();
        if (beforeSeller == null) {
            beforeSeller = BigDecimal.ZERO;
        }
        BigDecimal afterSeller = beforeSeller.add(BigDecimal.valueOf(amount));
        int freezeRows = userMapper.unfreezeToBalance(buyerId, amount);
        if (freezeRows == 0) {
            return false;
        }
        int rows = userMapper.addBalance(sellerId, amount);
        if (rows > 0) {
            transactionLogService.recordLog(sellerId, amount, "income", null, beforeSeller.doubleValue(), afterSeller.doubleValue(), "订单收入");
            return true;
        }
        return false;
    }
}