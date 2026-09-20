package com.trade.virtual_trade.task;

import com.trade.virtual_trade.entity.Order;
import com.trade.virtual_trade.entity.Product;
import com.trade.virtual_trade.entity.User;
import com.trade.virtual_trade.mapper.OrderMapper;
import com.trade.virtual_trade.mapper.ProductMapper;
import com.trade.virtual_trade.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务
 */
@Component
public class OrderTimeoutTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderTimeoutTask.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 订单超时时间（分钟）
     * 15分钟未支付自动取消
     */
    private static final int ORDER_TIMEOUT_MINUTES = 15;

    /**
     * 定时任务：每5分钟检查一次超时订单
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        logger.info("开始检查超时订单...");
        
        try {
            // 查询所有待支付且超时的订单
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
            List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(timeoutTime);
            
            if (timeoutOrders.isEmpty()) {
                logger.info("没有超时订单需要处理");
                return;
            }
            
            logger.info("发现 {} 个超时订单", timeoutOrders.size());
            
            // 逐个取消超时订单
            for (Order order : timeoutOrders) {
                try {
                    // 如果是余额支付且已支付状态，需要解冻买家余额
                    if (order.getStatus() == 2 && "balance".equals(order.getPaymentMethod())) {
                        User buyer = userMapper.selectById(order.getBuyerId());
                        if (buyer != null) {
                            BigDecimal frozen = buyer.getFrozenBalance() == null ? BigDecimal.ZERO : buyer.getFrozenBalance();
                            if (frozen.compareTo(order.getPrice()) >= 0) {
                                userMapper.unfreezeToBalance(order.getBuyerId(), order.getPrice().doubleValue());
                                logger.info("解冻买家余额，买家ID：{}，金额：{}", order.getBuyerId(), order.getPrice());
                            }
                        }
                    }

                    // 更新订单状态为已取消
                    Order update = new Order();
                    update.setId(order.getId());
                    update.setStatus(5); // CANCELLED
                    orderMapper.updateStatus(update);

                    // 恢复商品状态
                    Product product = productMapper.findById(order.getProductId());
                    if (product != null && product.getStatus() == 3) {
                        productMapper.updateById(order.getProductId(), 1);
                    }
                    
                    logger.info("订单已自动取消，订单号：{}，订单ID：{}", order.getOrderNo(), order.getId());
                    
                } catch (Exception e) {
                    logger.error("取消订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage());
                }
            }
            
            logger.info("超时订单处理完成");
            
        } catch (Exception e) {
            logger.error("检查超时订单时发生异常：{}", e.getMessage(), e);
        }
    }
}