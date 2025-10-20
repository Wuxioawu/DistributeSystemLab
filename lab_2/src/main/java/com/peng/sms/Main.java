package com.peng.sms;

import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("127.0.0.1", 7001));
        nodes.add(new HostAndPort("127.0.0.1", 7002));
        nodes.add(new HostAndPort("127.0.0.1", 7003));

        UserProfileRepository repo = new UserProfileRepository(nodes);

        // 创建用户
        UserProfile user1 = new UserProfile("1001", "Alice", "alice@example.com", null);
        repo.create(user1);

        // 获取用户
        UserProfile u = repo.get("1001");
        System.out.println(u);

        // 更新用户
        u.setUsername("Alice_updated");
        repo.update(u);

        // 删除用户
        repo.delete("1001");

        repo.close();
    }
}