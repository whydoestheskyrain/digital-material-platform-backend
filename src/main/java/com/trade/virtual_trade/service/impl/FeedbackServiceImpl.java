package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.Feedback;
import com.trade.virtual_trade.mapper.FeedbackMapper;
import com.trade.virtual_trade.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public void submit(Feedback feedback) {
        feedback.setStatus(0);
        feedbackMapper.insert(feedback);
    }

    @Override
    public List<Feedback> getByUserId(Integer userId) {
        return feedbackMapper.selectByUserId(userId);
    }

    @Override
    public List<Feedback> getAll() {
        return feedbackMapper.selectAll();
    }

    @Override
    public Feedback getById(Integer id) {
        return feedbackMapper.selectById(id);
    }

    @Override
    public void reply(Integer id, String reply) {
        feedbackMapper.updateReply(id, reply, LocalDateTime.now());
    }

    @Override
    public void close(Integer id) {
        feedbackMapper.updateStatus(id, 2);
    }

    @Override
    public void delete(Integer id) {
        feedbackMapper.deleteById(id);
    }
}