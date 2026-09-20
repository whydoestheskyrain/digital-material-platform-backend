package com.trade.virtual_trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Integer id;
    private Integer userId;
    private String type;
    private String title;
    private String content;
    private Integer orderId;
    private Integer status;
    private LocalDateTime createTime;
}