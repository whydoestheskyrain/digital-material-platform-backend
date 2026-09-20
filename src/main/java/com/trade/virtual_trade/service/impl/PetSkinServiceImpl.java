package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.PetSkin;
import com.trade.virtual_trade.mapper.PetSkinMapper;
import com.trade.virtual_trade.service.PetSkinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetSkinServiceImpl implements PetSkinService {

    @Autowired
    private PetSkinMapper petSkinMapper;

    @Override
    public void add(PetSkin petSkin) {
        if (petSkin.getIsDefault() == null) {
            petSkin.setIsDefault(0);
        }
        if (petSkin.getStatus() == null) {
            petSkin.setStatus(1);
        }
        if (petSkin.getIsDefault() == 1) {
            petSkinMapper.clearDefault();
        }
        petSkinMapper.insert(petSkin);
    }

    @Override
    public List<PetSkin> getAll() {
        return petSkinMapper.selectAll();
    }

    @Override
    public List<PetSkin> getAvailable() {
        return petSkinMapper.selectByStatus(1);
    }

    @Override
    public PetSkin getById(Integer id) {
        return petSkinMapper.selectById(id);
    }

    @Override
    public PetSkin getDefault() {
        PetSkin defaultSkin = petSkinMapper.selectDefault();
        if (defaultSkin == null) {
            List<PetSkin> skins = getAvailable();
            if (!skins.isEmpty()) {
                return skins.get(0);
            }
        }
        return defaultSkin;
    }

    @Override
    public void update(PetSkin petSkin) {
        petSkinMapper.update(petSkin);
    }

    @Override
    public void delete(Integer id) {
        petSkinMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Integer id) {
        petSkinMapper.clearDefault();
        petSkinMapper.updateDefault(id);
    }
}