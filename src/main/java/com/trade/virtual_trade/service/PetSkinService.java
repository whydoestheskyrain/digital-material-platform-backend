package com.trade.virtual_trade.service;

import com.trade.virtual_trade.entity.PetSkin;

import java.util.List;

public interface PetSkinService {
    void add(PetSkin petSkin);
    List<PetSkin> getAll();
    List<PetSkin> getAvailable();
    PetSkin getById(Integer id);
    PetSkin getDefault();
    void update(PetSkin petSkin);
    void delete(Integer id);
    void setDefault(Integer id);
}