package com.trade.virtual_trade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String phone;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String nickname;
    private String role;
    private String avatar;
    // 在 User 类中添加
    private BigDecimal balance;      // 可用余额
    private BigDecimal frozenBalance; // 冻结余额（担保模式可选）
}