package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.TransactionLog;
import java.util.List;

public interface TransactionLogService {
    void recordLog(Integer userId, Double amount, String type, Long orderId, Double balanceBefore, Double balanceAfter, String remark);
    List<TransactionLog> getUserLogs(Integer userId);
}