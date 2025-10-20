package com.peng.sms;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

public class SimpleTest {
    public static void main(String[] args) {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("172.18.195.82", 7001));

        try (JedisCluster jedis = new JedisCluster(nodes)) {
            System.out.println("✓ sucess！");
            jedis.set("test", "hello");
            String value = jedis.get("test");
            System.out.println("✓ write and read success！value = " + value);
        } catch (Exception e) {
            System.err.println("✗ connect fail：");
            e.printStackTrace();
        }
    }
}
