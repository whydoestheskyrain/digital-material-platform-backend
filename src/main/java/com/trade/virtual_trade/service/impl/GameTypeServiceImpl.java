package com.trade.virtual_trade.service.impl;

import com.trade.virtual_trade.entity.GameType;
import com.trade.virtual_trade.mapper.GameTypeMapper;
import com.trade.virtual_trade.service.GameTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameTypeServiceImpl implements GameTypeService {

    @Autowired
    private GameTypeMapper gameTypeMapper;

    @Override
    public List<GameType> getAll() {
        return gameTypeMapper.findAll();
    }

    @Override
    public void add(GameType gameType) {
        gameTypeMapper.add(gameType);
    }

    @Override
    public void update(GameType gameType) {
        gameTypeMapper.update(gameType);
    }

    @Override
    public void delete(Integer id) {
        gameTypeMapper.delete(id);
    }

    @Override
    public GameType getById(Integer id) {
        return gameTypeMapper.findById(id);
    }
}