package com.trade.virtual_trade.service.impl;
import com.trade.virtual_trade.entity.Transaction;
import com.trade.virtual_trade.mapper.TransactionMapper;
import com.trade.virtual_trade.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Override
    public List<Transaction> findPage(int page, int size) {
        int offset = (page - 1) * size;
        return transactionMapper.findPage(offset, size);
    }
    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public List<Transaction> findAll() { return transactionMapper.selectAll(); }
    @Override
    public Transaction findById(Integer id) { return transactionMapper.selectById(id); }
    @Override
    public int add(Transaction transaction) { return transactionMapper.insert(transaction); }
    @Override
    public int update(Transaction transaction) { return transactionMapper.update(transaction); }
    @Override
    public int delete(Integer id) { return transactionMapper.deleteById(id); }
}