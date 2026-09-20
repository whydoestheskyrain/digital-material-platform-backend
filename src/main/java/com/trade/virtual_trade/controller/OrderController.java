package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.Order;
import com.trade.virtual_trade.entity.User;
import com.trade.virtual_trade.service.OrderService;
import com.trade.virtual_trade.service.UserService;
import com.trade.virtual_trade.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




@RestController
@RequestMapping("/order")
public class OrderController {

    // ====================== 【管理员专用】查看所有待发货订单（状态=2）======================
    @GetMapping("/admin/waitDelivery")
    public Result<List<Order>> adminWaitDelivery() {
        // 直接查询所有状态=2的订单
        List<Order> list = orderService.getAdminWaitDelivery();
        return Result.success(list);
    }
    /**
     * 创建订单接口（前端购买商品时调用）
     */

    @Resource
    private OrderService orderService;
    
    @Resource
    private UserService userService;

    // 余额支付接口
    @PutMapping("/pay/{id}")
    public Result payByBalance(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        if (!userId.equals(order.getBuyerId())) {
            return Result.error("无权操作此订单");
        }
        
        if (order.getStatus() != 1) {
            return Result.error("订单状态不是待支付");
        }
        
        boolean success = orderService.payByBalance(id, userId);
        if (success) {
            return Result.success("支付成功");
        } else {
            return Result.error("支付失败，余额不足");
        }
    }

    @PostMapping("/add")
    public Result addOrder(@RequestBody Order order, @RequestHeader("Authorization") String token) {
        try {
            // 从 token 中获取当前登录用户ID，作为买家ID
            Integer buyerId = JwtUtil.getUserId(token.substring(7));
            if (buyerId == null) {
                return Result.error("登录已失效，请重新登录");
            }
            order.setBuyerId(buyerId);

            // 生成唯一订单号（格式：ORDER+时间戳+4位随机后缀，避免订单号重复和沙箱风控）
            String orderNo = "ORDER" + System.currentTimeMillis() + String.format("%04d", new java.util.Random().nextInt(10000));
            order.setOrderNo(orderNo);

            // 设置初始状态：待支付（1）
            order.setStatus(1);
            order.setCreateTime(LocalDateTime.now());

            // 保存订单
            orderService.createOrder(order);
            return Result.success("订单创建成功", order.getId());
        } catch (Exception e) {
            return Result.error("订单创建失败：" + e.getMessage());
        }
    }

    // 包装订单状态文字（给前端显示用）
    private List<Order> wrapOrdersWithStatusDesc(List<Order> orders) {
        for (Order order : orders) {
            order.setStatusDesc(getStatusDesc(order.getStatus()));
            order.setStatusColor(getStatusColor(order.getStatus()));
        }
        return orders;
    }

    // 获取状态文字描述
    private String getStatusDesc(Integer status) {
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

    // 获取状态颜色（用于前端展示）
    private String getStatusColor(Integer status) {
        if (status == null) return "#999999";
        switch (status) {
            case 1: return "#E6A23C"; // 橙色 - 待支付
            case 2: return "#409EFF"; // 蓝色 - 待发货
            case 3: return "#67C23A"; // 绿色 - 待收货
            case 4: return "#909399"; // 灰色 - 已完成
            case 5: return "#F56C6C"; // 红色 - 已取消
            default: return "#999999";
        }
    }

    // ====================== 统一列表接口（支持筛选状态 + 买家ID） ======================
    @GetMapping("/listAll")
    public Result<List<Order>> listAll(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "buyerId", required = false) Integer buyerId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        // 1. 管理员访问：没有 buyerId，直接查所有订单
        if (token != null) {
            try {
                Integer userId = JwtUtil.getUserId(token.substring(7));
                User user = userService.findById(userId);
                if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
                    // 管理员：查所有订单，可选按状态筛选
                    if (status == null) {
                        return Result.success(orderService.listAll());
                    } else {
                        return Result.success(orderService.getOrdersByStatus(status));
                    }
                }
            } catch (Exception e) {
                // token 解析失败，按普通用户处理
            }
        }

        // 2. 普通买家访问：必须有 buyerId，查自己的订单
        if (buyerId == null) {
            return Result.success(Collections.emptyList());
        }

        List<Order> orderList;
        if (status == null) {
            orderList = orderService.getBuyerOrders(buyerId);
        } else {
            switch (status) {
                case 1: orderList = orderService.getBuyerUnpaidOrders(buyerId); break;
                case 2: orderList = orderService.getBuyerPaidOrders(buyerId); break;
                case 3: orderList = orderService.getBuyerDeliveredOrders(buyerId); break;
                case 4: orderList = orderService.getBuyerCompletedOrders(buyerId); break;
                default: orderList = orderService.getBuyerOrders(buyerId);
            }
        }
        return Result.success(orderList);
    }

    // ====================== 下面是你原来所有代码，我完全保留 ======================

    @PostMapping("/confirm/{id}")
    public Result confirm(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));

        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!userId.equals(order.getBuyerId())) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.confirmOrder(id, userId);
        if (success) {
            return Result.success("确认收货成功");
        } else {
            return Result.error("确认收货失败");
        }
    }

    @PostMapping("/cancel/{id}")
    public Result cancel(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));

        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!userId.equals(order.getBuyerId())) {
            return Result.error("无权操作此订单");
        }

        if (orderService.cancelOrder(id, userId)) {
            return Result.success("订单已取消");
        } else {
            return Result.error("取消订单失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        orderService.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/buyer/{id}/unpaid")
    public Result buyerUnpaidOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getBuyerUnpaidOrders(id)));
    }

    // 管理员发货接口（支持管理员和卖家发货）
    @PostMapping("/deliver/{id}")
    public Result deliver(@PathVariable Integer id, @RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        String accountInfo = request.get("accountInfo");

        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 获取当前用户信息
        User user = userService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 权限验证：管理员或卖家本人可以发货
        boolean isAdmin = "admin".equalsIgnoreCase(user.getRole());
        boolean isSeller = userId.equals(order.getSellerId());
        
        if (!isAdmin && !isSeller) {
            return Result.error("无权操作此订单，只有管理员或卖家可以发货");
        }

        if (order.getStatus() != 2) {
            return Result.error("订单状态不是待发货");
        }

        orderService.deliverGoods(id, accountInfo);
        return Result.success("发货成功");
    }

    @GetMapping("/buyer/{id}/paid")
    public Result buyerPaidOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getBuyerPaidOrders(id)));
    }

    @GetMapping("/buyer/{id}/delivered")
    public Result buyerDeliveredOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getBuyerDeliveredOrders(id)));
    }

    @GetMapping("/buyer/{id}/completed")
    public Result buyerCompletedOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getBuyerCompletedOrders(id)));
    }

    @GetMapping("/buyer/{id}/cancelled")
    public Result buyerCancelledOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getBuyerCancelledOrders(id)));
    }

    // 获取卖家所有订单
    @GetMapping("/seller/{id}/all")
    public Result sellerAllOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getSellerOrders(id)));
    }

    @GetMapping("/seller/{id}/paid")
    public Result sellerPaidOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getSellerPaidOrders(id)));
    }

    @GetMapping("/seller/{id}/delivered")
    public Result sellerDeliveredOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getSellerDeliveredOrders(id)));
    }

    @GetMapping("/seller/{id}/completed")
    public Result sellerCompletedOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getSellerCompletedOrders(id)));
    }

    @GetMapping("/seller/{id}/cancelled")
    public Result sellerCancelledOrders(@PathVariable Integer id) {
        return Result.success(wrapOrdersWithStatusDesc(orderService.getSellerCancelledOrders(id)));
    }

    @GetMapping("/buyer/{id}/stats")
    public Result buyerOrderStats(@PathVariable Integer id) {
        return Result.success(orderService.getBuyerOrderStats(id));
    }

    @GetMapping("/seller/{id}/stats")
    public Result sellerOrderStats(@PathVariable Integer id) {
        return Result.success(orderService.getSellerOrderStats(id));
    }

    @GetMapping("/{orderId}/account-info")
    public Result getAccountInfo(@PathVariable Integer orderId, @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        Order order = orderService.getById(orderId);

        if (order == null) {
            return Result.error("订单不存在");
        }

        // 权限验证：买家本人或管理员可以查看
        User user = userService.findById(userId);
        boolean isAdmin = user != null && "admin".equalsIgnoreCase(user.getRole());
        
        if (!userId.equals(order.getBuyerId()) && !isAdmin) {
            return Result.error("无权查看此订单的账号信息");
        }

        if (!Integer.valueOf(3).equals(order.getStatus())) {
            return Result.error("订单尚未发货，无法查看账号信息");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("accountInfo", order.getAccountInfo());

        return Result.success(result);
    }
}