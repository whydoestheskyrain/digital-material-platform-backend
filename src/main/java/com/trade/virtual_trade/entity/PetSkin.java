package com.trade.virtual_trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetSkin {
    private Integer id;
    private String skinName;
    private String idleAni;
    private String clickAni;
    private String mainImg;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
}