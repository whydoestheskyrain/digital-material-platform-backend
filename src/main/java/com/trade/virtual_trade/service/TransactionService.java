package com.trade.virtual_trade.service;
import com.trade.virtual_trade.entity.Transaction;
import java.util.List;

public interface TransactionService {
    List<Transaction> findAll();
    Transaction findById(Integer id);
    int add(Transaction transaction);
    int update(Transaction transaction);
    int delete(Integer id);
    List<Transaction> findPage(int page, int size);
}