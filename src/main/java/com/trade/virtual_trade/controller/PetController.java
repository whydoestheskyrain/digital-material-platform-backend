package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.PetSkin;
import com.trade.virtual_trade.entity.UserPet;
import com.trade.virtual_trade.service.PetAiService;
import com.trade.virtual_trade.service.PetSkinService;
import com.trade.virtual_trade.service.UserPetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetAiService petAiService;

    @Autowired
    private PetSkinService petSkinService;

    @Autowired
    private UserPetService userPetService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> params) {
        String msg = params.get("msg");
        if (msg == null || msg.trim().isEmpty()) {
            return Result.error("请输入问题");
        }
        String reply = petAiService.getAiReply(msg.trim());
        return Result.success(reply);
    }

    @GetMapping("/user/get")
    public Result<Map<String, Object>> getUserPet(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        UserPet userPet = userPetService.getByUserId(userId);
        
        if (userPet != null) {
            result.put("posX", userPet.getPosX());
            result.put("posY", userPet.getPosY());
            result.put("skinId", userPet.getSkinId());
        } else {
            result.put("posX", 1300);
            result.put("posY", 650);
            result.put("skinId", null);
        }
        
        PetSkin defaultSkin = petSkinService.getDefault();
        if (defaultSkin != null) {
            result.put("skin", defaultSkin);
        }
        
        return Result.success(result);
    }

    @PostMapping("/user/save")
    public Result saveUserPet(@RequestBody Map<String, Object> params) {
        Integer userId = ((Number) params.get("userId")).intValue();
        Integer skinId = ((Number) params.get("skinId")).intValue();
        Integer posX = ((Number) params.get("posX")).intValue();
        Integer posY = ((Number) params.get("posY")).intValue();
        
        userPetService.saveOrUpdate(userId, skinId, posX, posY);
        return Result.success();
    }

    @GetMapping("/skin/all")
    public Result<List<PetSkin>> getAllSkins() {
        return Result.success(petSkinService.getAvailable());
    }

    @GetMapping("/skin/list")
    public Result<List<PetSkin>> getSkinList() {
        return Result.success(petSkinService.getAll());
    }

    @GetMapping("/skin/detail/{id}")
    public Result<PetSkin> getSkinDetail(@PathVariable Integer id) {
        PetSkin skin = petSkinService.getById(id);
        if (skin == null) {
            return Result.error("皮肤不存在");
        }
        return Result.success(skin);
    }

    @PostMapping("/skin/add")
    public Result addSkin(@RequestBody PetSkin petSkin) {
        petSkinService.add(petSkin);
        return Result.success();
    }

    @PutMapping("/skin/update")
    public Result updateSkin(@RequestBody PetSkin petSkin) {
        petSkinService.update(petSkin);
        return Result.success();
    }

    @DeleteMapping("/skin/delete/{id}")
    public Result deleteSkin(@PathVariable Integer id) {
        petSkinService.delete(id);
        return Result.success();
    }

    @PostMapping("/skin/setDefault/{id}")
    public Result setDefaultSkin(@PathVariable Integer id) {
        petSkinService.setDefault(id);
        return Result.success();
    }
}