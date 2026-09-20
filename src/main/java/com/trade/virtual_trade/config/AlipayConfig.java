package com.trade.virtual_trade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;

@Configuration
public class AlipayConfig {

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.app-private-key}")
    private String appPrivateKey;

    @Value("${alipay.public-key}")
    private String alipayPublicKey;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.recharge-notify-url}")
    private String rechargeNotifyUrl;

    private static final String FORMAT = "json";
    private static final String CHARSET = "utf-8";
    private static final String SIGN_TYPE = "RSA2";

    @Bean
    public AlipayClient alipayClient() {
        String cleanedPrivateKey = cleanKey(appPrivateKey);
        String cleanedPublicKey = cleanKey(alipayPublicKey);

        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                cleanedPrivateKey,
                FORMAT,
                CHARSET,
                cleanedPublicKey,
                SIGN_TYPE
        );
    }

    // ✅ 修复：同时清理 PKCS1 和 PKCS8 格式的密钥头
    private String cleanKey(String key) {
        if (key == null) return null;
        return key
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    public String getGatewayUrl() { return gatewayUrl; }
    public String getAppId() { return appId; }
    public String getAppPrivateKey() { return cleanKey(appPrivateKey); }
    public String getAlipayPublicKey() { return cleanKey(alipayPublicKey); }
    public String getReturnUrl() { return returnUrl; }
    public String getNotifyUrl() { return notifyUrl; }
    public String getRechargeNotifyUrl() { return rechargeNotifyUrl; }
}