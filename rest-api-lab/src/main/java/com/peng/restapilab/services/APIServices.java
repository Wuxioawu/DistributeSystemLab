package com.peng.restapilab.services;

import com.peng.restapilab.model.User;
import com.peng.restapilab.resposity.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class APIServices {

    @Autowired
    private UserDao userDao;

    public List<User> getAllUsers() {
        log.info("Get all users");
        return userDao.getAllUsers();
    }

    public User getUserById(String id) {
        log.info("Get user by id: {}", id);
        return userDao.getUserById(id);
    }

    public boolean createUser(User user) {
        log.info("Create user: {}", user);
        return userDao.addUser(user);
    }

    public User updateUser(String id, User user) {
        log.info("Update user: {}", user);
        return userDao.updateUser(id, user);
    }

    public boolean deleteUser(String id) {
        log.info("Delete user: {}", id);
        return userDao.deleteUser(id);
    }
}
