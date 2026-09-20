package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.Transaction;
import com.trade.virtual_trade.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction") // 👈 去掉 api，和 user/order 完全一样
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/page")
    public Result<List<Transaction>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(transactionService.findPage(page, size));
    }

    @GetMapping("/list")
    public Result<List<Transaction>> list() {
        return Result.success(transactionService.findAll());
    }

    @GetMapping("/get/{id}")
    public Result<Transaction> get(@PathVariable Integer id) {
        return Result.success(transactionService.findById(id));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Transaction transaction) {
        transactionService.add(transaction);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody Transaction transaction) {
        transactionService.update(transaction);
        return Result.success();
    }

    @GetMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        transactionService.delete(id);
        return Result.success();
    }
}