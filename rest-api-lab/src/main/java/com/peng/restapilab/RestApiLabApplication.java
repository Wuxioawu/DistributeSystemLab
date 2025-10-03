package com.peng.restapilab;

import com.peng.restapilab.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class RestApiLabApplication {
    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(RestApiLabApplication.class, args);
        System.out.println("--------------------------------------- the rest-api-lab start ---------------------------------------");
        long start = System.currentTimeMillis();
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:19002/api/users";
        User[] users = restTemplate.getForObject(url, User[].class);
        long end = System.currentTimeMillis();
        System.out.println("rest-api-lab: the time is " + (end - start));
        System.out.println("--------------------------------------- the rest-api-lab end ---------------------------------------");
        SpringApplication.exit(context, () -> 0);
    }
}
