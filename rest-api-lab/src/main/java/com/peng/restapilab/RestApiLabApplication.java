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

		long start = System.currentTimeMillis();

		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:19002/api/users";

		User[] users = restTemplate.getForObject(url, User[].class);

		long end = System.currentTimeMillis();
		System.out.println("the time is " + (end - start));

		SpringApplication.exit(context, () -> 0);
	}
}
