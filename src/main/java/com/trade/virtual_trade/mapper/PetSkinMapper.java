package com.trade.virtual_trade.mapper;

import com.trade.virtual_trade.entity.PetSkin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetSkinMapper {
    void insert(PetSkin petSkin);
    List<PetSkin> selectAll();
    List<PetSkin> selectByStatus(@Param("status") Integer status);
    PetSkin selectById(@Param("id") Integer id);
    PetSkin selectDefault();
    void update(PetSkin petSkin);
    void deleteById(@Param("id") Integer id);
    void updateDefault(@Param("id") Integer id);
    void clearDefault();
}