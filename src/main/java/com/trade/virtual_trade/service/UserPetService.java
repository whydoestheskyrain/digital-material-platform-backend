package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.UserPet;

public interface UserPetService {
    UserPet getByUserId(Integer userId);
    void saveOrUpdate(Integer userId, Integer skinId, Integer posX, Integer posY);
    void deleteByUserId(Integer userId);
}