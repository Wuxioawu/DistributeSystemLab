package com.peng.restapilab.services;

import com.peng.restapilab.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class APIServices {

    private static final Logger log = LoggerFactory.getLogger(APIServices.class);

    public List<User> getAllUsers() {
        log.info("Get all users");
        return null;
    }

    public User getUserById(String id) {
        log.info("Get user by id: {}", id);
        return null;
    }

    public User createUser(User user) {
        log.info("Create user: {}", user);
        return null;
    }

    public User updateUser(String id, User user) {
        log.info("Update user: {}", user);
        return null;
    }

    public void deleteUser(String id) {
        log.info("Delete user: {}", id);
    }
}
