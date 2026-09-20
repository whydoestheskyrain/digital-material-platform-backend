package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.Notification;
import com.trade.virtual_trade.mapper.NotificationMapper;
import com.trade.virtual_trade.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void sendOrderNotification(Integer userId, String title, String content, Integer orderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("order");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setOrderId(orderId);
        notification.setStatus(0);
        notificationMapper.insert(notification);
    }

    @Override
    public void sendBroadcast(String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(null);
        notification.setType("broadcast");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus(0);
        notificationMapper.insert(notification);
    }

    @Override
    public void sendFeedbackNotification(Integer userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("feedback");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus(0);
        notificationMapper.insert(notification);
    }

    @Override
    public List<Notification> getNotifications(Integer userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationMapper.selectUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Integer id) {
        notificationMapper.updateStatus(id, 1);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        notificationMapper.updateStatusByUserId(userId, 1);
    }

    @Override
    public int countUnread(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void delete(Integer id) {
        notificationMapper.deleteById(id);
    }
}