package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.User;
import java.util.List;

/**
 * 用户Service接口
 * 定义用户相关的业务方法
 */
public interface UserService {
    /**
     * 修改手机号
     * @param userId 当前用户ID
     * @param newPhone 新手机号
     * @return true成功 / false失败（手机号已存在）
     */
    boolean updatePhone(Integer userId, String newPhone);

    void updateNickname(User user);
    boolean updatePassword(Integer id, String oldPassword, String newPassword);

    User login(String username,String password);
    // 查询所有用户
    List<User> findAll();

    // 根据ID查询用户
    User findById(Integer id);

    // 新增用户
    int add(User user);

    // 修改用户
    int update(User user);

    // 删除用户
    int delete(Integer id);
    List<User> findPage(int page, int size);


        // ... 已有方法
        User selectBalanceById(Integer userId);
        int addBalance(Integer userId, Double amount);
        int freezeBalance(Integer userId, Double amount);
        int unfreezeToBalance(Integer userId, Double amount);

}