package com.trade.virtual_trade.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.trade.virtual_trade.config.AlipayConfig;
import com.trade.virtual_trade.entity.Order;
import com.trade.virtual_trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    private static final Logger logger = LoggerFactory.getLogger(AlipayController.class);

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private OrderService orderService;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    private static final String FORMAT = "json";
    private static final String CHARSET = "utf-8";
    private static final String SIGN_TYPE = "RSA2";

    // 订单支付接口：跳转到支付宝
    @GetMapping("/pay")
    public void pay(Long orderId, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = null;
        try {
            out = response.getWriter();
            
            // 1. 查询真实订单数据
            Order order = orderService.getById(orderId.intValue());
            if (order == null) {
                out.write("<html><body><h1>订单不存在</h1></body></html>");
                return;
            }

            // 2. 检查订单状态（幂等性检查）
            if (order.getStatus() != 1) { // 不是待支付状态
                out.write("<html><body><h1>订单状态异常，请刷新页面后重试</h1></body></html>");
                return;
            }

            // 3. 封装订单信息
            String outTradeNo = order.getOrderNo();
            
            // 检查订单金额是否为空
            if (order.getPrice() == null || order.getPrice().doubleValue() <= 0) {
                out.write("<html><body><h1>订单金额异常</h1></body></html>");
                return;
            }
            
            String totalAmount = order.getPrice().toString();
            String subject = "游戏账号交易订单-" + order.getOrderNo();

            // 4. 使用预配置的 AlipayClient
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(returnUrl);
            request.setNotifyUrl(notifyUrl);

            // 5. 封装订单信息
            String bizContent = "{"
                    + "\"out_trade_no\":\"" + outTradeNo + "\","
                    + "\"total_amount\":\"" + totalAmount + "\","
                    + "\"subject\":\"" + subject + "\","
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                    + "}";

            request.setBizContent(bizContent);

            // 6. 输出支付表单
            String form = alipayClient.pageExecute(request).getBody();
            out.write(form);
            
        } catch (AlipayApiException e) {
            logger.error("支付宝API配置错误: {}", e.getMessage());
            String errorMsg = "<html><body><h1>支付宝配置错误</h1><p>" + e.getMessage() + "</p><p>请检查支付宝密钥配置</p></body></html>";
            if (out != null) {
                out.write(errorMsg);
            } else {
                try {
                    response.getWriter().write(errorMsg);
                } catch (Exception ex) {
                    logger.error("写入错误信息失败", ex);
                }
            }
        } catch (Exception e) {
            logger.error("订单支付处理异常: {}", e.getMessage(), e);
            String errorMsg = "<html><body><h1>系统错误</h1><p>" + e.getMessage() + "</p></body></html>";
            if (out != null) {
                out.write(errorMsg);
            } else {
                try {
                    response.getWriter().write(errorMsg);
                } catch (Exception ex) {
                    logger.error("写入错误信息失败", ex);
                }
            }
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    // 订单支付异步回调（必须验签！）
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 1. 获取支付宝POST过来的所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = iter.next();
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            // 2. 支付宝验签（最关键！防止伪造通知）
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, 
                    alipayConfig.getAlipayPublicKey(), 
                    CHARSET, 
                    SIGN_TYPE
            );

            if (!signVerified) {
                logger.warn("支付宝回调验签失败，可能是伪造请求");
                return "fail";
            }

            // 3. 获取关键参数
            String outTradeNo = request.getParameter("out_trade_no");
            String tradeNo = request.getParameter("trade_no");
            String tradeStatus = request.getParameter("trade_status");
            String totalAmount = request.getParameter("total_amount");

            logger.info("支付宝回调验证通过，订单号：{}，交易号：{}，状态：{}", outTradeNo, tradeNo, tradeStatus);

            // 4. 判断交易状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 5. 幂等处理：检查订单是否已支付
                Order order = orderService.getByOrderNo(outTradeNo);
                if (order == null) {
                    logger.warn("订单不存在：{}", outTradeNo);
                    return "fail";
                }

                // 幂等性检查：如果订单已经是已支付或更高状态，直接返回success
                if (order.getStatus() >= 2) {
                    logger.info("订单已处理过，订单号：{}，当前状态：{}", outTradeNo, order.getStatus());
                    return "success";
                }

                // 6. 调用订单支付成功处理（含事务）
                orderService.paySuccess(outTradeNo, tradeNo);
                
                logger.info("订单支付成功处理完成，订单号：{}", outTradeNo);
                return "success";
            }
            
            logger.info("交易状态不匹配：{}", tradeStatus);
            return "fail";
            
        } catch (AlipayApiException e) {
            logger.error("支付宝验签API异常: {}", e.getMessage(), e);
            return "fail";
        } catch (Exception e) {
            logger.error("支付宝回调处理异常: {}", e.getMessage(), e);
            return "fail";
        }
    }
}
