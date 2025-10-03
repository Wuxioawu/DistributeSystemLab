package com.peng.restapilab.client;

import com.peng.restapilab.model.User;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class RestClientSpring {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:19001/api/users";

        User[] users = restTemplate.getForObject(url, User[].class);
        List<User> userList = Arrays.asList(users);

        long end = System.currentTimeMillis();
        System.out.println("the time is " + (end - start));
//        userList.forEach(user -> System.out.println(user));
    }
}
