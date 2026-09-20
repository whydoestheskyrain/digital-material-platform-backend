package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.TransactionLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionLogMapper {
    @Insert("INSERT INTO transaction_log(user_id, amount, type, order_id, balance_before, balance_after, remark, create_time) " +
            "VALUES(#{userId}, #{amount}, #{type}, #{orderId}, #{balanceBefore}, #{balanceAfter}, #{remark}, NOW())")
    int insert(TransactionLog log);

    @Select("SELECT * FROM transaction_log WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TransactionLog> findByUserId(Integer userId);
}