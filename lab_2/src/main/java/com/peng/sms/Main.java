package com.peng.sms;

import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<HostAndPort> nodes = new HashSet<>();
        String LOCAL_HOST = "172.18.195.82";
        nodes.add(new HostAndPort(LOCAL_HOST, 7001));
        nodes.add(new HostAndPort(LOCAL_HOST, 7002));
        nodes.add(new HostAndPort(LOCAL_HOST, 7003));
        nodes.add(new HostAndPort(LOCAL_HOST, 7004));
        nodes.add(new HostAndPort(LOCAL_HOST, 7005));
        nodes.add(new HostAndPort(LOCAL_HOST, 7006));

        UserProfileRepository repo = new UserProfileRepository(nodes);

        UserProfile user1 = new UserProfile("1001", "Alice", "alice@example.com", null);
        repo.create(user1);

        UserProfile u = repo.get("1001");
        System.out.println(u);

        u.setUsername("Alice_updated");
        repo.update(u);

        repo.delete("1001");

        repo.close();
    }
}