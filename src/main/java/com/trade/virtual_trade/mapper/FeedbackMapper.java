package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedbackMapper {
    void insert(Feedback feedback);
    List<Feedback> selectByUserId(@Param("userId") Integer userId);
    List<Feedback> selectAll();
    Feedback selectById(@Param("id") Integer id);
    void updateReply(@Param("id") Integer id, @Param("reply") String reply, @Param("replyTime") java.time.LocalDateTime replyTime);
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    void deleteById(@Param("id") Integer id);
}