package com.trade.virtual_trade.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.config.AlipayConfig;
import com.trade.virtual_trade.entity.RechargeOrder;
import com.trade.virtual_trade.service.RechargeService;
import com.trade.virtual_trade.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/recharge")
public class RechargeController {

    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);

    @Autowired
    private RechargeService rechargeService;
    
    @Autowired
    private AlipayConfig alipayConfig;

    // 查询订单状态（前端轮询用）
    @GetMapping("/status/{orderNo}")
    public Result<Map<String, Object>> getOrderStatus(
            @PathVariable String orderNo,
            @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        RechargeOrder order = rechargeService.getOrderByNo(orderNo);

        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("status", order.getStatus()); // 0=待支付 1=成功
        map.put("orderNo", orderNo);
        return Result.success(map);
    }

    @PostMapping("/create")
    public Result<String> create(@RequestBody Map<String, Double> params,
                                 @RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7)); // 解析token
        Double amount = params.get("amount");
        if (amount == null || amount <= 0) {
            logger.warn("充值金额错误，用户ID：{}，金额：{}", userId, amount);
            return Result.error("金额错误");
        }
        try {
            logger.info("用户发起充值，用户ID：{}，金额：{}", userId, amount);
            String form = rechargeService.createRecharge(userId, amount);
            return Result.success(form);
        } catch (Exception e) {
            logger.error("创建充值订单失败，用户ID：{}，金额：{}，错误：{}", userId, amount, e.getMessage(), e);
            return Result.error("创建充值订单失败");
        }
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 1. 获取支付宝回调参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = iter.next();
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            // 2. 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );

            if (!signVerified) {
                logger.warn("充值回调验签失败");
                return "fail";
            }

            // 3. 拿到核心参数
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");

            logger.info("支付宝回调成功：订单号={}, 交易号={}, 状态={}", outTradeNo, tradeNo, tradeStatus);

            // 4. 只处理成功状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                try {
                    rechargeService.handleAlipayNotify(outTradeNo, tradeNo);
                    logger.info("订单 {} 处理完成，余额已增加", outTradeNo);
                    return "success"; // 必须返回这个
                } catch (Exception e) {
                    // 捕获 service 里的错误，不让它 500
                    logger.error("处理订单失败：{}", e.getMessage(), e);
                    return "fail";
                }
            }

            // 其他状态直接返回成功
            return "success";

        } catch (Exception e) {
            // 全局捕获，绝对不会 500
            logger.error("支付宝回调整体异常：{}", e.getMessage(), e);
            return "fail";
        }
    }

    // 支付宝支付成功中转页 → 跳个人中心（解决 Vue 路由被参数破坏问题）
    @GetMapping("/redirect")
    public String redirectToUserCenter() {
        logger.info("充值成功，跳转到个人中心");
        // 使用服务端重定向，避免 JS 被浏览器拦截
        return "redirect:http://localhost:8082/#/user/center";
    }

    // 支付宝支付成功中转页 → 跳个人中心（解决 Vue 路由被参数破坏问题）
    // 根据订单号查状态，前端轮询用
    @GetMapping("/query/{orderNo}")
    public Result<Integer> queryOrder(@PathVariable String orderNo, @RequestHeader("Authorization")String token){
        Integer uid = JwtUtil.getUserId(token.substring(7));
        RechargeOrder order = rechargeService.getOrderByNo(orderNo);
        // 校验订单归属当前用户
        if(order==null || !order.getUserId().equals(uid)){
            return Result.error("无此订单");
        }
        // 0待支付，1已成功
        return Result.success(order.getStatus());
    }

    // 测试接口：手动触发充值成功（用于调试）
    @PostMapping("/test/success")
    public Result<String> testSuccess(@RequestParam String orderNo, @RequestParam String tradeNo) {
        try {
            boolean success = rechargeService.handleAlipayNotify(orderNo, tradeNo);
            if (success) {
                return Result.success("充值成功，余额已增加");
            } else {
                return Result.error("充值失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("充值失败: " + e.getMessage());
        }
    }
}