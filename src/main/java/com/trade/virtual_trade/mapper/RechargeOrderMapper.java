package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.RechargeOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface RechargeOrderMapper {
    @Insert("INSERT INTO recharge_order(order_no, user_id, amount, status, payment_method, create_time, update_time) " +
            "VALUES(#{orderNo}, #{userId}, #{amount}, 0, #{paymentMethod}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RechargeOrder order);

    @Select("SELECT * FROM recharge_order WHERE order_no = #{orderNo}")
    RechargeOrder findByOrderNo(String orderNo);

    @Update("UPDATE recharge_order SET status = #{status}, trade_no = #{tradeNo}, pay_time = NOW(), update_time = NOW() WHERE order_no = #{orderNo}")
    int updateSuccess(@Param("orderNo") String orderNo, @Param("status") Integer status, @Param("tradeNo") String tradeNo);
}