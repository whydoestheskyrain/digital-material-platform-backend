package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    void insert(Notification notification);
    List<Notification> selectByUserId(@Param("userId") Integer userId);
    List<Notification> selectUnreadByUserId(@Param("userId") Integer userId);
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    void updateStatusByUserId(@Param("userId") Integer userId, @Param("status") Integer status);
    int countUnread(@Param("userId") Integer userId);
    void deleteById(@Param("id") Integer id);
    List<Notification> selectBroadcast();
}