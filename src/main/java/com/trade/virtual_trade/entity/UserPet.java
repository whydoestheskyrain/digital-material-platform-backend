package com.trade.virtual_trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserPet {
    private Integer id;
    private Integer userId;
    private Integer skinId;
    private Integer posX;
    private Integer posY;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}