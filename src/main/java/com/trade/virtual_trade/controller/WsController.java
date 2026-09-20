package com.trade.virtual_trade.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws")
public class WsController {

    @GetMapping
    public void ignoreWs() {
        // 空实现，只是为了让前端的 ws 请求不报错
    }
}