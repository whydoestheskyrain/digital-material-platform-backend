package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.UserPet;
import com.trade.virtual_trade.mapper.UserPetMapper;
import com.trade.virtual_trade.service.UserPetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPetServiceImpl implements UserPetService {

    @Autowired
    private UserPetMapper userPetMapper;

    @Override
    public UserPet getByUserId(Integer userId) {
        return userPetMapper.selectByUserId(userId);
    }

    @Override
    public void saveOrUpdate(Integer userId, Integer skinId, Integer posX, Integer posY) {
        UserPet existing = userPetMapper.selectByUserId(userId);
        if (existing != null) {
            existing.setSkinId(skinId);
            existing.setPosX(posX);
            existing.setPosY(posY);
            userPetMapper.update(existing);
        } else {
            UserPet userPet = new UserPet();
            userPet.setUserId(userId);
            userPet.setSkinId(skinId);
            userPet.setPosX(posX != null ? posX : 1300);
            userPet.setPosY(posY != null ? posY : 650);
            userPetMapper.insert(userPet);
        }
    }

    @Override
    public void deleteByUserId(Integer userId) {
        userPetMapper.deleteByUserId(userId);
    }
}