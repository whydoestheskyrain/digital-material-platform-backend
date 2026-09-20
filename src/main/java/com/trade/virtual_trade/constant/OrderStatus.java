package com.trade.virtual_trade.constant;

/**
 * 订单状态常量类
 */
public class OrderStatus {

    // ============ 订单状态 ============
    
    /**
     * 待付款
     */
    public static final Integer UNPAID = 1;
    
    /**
     * 已付款（待发货）
     */
    public static final Integer PAID = 2;
    
    /**
     * 已发货（待收货）
     */
    public static final Integer DELIVERED = 3;
    
    /**
     * 已完成
     */
    public static final Integer COMPLETED = 4;
    
    /**
     * 已取消
     */
    public static final Integer CANCELLED = 5;

    // ============ 交易类型 ============
    
    /**
     * 充值
     */
    public static final String TRANSACTION_RECHARGE = "recharge";
    
    /**
     * 支付
     */
    public static final String TRANSACTION_PAY = "pay";
    
    /**
     * 冻结
     */
    public static final String TRANSACTION_FREEZE = "freeze";
    
    /**
     * 解冻
     */
    public static final String TRANSACTION_UNFREEZE = "unfreeze";
    
    /**
     * 收入
     */
    public static final String TRANSACTION_INCOME = "income";
    
    /**
     * 提现
     */
    public static final String TRANSACTION_WITHDRAW = "withdraw";

    /**
     * 获取状态中文描述
     */
    public static String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待支付";
            case 2:
                return "待发货";
            case 3:
                return "待收货";
            case 4:
                return "已完成";
            case 5:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否是终态（已完成或已取消）
     */
    public static boolean isFinalStatus(Integer status) {
        return status != null && (status == COMPLETED || status == CANCELLED);
    }
}