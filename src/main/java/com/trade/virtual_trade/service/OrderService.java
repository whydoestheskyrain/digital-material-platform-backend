package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> getOrdersByStatus(Integer status);
    // 余额支付
    boolean payByBalance(Integer orderId, Integer buyerId);
    
    // 确认收货（卖家收到钱）
    boolean confirmOrder(Integer orderId, Integer buyerId);

    List<Order> listAll();
    void deleteById(Integer id);
    
    // 1. 买家下单：创建订单，状态默认 UNPAID
    Order createOrder(Order order);

    // 2. 买家付款：更新状态为 PAID（资金平台托管）
    void payOrder(Integer orderId);

    // 3. 卖家发货：填写账号信息，更新状态为 DELIVERED
    void deliverOrder(Integer orderId, String accountInfo);

    void deliverGoods(Integer id, String accountInfo);

    // 4. 买家验收：确认收货，更新状态为 COMPLETED（平台放款）
    void confirmOrder(Integer orderId);

    // 查询接口
    List<Order> getBuyerOrders(Integer buyerId);
    List<Order> getSellerOrders(Integer sellerId);
    Order getOrderById(Integer orderId);
    Order getById(Integer orderId);

    void updateStatus(Integer id, Integer status);

    boolean cancelOrder(Integer orderId, Integer buyerId);

    List<Order> getAdminWaitDelivery();

    // 支付宝支付成功回调（新版，带交易号）
    void paySuccess(String orderNo, String tradeNo);
    
    // 根据订单号查询订单
    Order getByOrderNo(String orderNo);
    
    // ====================== 按状态分类查询 ======================
    
    /**
     * 获取买家待支付订单
     */
    List<Order> getBuyerUnpaidOrders(Integer buyerId);
    
    /**
     * 获取买家待发货订单（已支付）
     */
    List<Order> getBuyerPaidOrders(Integer buyerId);
    
    /**
     * 获取买家待收货订单（已发货）
     */
    List<Order> getBuyerDeliveredOrders(Integer buyerId);
    
    /**
     * 获取买家已完成订单
     */
    List<Order> getBuyerCompletedOrders(Integer buyerId);
    
    /**
     * 获取买家已取消订单
     */
    List<Order> getBuyerCancelledOrders(Integer buyerId);
    
    /**
     * 获取卖家待发货订单
     */
    List<Order> getSellerPaidOrders(Integer sellerId);
    
    /**
     * 获取卖家待收货订单（已发货）
     */
    List<Order> getSellerDeliveredOrders(Integer sellerId);
    
    /**
     * 获取卖家已完成订单
     */
    List<Order> getSellerCompletedOrders(Integer sellerId);
    
    /**
     * 获取卖家已取消订单
     */
    List<Order> getSellerCancelledOrders(Integer sellerId);
    
    /**
     * 获取买家订单统计（各状态数量）
     */
    Map<String, Long> getBuyerOrderStats(Integer buyerId);
    
    /**
     * 获取卖家订单统计（各状态数量）
     */
    Map<String, Long> getSellerOrderStats(Integer sellerId);
}