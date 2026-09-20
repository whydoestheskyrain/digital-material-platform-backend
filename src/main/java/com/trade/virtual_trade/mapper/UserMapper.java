// UserMapper.java
package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    // 更新手机号
    void updatePhone(@Param("userId") Integer userId, @Param("phone") String phone);
    // 根据手机号查询用户
    User selectByPhone(String phone);
    User login(@Param("username") String username, @Param("password") String password);
    // 查询所有用户
    List<User> selectAll();
    // 根据ID查询单个用户
    User selectById(Integer id);
    // 新增用户
    int insert(User user);
    // 修改用户信息
    int update(User user);
    // 根据ID删除用户
    int deleteById(Integer id);
    List<User> findPage(int offset, int size);
    // 注册
    int add(User user);

    // 根据用户名查询（判断重复）
    User findByUsername(String username);

    void updateNickname(User user);
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    // 在 UserMapper 接口中添加以下方法
    int addBalance(@Param("userId") Integer userId, @Param("amount") Double amount);
    int freezeBalance(@Param("userId") Integer userId, @Param("amount") Double amount);
    int unfreezeToBalance(@Param("userId") Integer userId, @Param("amount") Double amount);
    int deductBalance(@Param("userId") Integer userId, @Param("amount") Double amount);
    int transferFromFrozenToSeller(@Param("buyerId") Integer buyerId, @Param("amount") Double amount);
    User selectBalanceById(Integer userId);
}