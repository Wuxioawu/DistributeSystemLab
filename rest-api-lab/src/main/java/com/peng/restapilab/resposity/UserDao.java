package com.peng.restapilab.resposity;


import com.peng.restapilab.model.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserDao {
    static List<User> users;

    static {
        users = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String id = "2025" + String.format("%04d", 3000 + i); // generator 20253000
            String username = "user" + i;
            String firstName = "Name" + random.nextInt(1000); // create randomly name
            users.add(new User(id, username, firstName));
        }
    }

    public List<User> getAllUsers() {
        return users;
    }

    public User getUserById(String id) {
        for (User user : users) {
            if (id.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        return users.add(user);
    }

    public User updateUser(String id, User user) {
        User userById = getUserById(id);
        users.set(users.indexOf(userById), user);
        return users.set(users.indexOf(userById), user);
    }

    public boolean deleteUser(String id) {
        User userById = getUserById(id);
        return users.remove(userById);
    }
}
