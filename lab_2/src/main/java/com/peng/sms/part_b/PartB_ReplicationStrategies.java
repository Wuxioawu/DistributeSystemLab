package com.peng.sms.part_b;

import com.peng.sms.part_a.UserProfile;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.*;
import java.text.DecimalFormat;

/**
 * Part B: Replication Strategies
 * Demonstrates write concerns and replication behavior
 */
public class PartB_ReplicationStrategies {
    
    private static final String REDIS_HOST = "localhost";
    private static final int PRIMARY_PORT = 7001;
    private static final DecimalFormat df = new DecimalFormat("#.##");
    
    private List<String> observations = new ArrayList<>();
    
    public static void main(String[] args) {
        PartB_ReplicationStrategies partB = new PartB_ReplicationStrategies();
        partB.run();
        partB.printObservations();
    }
    
    public void run() {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║        Part B: Replication Strategies             ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");
        
        // Test 1: Write Concerns
//        testWriteConcerns();
        
        // Test 2: Leader-Follower Model
        testLeaderFollowerModel();
//
//        // Test 3: Failover Simulation
//        testFailoverBehavior();
    }
    
    /**
     * Test 1: Replication Factor / Write Concern
     */
    private void testWriteConcerns() {
        System.out.println("═══ Test 1: Write Concerns & Replication Factor ═══\n");
        
        try (Jedis jedis = new Jedis(REDIS_HOST, PRIMARY_PORT)) {
            
            String info = jedis.info("replication");
            int replicaCount = countReplicas(info);
            
            System.out.println("Cluster Configuration:");
            System.out.println("  Primary: 1 node");
            System.out.println("  Replicas: " + replicaCount + " nodes");
            System.out.println("  Replication Factor: " + (1 + replicaCount) + "\n");
            
            // Test different write concerns
            testWriteConcernLevel("ONE (No Wait)", 0, 0);
            testWriteConcernLevel("QUORUM (Wait for 1)", 1, 1000);
            testWriteConcernLevel("ALL (Wait for " + replicaCount + ")", replicaCount, 1000);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void testWriteConcernLevel(String levelName, int replicas, int timeout) {
        System.out.println("--- Testing Write Concern: " + levelName + " ---");
        
        try (Jedis jedis = new Jedis(REDIS_HOST, PRIMARY_PORT)) {
            
            List<Long> latencies = new ArrayList<>();
            int operations = 100;
            int successCount = 0;
            
            for (int i = 0; i < operations; i++) {
                try {
                    UserProfile user = new UserProfile(
                        "test_" + levelName + "_" + i,
                        "user_" + i,
                        "user" + i + "@test.com"
                    );
                    
                    String key = "wc:" + levelName + ":" + i;
                    
                    long start = System.nanoTime();
                    jedis.set(key, user.toJson());
                    
                    // Apply write concern
                    if (replicas > 0) {
                        long replicatedTo = jedis.waitReplicas(replicas, timeout);
                        if (replicatedTo < replicas) {
                            System.out.println("  ⚠ Only replicated to " + replicatedTo + " of " + replicas + " replicas");
                        }
                    }
                    
                    long latency = (System.nanoTime() - start) / 1_000_000; // Convert to ms
                    latencies.add(latency);
                    successCount++;
                    
                } catch (Exception e) {
                    System.err.println("  ✗ Operation " + i + " failed: " + e.getMessage());
                }
            }
            
            // Calculate statistics
            if (!latencies.isEmpty()) {
                double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
                long minLatency = Collections.min(latencies);
                long maxLatency = Collections.max(latencies);
                double p95 = calculatePercentile(latencies, 95);
                double p99 = calculatePercentile(latencies, 99);
                
                System.out.println("\nResults for " + levelName + ":");
                System.out.println("  Operations: " + operations);
                System.out.println("  Success rate: " + (successCount * 100.0 / operations) + "%");
                System.out.println("  Average latency: " + df.format(avgLatency) + " ms");
                System.out.println("  Min latency: " + minLatency + " ms");
                System.out.println("  Max latency: " + maxLatency + " ms");
                System.out.println("  P95 latency: " + df.format(p95) + " ms");
                System.out.println("  P99 latency: " + df.format(p99) + " ms");
                
                // Analyze durability
                String durability = analyzeDurability(replicas);
                System.out.println("  Durability: " + durability);
                
                // Add observation
                observations.add(String.format(
                    "**Write Concern %s**: Avg latency %.2f ms, P95 %.2f ms. %s. Success rate: %.1f%%",
                    levelName, avgLatency, p95, durability, (successCount * 100.0 / operations)
                ));
            }
            
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error in test: " + e.getMessage());
        }
    }
    
    /**
     * Test 2: Leader-Follower Model
     */
    private void testLeaderFollowerModel() {
        System.out.println("\n═══ Test 2: Leader-Follower (Primary-Backup) Model ═══\n");
        
        try (Jedis primary = new Jedis(REDIS_HOST, PRIMARY_PORT);
             Jedis replica1 = new Jedis(REDIS_HOST, 7005)) {

            System.out.println("--- Demonstrating Primary-Replica Architecture ---\n");
            
            // Show roles
            System.out.println("Node Roles:");
            System.out.println("  Primary (7001): Handles all WRITES");
            System.out.println("  Replica 1 (7005): Read-only, receives updates from primary");

            
            // Write to primary
            UserProfile testUser = new UserProfile(
                "leader_test_001",
                "leader_test_user",
                "leader@test.com"
            );
            String key = "leader:test:001";
            
            System.out.println("Step 1: Writing to PRIMARY");
            long writeStart = System.nanoTime();
            primary.set(key, testUser.toJson());
            long writeTime = (System.nanoTime() - writeStart) / 1_000_000;
            System.out.println("  ✓ Write completed in " + writeTime + " ms");
            System.out.println("  Data: " + testUser);
            
            // Wait for replication
            System.out.println("\nStep 2: Waiting for replication...");
            long replicatedTo = primary.waitReplicas(1, 2000);

            System.out.println("  ✓ Replicated to " + replicatedTo + " replica(s)");
            
            // Read from primary
            System.out.println("\nStep 3: Reading from PRIMARY");
            String primaryData = primary.get(key);
            System.out.println("  ✓ Read successful: " + UserProfile.fromJson(primaryData).getUsername());
            
            // Read from replicas
            System.out.println("\nStep 4: Reading from REPLICAS");
            replica1.readonly();
            String replica1Data = replica1.get(key);
            System.out.println("  ✓ Replica 1: " + UserProfile.fromJson(replica1Data).getUsername());
            
            // Try to write to replica (should fail)
            System.out.println("\nStep 5: Attempting to write to REPLICA (should fail)");
            try {
                replica1.set("test:key", "test:value");
                System.out.println("  ✗ Unexpected: Write to replica succeeded!");
            } catch (JedisDataException e) {
                System.out.println("  ✓ Expected: Write rejected - " + e.getMessage());
            }
            
            observations.add("**Leader-Follower Model**: Primary handles all writes. " +
                           "Replicas are read-only. Asynchronous replication with " +
                           "typical lag < 10ms. Write latency ~" + writeTime + "ms.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Test 3: Failover Behavior
     */
    private void testFailoverBehavior() {
        System.out.println("\n═══ Test 3: Failover Simulation ═══\n");
        
        System.out.println("--- Failover Guide ---");
        System.out.println("To simulate failover:");
        System.out.println("1. Manual failover:");
        System.out.println("   docker exec -it redis-primary redis-cli DEBUG SLEEP 30");
        System.out.println("   docker exec -it redis-replica1 redis-cli REPLICAOF NO ONE");
        System.out.println();
        System.out.println("2. Automatic failover (with Sentinel):");
        System.out.println("   docker exec -it redis-primary redis-cli SHUTDOWN");
        System.out.println("   (Sentinel will auto-promote a replica in ~5-10 seconds)");
        System.out.println();
        
        // Demonstrate monitoring during normal operations
        System.out.println("--- Monitoring Write Availability ---");
        try (Jedis jedis = new Jedis(REDIS_HOST, PRIMARY_PORT)) {
            
            int attempts = 10;
            int successCount = 0;
            
            for (int i = 0; i < attempts; i++) {
                try {
                    String key = "failover:monitor:" + i;
                    String value = "data_" + System.currentTimeMillis();
                    
                    jedis.set(key, value);
                    successCount++;
                    System.out.println("  [" + i + "] ✓ Write successful");
                    
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.err.println("  [" + i + "] ✗ Write failed: " + e.getMessage());
                }
            }
            
            System.out.println("\nAvailability: " + (successCount * 100.0 / attempts) + "%");
            
            observations.add("**Failover Behavior**: Manual failover requires manual promotion. " +
                           "Sentinel provides automatic failover in 5-10 seconds. " +
                           "Brief downtime occurs during election.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    // Utility methods
    private double calculatePercentile(List<Long> values, double percentile) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
    
    private int countReplicas(String info) {
        for (String line : info.split("\n")) {
            if (line.startsWith("connected_slaves:")) {
                return Integer.parseInt(line.split(":")[1].trim());
            }
        }
        return 0;
    }
    
    private String analyzeDurability(int replicas) {
        if (replicas == 0) return "LOW - No replication guarantee, data loss possible";
        if (replicas == 1) return "MEDIUM - Replicated to 1 node, tolerates 1 failure";
        return "HIGH - Replicated to " + replicas + " nodes, tolerates " + replicas + " failures";
    }
    
    private void printObservations() {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║           Part B: Key Observations                ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");
        
        for (int i = 0; i < observations.size(); i++) {
            System.out.println((i + 1) + ". " + observations.get(i));
            System.out.println();
        }
    }
}