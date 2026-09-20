package com.trade.virtual_trade.mapper;
import com.trade.virtual_trade.entity.Product;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductMapper {
    void audit(@Param("id") Integer id, @Param("status") Integer status);
    List<Product> listByStatus(Integer status);
    List<Product> myList(Integer sellerId);
    List<Product> findAll();
    List<Product> findPage(Integer start, Integer size);
    Product findById(Integer id);
    void add(Product product);
    void update(Product product);
    void updateById(@Param("id") Integer id, @Param("status") Integer status);
    void delete(Integer id);
}