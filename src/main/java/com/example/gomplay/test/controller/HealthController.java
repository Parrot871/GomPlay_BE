package com.example.gomplay.test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public  String health(){
        return "서버가 띄워졌습니다";
    }
}
