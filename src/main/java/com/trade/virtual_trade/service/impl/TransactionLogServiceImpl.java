package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.TransactionLog;
import com.trade.virtual_trade.mapper.TransactionLogMapper;
import com.trade.virtual_trade.service.TransactionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionLogServiceImpl implements TransactionLogService {

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    @Override
    public void recordLog(Integer userId, Double amount, String type, Long orderId, Double balanceBefore, Double balanceAfter, String remark) {
        TransactionLog log = new TransactionLog();
        log.setUserId(userId);
        log.setAmount(BigDecimal.valueOf(amount));
        log.setType(type);
        log.setOrderId(orderId);
        log.setBalanceBefore(BigDecimal.valueOf(balanceBefore));
        log.setBalanceAfter(BigDecimal.valueOf(balanceAfter));
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        transactionLogMapper.insert(log);
    }

    @Override
    public List<TransactionLog> getUserLogs(Integer userId) {
        return transactionLogMapper.findByUserId(userId);
    }
}