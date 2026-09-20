package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list/{userId}")
    public Result list(@PathVariable Integer userId) {
        return Result.success(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread/{userId}")
    public Result unread(@PathVariable Integer userId) {
        return Result.success(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/count/{userId}")
    public Result count(@PathVariable Integer userId) {
        return Result.success(notificationService.countUnread(userId));
    }

    @PutMapping("/read/{id}")
    public Result read(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/readAll/{userId}")
    public Result readAll(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return Result.success();
    }
}