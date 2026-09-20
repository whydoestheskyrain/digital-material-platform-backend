package com.trade.virtual_trade.mapper;
import com.trade.virtual_trade.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper  {
    List<Order> findByStatus(Integer status);

    List<Order> listAll();
    void deleteById(Integer id);
    
    // 创建订单
    int insert(Order order);

    // 根据买家ID查询订单
    List<Order> selectByBuyerId(Integer buyerId);

    // 根据卖家ID查询订单
    List<Order> selectBySellerId(Integer sellerId);

    // 根据ID查询订单
    Order selectById(Integer id);

    // 根据订单号查询订单
    Order selectByOrderNo(String orderNo);

    // 更新订单状态
    int updateStatus(Order order);

    // 卖家填写账号信息，同时更新状态为已发货
    int updateAccountInfo(Order order);
    
    Order getById(Integer id);
    
    // 更新订单
    int update(Order order);
    
    // ====================== 按状态分类查询 ======================
    
    /**
     * 根据买家ID和状态查询订单
     */
    List<Order> selectByBuyerIdAndStatus(@Param("buyerId") Integer buyerId, @Param("status") Integer status);
    
    /**
     * 根据卖家ID和状态查询订单
     */
    List<Order> selectBySellerIdAndStatus(@Param("sellerId") Integer sellerId, @Param("status") Integer status);
    
    /**
     * 统计买家各状态订单数量
     */
    Long countByBuyerIdAndStatus(@Param("buyerId") Integer buyerId, @Param("status") Integer status);
    
    /**
     * 统计卖家各状态订单数量
     */
    Long countBySellerIdAndStatus(@Param("sellerId") Integer sellerId, @Param("status") Integer status);
    
    /**
     * 查询超时未支付的订单
     * @param timeoutTime 超时时间点
     */
    List<Order> selectTimeoutOrders(@Param("timeoutTime") java.time.LocalDateTime timeoutTime);
}