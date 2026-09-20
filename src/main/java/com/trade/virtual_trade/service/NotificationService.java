package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendOrderNotification(Integer userId, String title, String content, Integer orderId);
    void sendBroadcast(String title, String content);
    void sendFeedbackNotification(Integer userId, String title, String content);
    List<Notification> getNotifications(Integer userId);
    List<Notification> getUnreadNotifications(Integer userId);
    void markAsRead(Integer id);
    void markAllAsRead(Integer userId);
    int countUnread(Integer userId);
    void delete(Integer id);
}