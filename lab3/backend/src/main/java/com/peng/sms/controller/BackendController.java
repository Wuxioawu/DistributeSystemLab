package com.peng.sms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class BackendController {

    private final Random random = new Random();

    // configure via env vars or application.properties
    @Value("${app.failRate:0.20}")
    private double failRate;

    @Value("${app.slowRate:0.30}")
    private double slowRate;

    @Value("${app.maxDelayMs:3000}")
    private int maxDelayMs;

    @GetMapping("/data")
    public Map<String, Object> getData() throws InterruptedException {
        double r = random.nextDouble();
        if (r < slowRate) {
            int delay = 200 + random.nextInt(maxDelayMs);
            Thread.sleep(delay);
            return Map.of("status", "slow", "delay_ms", delay);
        } else if (r < slowRate + failRate) {
            throw new RuntimeException("Simulated internal server error");
        }
        return Map.of("status", "ok");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
