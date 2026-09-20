package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.Product;
import java.util.List;

public interface ProductService {
    void audit(Integer id, Integer status);        // 审核
    List<Product> listByStatus(Integer status);   // 按状态查询
    List<Product> myList(Integer sellerId);       // 我的商品
    List<Product> findAll();                      // 全部列表
    List<Product> findPage(Integer page, Integer size); // 分页
    Product findById(Integer id);                 // 根据ID查
    void add(Product product);                    // 新增
    void update(Product product);                 // 修改
    void delete(Integer id);                       // 删除
}