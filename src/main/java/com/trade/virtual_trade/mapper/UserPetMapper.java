package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.UserPet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPetMapper {
    void insert(UserPet userPet);
    UserPet selectByUserId(@Param("userId") Integer userId);
    void update(UserPet userPet);
    void deleteByUserId(@Param("userId") Integer userId);
}