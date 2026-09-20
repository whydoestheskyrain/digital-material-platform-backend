package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.GameType;
import com.trade.virtual_trade.service.GameTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gameType")
public class GameTypeController {

    @Autowired
    private GameTypeService gameTypeService;

    @GetMapping("/list")
    public Result list() {
        List<GameType> list = gameTypeService.getAll();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result add(@RequestBody GameType gameType) {
        // 👇 加固：防止 type_name 为空
        if (gameType.getTypeName() == null || gameType.getTypeName().trim().isEmpty()) {
            return Result.error("游戏类型名称不能为空");
        }
        gameTypeService.add(gameType);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody GameType gameType) {
        // 👇 加固：防止 type_name 为空
        if (gameType.getTypeName() == null || gameType.getTypeName().trim().isEmpty()) {
            return Result.error("游戏类型名称不能为空");
        }
        gameTypeService.update(gameType);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        gameTypeService.delete(id);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        return Result.success(gameTypeService.getById(id));
    }
}