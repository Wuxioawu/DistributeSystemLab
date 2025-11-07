package com.peng.sms.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClientController {

    @Value("${backend.base-url}")
    private String backendBaseUrl;

    private final RestTemplate restTemplate;

    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Baseline endpoint (no resilience) - use for Part A baseline tests
    @GetMapping("/call-baseline")
    public ResponseEntity<?> callBaseline() {
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(backendBaseUrl + "/api/data", Map.class);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "backend_status", resp.getStatusCodeValue(),
                    "backend_body", resp.getBody()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // Resilient endpoint: Circuit Breaker + Retry + fallback
    @GetMapping("/call-resilient")
    @CircuitBreaker(name = "backendCB", fallbackMethod = "fallbackForBackend")
    @Retry(name = "backendRetry")
    public ResponseEntity<?> callResilient() {
        ResponseEntity<Map> resp = restTemplate.getForEntity(backendBaseUrl + "/api/data", Map.class);
        return ResponseEntity.ok(Map.of("backend_status", resp.getStatusCodeValue(), "backend_body", resp.getBody()));
    }

    public ResponseEntity<?> fallbackForBackend(Throwable t) {
        // quick fallback response if CB is open or retries exhausted
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message", "fallback: backend unavailable",
                "error", t.getMessage()
        ));
    }
}
