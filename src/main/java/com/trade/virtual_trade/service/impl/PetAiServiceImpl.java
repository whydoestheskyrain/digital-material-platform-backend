package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.service.PetAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PetAiServiceImpl implements PetAiService {

    @Value("${dashscope.api-key:}")
    private String apiKey;

    @Value("${dashscope.app-id:}")
    private String appId;

    private final String SYSTEM_PROMPT = "你是本游戏账号交易网站的AI客服像素少女桌宠，仅解答平台相关问题：" +
            "1. 用户注册、登录、修改手机号/密码、头像；" +
            "2. 商品发布、下单、支付、充值、支付宝沙箱；" +
            "3. 订单发货、确认收货、资金冻结结算、余额提现；" +
            "4. AI桌宠形象切换、皮肤更换问题。" +
            "用户询问无关内容（动漫、游戏攻略、生活闲聊）统一回复：我只解答本平台交易相关问题哦~" +
            "回答简短温柔，符合少女客服语气，不要长篇大论。";

    private final List<String> MOCK_REPLIES = new ArrayList<>();

    public PetAiServiceImpl() {
        MOCK_REPLIES.add("您好呀~ 请问有什么可以帮助您的呢？");
        MOCK_REPLIES.add("关于注册登录的问题，您可以点击页面右上角的登录按钮哦~");
        MOCK_REPLIES.add("发布商品很简单的，点击首页的发布按钮就可以啦！");
        MOCK_REPLIES.add("订单相关问题，请在个人中心查看我的订单哦~");
        MOCK_REPLIES.add("充值功能支持支付宝沙箱测试，请放心使用~");
        MOCK_REPLIES.add("桌宠皮肤可以在个人中心切换哦，有很多可爱形象~");
        MOCK_REPLIES.add("我只解答本平台交易相关问题哦~");
        MOCK_REPLIES.add("支付问题请联系管理员处理，感谢您的理解~");
        MOCK_REPLIES.add("收货后资金会自动结算到您的账户余额哦~");
        MOCK_REPLIES.add("商品审核通常在24小时内完成，请耐心等待~");
    }

    @Override
    public String getAiReply(String userMsg) {
        if (isValidApiKeyAndAppId()) {
            return getRealAiReply(userMsg);
        }
        return getMockReply(userMsg);
    }

    private boolean isValidApiKeyAndAppId() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("sk-your-api-key")
                && appId != null && !appId.isEmpty() && !appId.equals("your-app-id-here");
    }

    private String getRealAiReply(String userMsg) {
        try {
            com.alibaba.dashscope.app.ApplicationParam param =
                    com.alibaba.dashscope.app.ApplicationParam.builder()
                            .apiKey(apiKey)
                            .appId(appId)
                            .prompt(SYSTEM_PROMPT + "\n用户提问：" + userMsg)
                            .build();

            com.alibaba.dashscope.app.Application app = new com.alibaba.dashscope.app.Application();
            com.alibaba.dashscope.app.ApplicationResult result = app.call(param);

            if (result != null && result.getOutput() != null) {
                return result.getOutput().getText();
            }
        } catch (Exception e) {
            System.err.println("DashScope API call failed: " + e.getMessage());
        }
        return getMockReply(userMsg);
    }

    private String getMockReply(String userMsg) {
        String lowerMsg = userMsg.toLowerCase();

        if (lowerMsg.contains("注册") || lowerMsg.contains("登录") || lowerMsg.contains("密码") || lowerMsg.contains("手机") || lowerMsg.contains("头像")) {
            return "关于注册登录的问题，您可以点击页面右上角的登录按钮哦~";
        }
        if (lowerMsg.contains("发布") || lowerMsg.contains("商品") || lowerMsg.contains("上架")) {
            return "发布商品很简单的，点击首页的发布按钮就可以啦！";
        }
        if (lowerMsg.contains("订单") || lowerMsg.contains("下单") || lowerMsg.contains("收货")) {
            return "订单相关问题，请在个人中心查看我的订单哦~";
        }
        if (lowerMsg.contains("充值") || lowerMsg.contains("支付") || lowerMsg.contains("提现")) {
            return "充值功能支持支付宝沙箱测试，提现会在审核后到账哦~";
        }
        if (lowerMsg.contains("桌宠") || lowerMsg.contains("皮肤") || lowerMsg.contains("形象")) {
            return "桌宠皮肤可以在个人中心切换哦，有很多可爱形象~";
        }

        if (isBusinessRelated(lowerMsg)) {
            Random random = new Random();
            return MOCK_REPLIES.get(random.nextInt(MOCK_REPLIES.size() - 1));
        }

        return "我只解答本平台交易相关问题哦~";
    }

    private boolean isBusinessRelated(String msg) {
        String[] businessKeywords = {"交易", "购买", "出售", "账号", "游戏", "审核", "客服", "帮助"};
        for (String keyword : businessKeywords) {
            if (msg.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}