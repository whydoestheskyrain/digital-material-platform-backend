package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.Feedback;

import java.util.List;

public interface FeedbackService {
    void submit(Feedback feedback);
    List<Feedback> getByUserId(Integer userId);
    List<Feedback> getAll();
    Feedback getById(Integer id);
    void reply(Integer id, String reply);
    void close(Integer id);
    void delete(Integer id);
}