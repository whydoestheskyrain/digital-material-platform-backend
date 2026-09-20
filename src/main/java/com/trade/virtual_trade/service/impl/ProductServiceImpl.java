package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.Product;
import com.trade.virtual_trade.mapper.ProductMapper;
import com.trade.virtual_trade.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_LIST_KEY = "product:list";

    // 审核商品
    @Override
    public void audit(Integer id, Integer status) {
        productMapper.audit(id, status);
        // 审核后清除缓存
        redisTemplate.delete(PRODUCT_LIST_KEY);
    }

    // 按状态查询
    @Override
    public List<Product> listByStatus(Integer status) {
        return productMapper.listByStatus(status);
    }

    // 我的商品
    @Override
    public List<Product> myList(Integer sellerId) {
        return productMapper.myList(sellerId);
    }

    // 全部商品（带Redis缓存）
    @Override
    public List<Product> findAll() {
        List<Product> list = (List<Product>) redisTemplate.opsForValue().get(PRODUCT_LIST_KEY);
        if (list != null) {
            return list;
        }
        list = productMapper.findAll();
        redisTemplate.opsForValue().set(PRODUCT_LIST_KEY, list, 10, TimeUnit.MINUTES);
        return list;
    }

    // 分页
    @Override
    public List<Product> findPage(Integer page, Integer size) {
        int start = (page - 1) * size;
        return productMapper.findPage(start, size);
    }

    // 根据ID查询
    @Override
    public Product findById(Integer id) {
        return productMapper.findById(id);
    }

    // 新增
    @Override
    public void add(Product product) {
        productMapper.add(product);
        redisTemplate.delete(PRODUCT_LIST_KEY);
    }

    // 修改
    @Override
    public void update(Product product) {
        productMapper.update(product);
        redisTemplate.delete(PRODUCT_LIST_KEY);
    }

    // 删除
    @Override
    public void delete(Integer id) {
        productMapper.delete(id);
        redisTemplate.delete(PRODUCT_LIST_KEY);
    }
}