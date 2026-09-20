package com.trade.virtual_trade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {

    private Integer id;             // 商品ID
    private Integer sellerId;       // 卖家ID
    private Integer gameTypeId;     // 游戏类型ID
    private String gameType;        // 游戏类型名称
    private String gameTypeName;    // 游戏类型名称（用于前端显示）
    private String title;           // 商品标题
    private String gameServer;      // 区服
    private Integer roleLevel;      // 角色等级
    private BigDecimal price;       // 价格
    private String description;     // 商品描述
    private String detailDesc;      // 商品详情描述（富文本）
    private String detailImages;    // 商品详情图片（JSON数组）
    private String gameAccount;     // 游戏账号（管理员可见）
    private String gamePassword;    // 游戏密码（管理员可见）
    private String bindInfo;        // 绑定信息
    private Integer status;         // 状态 0待审核 1已上架 2拒绝 3下架

    private String rejectReason;    // 拒绝原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String imageUrl;
}