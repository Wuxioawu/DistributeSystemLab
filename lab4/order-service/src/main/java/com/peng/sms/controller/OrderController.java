package com.peng.sms.controller;

import com.peng.sms.model.Order;
import com.peng.sms.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository repo;
    private final WebClient webClient;

    public OrderController(OrderRepository repo, WebClient webClient) {
        this.repo = repo;
        this.webClient = webClient;
    }

    @GetMapping
    public List<Order> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Order>> create(@RequestBody Order order) {
        // Validate user exists by calling user-service
        String userUrl = "http://user-service:8081/users/" + order.getUserId();
        return webClient.get()
                .uri(userUrl)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    // user exists; save order
                    Order saved = repo.save(order);
                    return ResponseEntity.created(URI.create("/orders/" + saved.getId())).body(saved);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
