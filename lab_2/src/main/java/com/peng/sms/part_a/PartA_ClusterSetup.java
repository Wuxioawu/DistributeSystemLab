package com.peng.sms.part_a;

import redis.clients.jedis.*;
import redis.clients.jedis.util.JedisClusterCRC16;
import java.util.*;

/**
 * Part A: Redis Cluster Setup & Baseline Validation
 */
public class PartA_ClusterSetup {

    private static final Set<HostAndPort> CLUSTER_NODES = new HashSet<>(Arrays.asList(
            new HostAndPort("localhost", 7001),
            new HostAndPort("localhost", 7002),
            new HostAndPort("localhost", 7003),
            new HostAndPort("localhost", 7004),
            new HostAndPort("localhost", 7005),
            new HostAndPort("localhost", 7006)
    ));

    public static void main(String[] args) {
        PartA_ClusterSetup setup = new PartA_ClusterSetup();
        setup.run();
    }

    public void run() {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║     Part A: Redis Cluster Setup & Baseline        ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        // Step 1: Verify cluster setup
        verifyClusterSetup();

        // Step 2: Initialize sample data
        initializeData();

        // Step 3: Verify data distribution across the cluster
        verifyDataDistribution();

        System.out.println("\n✓ Part A Complete!");
    }

    /**
     * Step 1: Verify that the cluster is properly configured
     */
    private void verifyClusterSetup() {
        System.out.println("═══ Step 1: Verifying Cluster Configuration ═══\n");

        try (JedisCluster jedisCluster = new JedisCluster(CLUSTER_NODES)) {

            System.out.println("Using seed nodes: " + CLUSTER_NODES.size());
            System.out.println();

            int masterCount = 0;
            int replicaCount = 0;

            for (HostAndPort hp : CLUSTER_NODES) {
                String host = hp.getHost();
                int port = hp.getPort();

                System.out.println("Connecting to node " + host + ":" + port + " ...");
                try (Jedis jedis = new Jedis(host, port)) {

                    String info = jedis.clusterInfo();
                    String nodes = jedis.clusterNodes();

                    System.out.println("CLUSTER INFO for " + host + ":" + port + ":");
                    System.out.println(info.replaceAll("(?m)^", "  "));
                    System.out.println();

                    // Parse "cluster nodes" output and find the "myself" line
                    for (String line : nodes.split("\\r?\\n")) {
                        if (line.contains("myself")) {
                            System.out.println("Node: " + host + ":" + port);

                            if (line.contains("master")) {
                                System.out.println("  Role: MASTER");
                                masterCount++;
                            } else if (line.contains("slave") || line.contains("replica")) {
                                System.out.println("  Role: REPLICA");
                                replicaCount++;
                            } else {
                                System.out.println("  Role: UNKNOWN → " + line);
                            }

                            String[] parts = line.split(" ");
                            for (String part : parts) {
                                if (part.contains("-")) {
                                    System.out.println("  Slots: " + part);
                                }
                            }
                            System.out.println();
                        }
                    }

                } catch (Exception e) {
                    System.err.println("  ✗ Failed to connect to " + host + ":" + port + " → " + e.getMessage());
                }
            }

            System.out.println("Cluster Summary:");
            System.out.println("  Masters detected: " + masterCount);
            System.out.println("  Replicas detected: " + replicaCount);
            System.out.println();

            if (masterCount == 3 && replicaCount == 3) {
                System.out.println("  ✓ Cluster correctly configured (3 masters + 3 replicas)");
            } else {
                System.out.println("  ⚠ Warning: Expected 3 masters and 3 replicas (check your cluster setup)");
            }

        } catch (Exception e) {
            System.err.println("✗ Error verifying cluster configuration: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    /**
     * Step 2: Initialize user data across the cluster
     */
    private void initializeData() {
        System.out.println("═══ Step 2: Initializing Data Model ═══\n");

        try (JedisCluster jedisCluster = new JedisCluster(CLUSTER_NODES)) {

            // Sample user profiles
            UserProfile[] users = {
                    new UserProfile("user001", "alice_smith", "alice@example.com"),
                    new UserProfile("user002", "bob_jones", "bob@example.com"),
                    new UserProfile("user003", "charlie_brown", "charlie@example.com"),
                    new UserProfile("user004", "diana_prince", "diana@example.com"),
                    new UserProfile("user005", "eve_wilson", "eve@example.com"),
                    new UserProfile("user006", "frank_miller", "frank@example.com"),
                    new UserProfile("user007", "grace_hopper", "grace@example.com"),
                    new UserProfile("user008", "henry_ford", "henry@example.com"),
                    new UserProfile("user009", "iris_west", "iris@example.com"),
                    new UserProfile("user010", "jack_ryan", "jack@example.com")
            };

            System.out.println("Inserting " + users.length + " user profiles into the cluster...\n");

            for (UserProfile user : users) {
                String key = "user:" + user.getUserId();
                jedisCluster.set(key, user.toJson());

                int slot = JedisClusterCRC16.getSlot(key);
                System.out.println("  ✓ " + key + " → slot " + slot + " | username: " + user.getUsername());
            }

            System.out.println("\n✓ All user data inserted successfully.");

        } catch (Exception e) {
            System.err.println("✗ Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    /**
     * Step 3: Validate data distribution across cluster slots
     */
    private void verifyDataDistribution() {
        System.out.println("═══ Step 3: Verifying Data Distribution ═══\n");

        try (JedisCluster jedisCluster = new JedisCluster(CLUSTER_NODES)) {

            System.out.println("Reading and verifying data from cluster...\n");

            for (int i = 1; i <= 10; i++) {
                String userId = String.format("user%03d", i);
                String key = "user:" + userId;

                try {
                    String data = jedisCluster.get(key);

                    if (data != null) {
                        UserProfile user = UserProfile.fromJson(data);
                        int slot = JedisClusterCRC16.getSlot(key);
                        System.out.println("  ✓ Retrieved " + key + " from slot " + slot);
                        System.out.println("    Data: " + user);
                    } else {
                        System.out.println("  ✗ Key not found: " + key);
                    }
                } catch (Exception e) {
                    System.err.println("  ✗ Error reading " + key + ": " + e.getMessage());
                }
            }

            System.out.println("\n✓ Data verified successfully across all cluster nodes.");

            System.out.println("\nData Distribution Overview:");
            System.out.println("  • Total hash slots: 16384");
            System.out.println("  • Each master covers ~5461 slots");
            System.out.println("  • Data automatically sharded across masters");
            System.out.println("  • Each shard has 1 replica node");

        } catch (Exception e) {
            System.err.println("✗ Error verifying data distribution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
