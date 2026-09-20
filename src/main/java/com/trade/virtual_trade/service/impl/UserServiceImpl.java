package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.User;
import com.trade.virtual_trade.mapper.UserMapper;
import com.trade.virtual_trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户Service实现类
 * 真正处理业务逻辑，调用Mapper操作数据库
 */
@Service // 标记为业务层组件，交给Spring管理
public class UserServiceImpl implements UserService {
    @Override
    @Transactional
    public boolean updatePhone(Integer userId, String newPhone) {
        // 1. 校验手机号格式 11位数字
        if (!newPhone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式错误");
        }
        // 2. 查询该手机号是否被其他用户占用
        User existUser = userMapper.selectByPhone(newPhone);
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new RuntimeException("该手机号已被其他账号绑定");
        }
        // 3. 更新手机号
        userMapper.updatePhone(userId, newPhone);
        return true;
    }

    @Override
    public User selectBalanceById(Integer userId) {
        return userMapper.selectBalanceById(userId);
    }

    @Override
    public int addBalance(Integer userId, Double amount) {
        return userMapper.addBalance(userId, amount);
    }

    @Override
    public int freezeBalance(Integer userId, Double amount) {
        return userMapper.freezeBalance(userId, amount);
    }

    @Override
    public int unfreezeToBalance(Integer userId, Double amount) {
        return userMapper.unfreezeToBalance(userId, amount);
    }
    // 注入Mapper
    @Autowired
    private UserMapper userMapper;

    @Override
    public void updateNickname(User user) {
        userMapper.updateNickname(user);
    }

    @Override
    public boolean updatePassword(Integer id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user != null && user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            userMapper.update(user);
            return true;
        }
        return false;
    }

    @Override
    public User login(String username, String password) {
        return userMapper.login(username,password);
    }






    // 查询所有用户
    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    // 根据ID查询
    @Override
    public User findById(Integer id) {
        return userMapper.selectById(id);
    }

    // 新增
    @Override
    public int add(User user) {
        return userMapper.insert(user);
    }

    // 修改
    @Override
    public int update(User user) {
        return userMapper.update(user);
    }

    // 删除
    @Override
    public int delete(Integer id) {
        return userMapper.deleteById(id);
    }
    @Override
    public List<User> findPage(int page, int size) {
        int offset = (page - 1) * size;
        return userMapper.findPage(offset, size);
    }
}