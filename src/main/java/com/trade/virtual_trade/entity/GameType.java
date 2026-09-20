package com.trade.virtual_trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameType {
    private Integer id;
    private String typeName; // 游戏分类名称，如：王者荣耀、原神
    private LocalDateTime createTime;
}