package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.Feedback;
import com.trade.virtual_trade.service.FeedbackService;
import com.trade.virtual_trade.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/submit")
    public Result submit(@RequestBody Feedback feedback) {
        feedbackService.submit(feedback);
        return Result.success();
    }

    @GetMapping("/list/{userId}")
    public Result list(@PathVariable Integer userId) {
        return Result.success(feedbackService.getByUserId(userId));
    }

    @GetMapping("/admin/list")
    public Result adminList() {
        return Result.success(feedbackService.getAll());
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        return Result.success(feedbackService.getById(id));
    }

    @PostMapping("/reply/{id}")
    public Result reply(@PathVariable Integer id, @RequestParam String reply) {
        feedbackService.reply(id, reply);
        Feedback feedback = feedbackService.getById(id);
        if (feedback != null) {
            notificationService.sendFeedbackNotification(
                feedback.getUserId(),
                "反馈已回复",
                "您的反馈「" + feedback.getTitle() + "」已收到回复：" + reply
            );
        }
        return Result.success();
    }

    @PutMapping("/close/{id}")
    public Result close(@PathVariable Integer id) {
        feedbackService.close(id);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        feedbackService.delete(id);
        return Result.success();
    }
}