package com.trade.virtual_trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Feedback {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Integer status;
    private String reply;
    private LocalDateTime createTime;
    private LocalDateTime replyTime;
}