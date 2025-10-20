package com.peng.sms;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserProfileRepository {
    private JedisCluster jedisCluster;
    private static final String KEY_PREFIX = "user:profile:";

    public UserProfileRepository(Set<HostAndPort> clusterNodes) {
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);

        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(5000)
                .socketTimeoutMillis(5000)
                .build();

        this.jedisCluster = new JedisCluster(
                clusterNodes,
                clientConfig,
                5,
                poolConfig
        );
    }

    private String getKey(String userId) {
        return KEY_PREFIX + userId;
    }

    public boolean create(UserProfile profile) {
        String key = getKey(profile.getUserId());
        String result = jedisCluster.set(key, profile.toJson());
        return "OK".equals(result);
    }

    public UserProfile get(String userId) {
        String key = getKey(userId);
        String data = jedisCluster.get(key);
        return data != null ? UserProfile.fromJson(data) : null;
    }

    public boolean update(UserProfile profile) {
        return create(profile);
    }

    public boolean delete(String userId) {
        return jedisCluster.del(getKey(userId)) > 0;
    }

    public boolean exists(String userId) {
        return jedisCluster.exists(getKey(userId));
    }

    public List<String> getAllUserIds() {
        return new ArrayList<>();
    }

    public int count() {
        return getAllUserIds().size();
    }

    public void close() {
        try {
            jedisCluster.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}