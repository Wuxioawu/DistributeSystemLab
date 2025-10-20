package com.peng.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;

/**
 * Repository for managing UserProfile in Redis Cluster
 */
public class UserProfileRepository implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileRepository.class);
    private static final String KEY_PREFIX = "user:profile:";

    private final JedisCluster jedisCluster;

    public UserProfileRepository(Set<HostAndPort> clusterNodes) {
        this.jedisCluster = new JedisCluster(clusterNodes);
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
        return create(profile);  // SET will overwrite existing key
    }

    public boolean delete(String userId) {
        return jedisCluster.del(getKey(userId)) > 0;
    }

    public boolean exists(String userId) {
        return jedisCluster.exists(getKey(userId));
    }

    public List<String> getAllUserIds() {
        List<String> userIds = new ArrayList<>();
        // JedisCluster#getClusterNodes() returns a map of nodeId -> ConnectionPool
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();

        for (JedisPool pool : clusterNodes.values()) {
            try (Jedis jedis = pool.getResource()) {
                String cursor = "0";
                do {
                    ScanResult<String> scanResult = jedis.scan(cursor, new ScanParams().match(KEY_PREFIX + "*").count(100));
                    cursor = scanResult.getCursor();
                    List<String> keys = scanResult.getResult();
                    for (String key : keys) {
                        userIds.add(key.replace(KEY_PREFIX, ""));
                    }
                } while (!"0".equals(cursor));
            }
        }
        return userIds;
    }

    public int count() {
        return getAllUserIds().size();
    }

    public void close() {
        try {
            jedisCluster.close();
        } catch (Exception e) {
            logger.error("Error closing JedisCluster", e);
        }
    }
}
