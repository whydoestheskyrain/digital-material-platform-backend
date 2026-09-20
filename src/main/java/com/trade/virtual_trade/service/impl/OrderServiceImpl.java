package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.Order;
import com.trade.virtual_trade.entity.Product;
import com.trade.virtual_trade.entity.TransactionLog;
import com.trade.virtual_trade.entity.User;
import com.trade.virtual_trade.mapper.OrderMapper;
import com.trade.virtual_trade.mapper.ProductMapper;
import com.trade.virtual_trade.mapper.TransactionLogMapper;
import com.trade.virtual_trade.mapper.UserMapper;
import com.trade.virtual_trade.service.NotificationService;
import com.trade.virtual_trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final Integer STATUS_UNPAID = 1;      // 待付款
    private static final Integer STATUS_PAID = 2;        // 已付款（待发货）
    private static final Integer STATUS_DELIVERED = 3;   // 已发货（待收货）
    private static final Integer STATUS_COMPLETED = 4;   // 已完成
    private static final Integer STATUS_CANCELLED = 5;   // 已取消

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    @Autowired
    private NotificationService notificationService;

    private String getStatusDescription(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待支付";
            case 2: return "待发货";
            case 3: return "待收货";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    @Override
    @Transactional
    public boolean payByBalance(Integer orderId, Integer buyerId) {
        logger.info("余额支付，订单ID：{}，买家ID：{}", orderId, buyerId);

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            logger.warn("订单不存在，订单ID：{}", orderId);
            return false;
        }

        if (!STATUS_UNPAID.equals(order.getStatus())) {
            logger.warn("订单状态不是待支付，当前状态：{}", order.getStatus());
            return false;
        }

        User buyer = userMapper.selectById(buyerId);
        if (buyer == null) return false;

        BigDecimal balance = buyer.getBalance() == null ? BigDecimal.ZERO : buyer.getBalance();
        if (balance.compareTo(order.getPrice()) < 0) return false;

        int rows = userMapper.freezeBalance(buyerId, order.getPrice().doubleValue());
        if (rows == 0) return false;

        Order update = new Order();
        update.setId(orderId);
        update.setStatus(STATUS_PAID);
        update.setPayTime(LocalDateTime.now());
        update.setPaymentMethod("balance");
        orderMapper.updateStatus(update);

        TransactionLog log = new TransactionLog();
        log.setUserId(buyerId);
        log.setAmount(order.getPrice().negate());
        log.setType("pay");
        log.setOrderId(Long.valueOf(orderId));
        log.setBalanceBefore(balance);
        log.setBalanceAfter(balance.subtract(order.getPrice()));
        log.setRemark("购买商品");
        transactionLogMapper.insert(log);

        notificationService.sendOrderNotification(order.getBuyerId(), "支付成功", "订单" + order.getOrderNo() + "已支付成功，等待卖家发货", orderId);
        notificationService.sendOrderNotification(order.getSellerId(), "订单已支付", "订单" + order.getOrderNo() + "买家已支付，请尽快发货", orderId);

        logger.info("余额支付成功，订单已改为待发货");
        return true;
    }

    @Override
    @Transactional
    public boolean confirmOrder(Integer orderId, Integer buyerId) {
        Order order = orderMapper.getById(orderId);
        if (order == null || !STATUS_DELIVERED.equals(order.getStatus())) {
            return false;
        }

        if ("balance".equals(order.getPaymentMethod())) {
            User buyer = userMapper.selectById(buyerId);
            if (buyer == null) return false;

            BigDecimal frozen = buyer.getFrozenBalance() == null ? BigDecimal.ZERO : buyer.getFrozenBalance();
            if (frozen.compareTo(order.getPrice()) < 0) return false;

            // 从买家冻结余额中扣除（转账给卖家）
            userMapper.transferFromFrozenToSeller(buyerId, order.getPrice().doubleValue());
        }

        // 给卖家加钱
        userMapper.addBalance(order.getSellerId(), order.getPrice().doubleValue());

        // 更新订单为已完成
        Order update = new Order();
        update.setId(orderId);
        update.setStatus(STATUS_COMPLETED);
        orderMapper.updateStatus(update);

        notificationService.sendOrderNotification(order.getBuyerId(), "交易完成", "订单" + order.getOrderNo() + "交易已完成，感谢您的购买", orderId);
        notificationService.sendOrderNotification(order.getSellerId(), "交易完成", "订单" + order.getOrderNo() + "交易已完成，余额已到账", orderId);

        return true;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Integer orderId, Integer buyerId) {
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            logger.warn("订单不存在，订单ID：{}", orderId);
            return false;
        }

        if (STATUS_COMPLETED.equals(order.getStatus()) || STATUS_CANCELLED.equals(order.getStatus())) {
            logger.warn("订单已是终态，无法取消，订单ID：{}", orderId);
            return false;
        }

        // 如果是余额支付且已支付状态，需要解冻买家余额
        if (STATUS_PAID.equals(order.getStatus()) && "balance".equals(order.getPaymentMethod())) {
            User buyer = userMapper.selectById(buyerId);
            if (buyer != null) {
                BigDecimal frozen = buyer.getFrozenBalance() == null ? BigDecimal.ZERO : buyer.getFrozenBalance();
                if (frozen.compareTo(order.getPrice()) >= 0) {
                    userMapper.unfreezeToBalance(buyerId, order.getPrice().doubleValue());
                }
            }
        }

        Order update = new Order();
        update.setId(orderId);
        update.setStatus(STATUS_CANCELLED);
        orderMapper.updateStatus(update);

        // 如果商品还没恢复，恢复商品状态
        Product product = productMapper.findById(order.getProductId());
        if (product != null && product.getStatus() == 3) {
            productMapper.updateById(order.getProductId(), 1);
        }

        notificationService.sendOrderNotification(order.getBuyerId(), "订单已取消", "订单" + order.getOrderNo() + "已取消，已支付的金额将原路退回", orderId);
        notificationService.sendOrderNotification(order.getSellerId(), "订单已取消", "订单" + order.getOrderNo() + "已被买家取消", orderId);

        logger.info("订单已取消，订单ID：{}", orderId);
        return true;
    }

    @Override
    public void updateStatus(Integer orderId, Integer status) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        orderMapper.updateStatus(order);
    }

    @Override
    public List<Order> listAll() {
        return orderMapper.listAll();
    }

    @Override
    public void deleteById(Integer id) {
        orderMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Order order) {
        Product product = productMapper.findById(order.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new RuntimeException("商品无法购买");
        }

        order.setStatus(STATUS_UNPAID);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        productMapper.updateById(order.getProductId(), 3);

        notificationService.sendOrderNotification(order.getBuyerId(), "下单成功", "订单" + order.getOrderNo() + "已创建，请尽快支付", order.getId());
        notificationService.sendOrderNotification(order.getSellerId(), "新订单提醒", "收到新订单" + order.getOrderNo() + "，买家已下单", order.getId());

        return order;
    }

    @Override
    public void payOrder(Integer orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
    }

    @Override
    public void deliverOrder(Integer orderId, String accountInfo) {
        Order order = orderMapper.getById(orderId);
        if (order == null || !STATUS_PAID.equals(order.getStatus())) return;

        if (accountInfo == null || accountInfo.trim().isEmpty()) {
            Product product = productMapper.findById(order.getProductId());
            if (product != null) {
                accountInfo = "账号：" + product.getGameAccount() + "\n密码：" + product.getGamePassword();
            }
        }

        Order update = new Order();
        update.setId(orderId);
        update.setAccountInfo(accountInfo);
        update.setStatus(STATUS_DELIVERED);

        orderMapper.updateAccountInfo(update);
        orderMapper.updateStatus(update);

        notificationService.sendOrderNotification(order.getBuyerId(), "卖家已发货", "订单" + order.getOrderNo() + "卖家已发货，请确认收货", orderId);
        notificationService.sendOrderNotification(order.getSellerId(), "已发货", "订单" + order.getOrderNo() + "已发货，等待买家确认", orderId);

        logger.info("订单已发货，状态改为3");
    }

    @Override
    public void deliverGoods(Integer id, String accountInfo) {
        deliverOrder(id, accountInfo);
    }

    @Override
    public void confirmOrder(Integer orderId) {
        Order order = orderMapper.getById(orderId);
        if (order != null && STATUS_DELIVERED.equals(order.getStatus())) {
            Order update = new Order();
            update.setId(orderId);
            update.setStatus(STATUS_COMPLETED);
            orderMapper.updateStatus(update);
        }
    }

    // ========================= 管理员查看待发货 =========================
    @Override
    public List<Order> getAdminWaitDelivery() {
        return orderMapper.findByStatus(STATUS_PAID);
    }

    // ========================= 支付宝支付成功回调 =========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo, String tradeNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() >= STATUS_PAID) return;

        Order update = new Order();
        update.setId(order.getId());
        update.setStatus(STATUS_PAID);
        update.setPayTime(LocalDateTime.now());
        update.setPaymentMethod("alipay");
        update.setTradeNo(tradeNo);
        orderMapper.update(update);

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null && buyer.getBalance() != null && buyer.getBalance().compareTo(order.getPrice()) >= 0) {
            userMapper.freezeBalance(order.getBuyerId(), order.getPrice().doubleValue());

            TransactionLog log = new TransactionLog();
            log.setUserId(order.getBuyerId());
            log.setAmount(order.getPrice());
            log.setType("freeze");
            log.setOrderId(Long.valueOf(order.getId()));
            log.setBalanceBefore(buyer.getBalance());
            log.setBalanceAfter(buyer.getBalance().subtract(order.getPrice()));
            log.setRemark("订单支付，资金冻结");
            transactionLogMapper.insert(log);
        }
        logger.info("支付宝支付成功，订单改为待发货");
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<Order> getBuyerOrders(Integer buyerId) {
        return orderMapper.selectByBuyerId(buyerId);
    }

    @Override
    public List<Order> getSellerOrders(Integer sellerId) {
        return orderMapper.selectBySellerId(sellerId);
    }

    @Override
    public Order getOrderById(Integer orderId) {
        return orderMapper.getById(orderId);
    }

    @Override
    public Order getById(Integer orderId) {
        return orderMapper.getById(orderId);
    }

    // ====================== 按状态查询 ======================

    @Override
    public List<Order> getBuyerUnpaidOrders(Integer buyerId) {
        return orderMapper.selectByBuyerIdAndStatus(buyerId, STATUS_UNPAID);
    }

    @Override
    public List<Order> getBuyerPaidOrders(Integer buyerId) {
        return orderMapper.selectByBuyerIdAndStatus(buyerId, STATUS_PAID);
    }

    @Override
    public List<Order> getBuyerDeliveredOrders(Integer buyerId) {
        return orderMapper.selectByBuyerIdAndStatus(buyerId, STATUS_DELIVERED);
    }

    @Override
    public List<Order> getBuyerCompletedOrders(Integer buyerId) {
        return orderMapper.selectByBuyerIdAndStatus(buyerId, STATUS_COMPLETED);
    }

    @Override
    public List<Order> getBuyerCancelledOrders(Integer buyerId) {
        return orderMapper.selectByBuyerIdAndStatus(buyerId, STATUS_CANCELLED);
    }

    @Override
    public List<Order> getSellerPaidOrders(Integer sellerId) {
        return orderMapper.selectBySellerIdAndStatus(sellerId, STATUS_PAID);
    }

    @Override
    public List<Order> getSellerDeliveredOrders(Integer sellerId) {
        return orderMapper.selectBySellerIdAndStatus(sellerId, STATUS_DELIVERED);
    }

    @Override
    public List<Order> getSellerCompletedOrders(Integer sellerId) {
        return orderMapper.selectBySellerIdAndStatus(sellerId, STATUS_COMPLETED);
    }

    @Override
    public List<Order> getSellerCancelledOrders(Integer sellerId) {
        return orderMapper.selectBySellerIdAndStatus(sellerId, STATUS_CANCELLED);
    }

    @Override
    public Map<String, Long> getBuyerOrderStats(Integer buyerId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("unpaid", orderMapper.countByBuyerIdAndStatus(buyerId, STATUS_UNPAID));
        stats.put("paid", orderMapper.countByBuyerIdAndStatus(buyerId, STATUS_PAID));
        stats.put("delivered", orderMapper.countByBuyerIdAndStatus(buyerId, STATUS_DELIVERED));
        stats.put("completed", orderMapper.countByBuyerIdAndStatus(buyerId, STATUS_COMPLETED));
        stats.put("cancelled", orderMapper.countByBuyerIdAndStatus(buyerId, STATUS_CANCELLED));
        return stats;
    }
    @Override
    public List<Order> getOrdersByStatus(Integer status) {
        return orderMapper.findByStatus(status);
    }
    @Override
    public Map<String, Long> getSellerOrderStats(Integer sellerId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("unpaid", orderMapper.countBySellerIdAndStatus(sellerId, STATUS_UNPAID));
        stats.put("paid", orderMapper.countBySellerIdAndStatus(sellerId, STATUS_PAID));
        stats.put("delivered", orderMapper.countBySellerIdAndStatus(sellerId, STATUS_DELIVERED));
        stats.put("completed", orderMapper.countBySellerIdAndStatus(sellerId, STATUS_COMPLETED));
        stats.put("cancelled", orderMapper.countBySellerIdAndStatus(sellerId, STATUS_CANCELLED));
        return stats;
    }
}