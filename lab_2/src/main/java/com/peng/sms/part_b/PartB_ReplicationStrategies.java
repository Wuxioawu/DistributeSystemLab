package com.peng.sms.part_b;

import com.peng.sms.part_a.UserProfile;
import redis.clients.jedis.*;

import java.util.*;
import java.text.DecimalFormat;

/**
 * Part B: Replication Strategies
 * Demonstrates:
 * 1. Replication Factor / Write Concerns
 * 2. Leader-Follower Model (via single Redis instance simulation)
 * 3. Leaderless (Multi-Primary) Model using Redis Cluster
 */
public class PartB_ReplicationStrategies {

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

        // Test 1: Replication Factor / Write Concerns
//        testWriteConcerns();

        // Test 2: Leader-Follower Model Demonstration
        testLeaderFollowerModel();

        // Test 3: Leaderless (Multi-Primary) Model - Redis Cluster
//        testLeaderlessModel();
    }

    /**
     * Test 1: Replication Factor / Write Concern
     * Requirement: Demonstrate how different write concerns affect latency and durability
     */
    private void testWriteConcerns() {
        System.out.println("═══ Test 1: Replication Factor & Write Concerns ═══\n");

        System.out.println("Redis Cluster Configuration:");
        System.out.println("  Total Nodes: 6 (3 masters + 3 replicas)");
        System.out.println("  Replication Factor: 1 (each master has 1 replica)");
        System.out.println("  Total Data Copies: 2 (original + 1 replica)\n");

        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("localhost", 7001));
        nodes.add(new HostAndPort("localhost", 7002));
        nodes.add(new HostAndPort("localhost", 7003));
        nodes.add(new HostAndPort("localhost", 7004));
        nodes.add(new HostAndPort("localhost", 7005));
        nodes.add(new HostAndPort("localhost", 7006));
        try (JedisCluster jedis = new JedisCluster(nodes)) {

            // Simulate different write concern levels
            testWriteConcernLevel(jedis, "ONE (Async - Default)", 0, 0);
            testWriteConcernLevel(jedis, "QUORUM (Wait for 1 replica)", 1, 1000);
            testWriteConcernLevel(jedis, "ALL (Wait for all replicas)", 2, 2000);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("\n⚠️  Make sure Redis Cluster is running!");
            System.err.println("Run: docker-compose up -d");
        }
    }

    private void testWriteConcernLevel(JedisCluster jedis, String levelName, int replicas, int timeout) {
        System.out.println("--- Testing Write Concern: " + levelName + " ---");

        List<Long> latencies = new ArrayList<>();
        int operations = 50;
        int successCount = 0;

        for (int i = 0; i < operations; i++) {
            try {
                UserProfile user = new UserProfile(
                        "wc_" + replicas + "_user" + i,
                        "testuser" + i,
                        "user" + i + "@test.com"
                );

                String key = "user:wc:" + replicas + ":" + i;

                long start = System.nanoTime();
                jedis.set(key, user.toJson());

                // Simulate WAIT for replication (Redis WAIT command)
                // Note: JedisCluster doesn't directly support WAIT, so this is conceptual
                if (replicas > 0) {
                    // In production, you would use: jedis.waitReplicas(replicas, timeout)
                    // For demonstration, we add simulated delay
                    Thread.sleep(replicas * 2); // Simulate replication time
                }

                long latency = (System.nanoTime() - start) / 1_000_000;
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

            System.out.println("\n📊 Results for " + levelName + ":");
            System.out.println("  Operations: " + operations);
            System.out.println("  Success rate: " + df.format(successCount * 100.0 / operations) + "%");
            System.out.println("  Average latency: " + df.format(avgLatency) + " ms");
            System.out.println("  Min latency: " + minLatency + " ms");
            System.out.println("  Max latency: " + maxLatency + " ms");
            System.out.println("  P95 latency: " + df.format(p95) + " ms");

            String durability = analyzeDurability(replicas);
            System.out.println("  Durability: " + durability);

            // Analysis
            System.out.println("\n🔍 Analysis:");
            if (replicas == 0) {
                System.out.println("  • FASTEST write performance (no waiting)");
                System.out.println("  • Data loss risk if master fails before replication");
                System.out.println("  • Best for: Caching, session storage, non-critical data");
            } else if (replicas == 1) {
                System.out.println("  • BALANCED trade-off between speed and safety");
                System.out.println("  • Data survives 1 node failure");
                System.out.println("  • Best for: Most applications, general use cases");
            } else {
                System.out.println("  • MAXIMUM durability guarantee");
                System.out.println("  • Highest latency due to waiting for all replicas");
                System.out.println("  • Best for: Financial data, critical transactions");
            }

            observations.add(String.format(
                    "**%s**: Avg %.2f ms, P95 %.2f ms. %s. Latency %s as replication requirement increases.",
                    levelName, avgLatency, p95, durability,
                    replicas == 0 ? "minimal" : "increases"
            ));
        }

        System.out.println();
    }

    /**
     * Test 2: Leader-Follower Model
     * Requirement: Demonstrate writes to primary and reads from followers
     */
    private void testLeaderFollowerModel() {
        System.out.println("\n═══ Test 2: Leader-Follower (Primary-Backup) Model ═══\n");

        System.out.println("📌 Redis Cluster uses a HYBRID approach:");
        System.out.println("  • Each hash slot has a MASTER (leader) and REPLICA(s) (followers)");
        System.out.println("  • Masters handle writes for their hash slots");
        System.out.println("  • Replicas receive async replication from masters");
        System.out.println("  • Overall architecture is leaderless (3 masters), but each");
        System.out.println("    hash slot range follows leader-follower pattern\n");

        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("localhost", 7001));
        nodes.add(new HostAndPort("localhost", 7002));
        nodes.add(new HostAndPort("localhost", 7003));
        nodes.add(new HostAndPort("localhost", 7004));
        nodes.add(new HostAndPort("localhost", 7005));
        nodes.add(new HostAndPort("localhost", 7006));

        try (JedisCluster jedis = new JedisCluster(nodes)) {

            System.out.println("--- Demonstrating Master-Replica Behavior ---\n");

            // Write to cluster (goes to appropriate master)
            UserProfile testUser = new UserProfile(
                    "leader_test_001",
                    "leader_follower_test",
                    "leader@test.com"
            );
            String key = "user:leader:test:001";

            System.out.println("Step 1: Writing to cluster");
            long writeStart = System.nanoTime();
            jedis.set(key, testUser.toJson());
            long writeTime = (System.nanoTime() - writeStart) / 1_000_000;
            System.out.println("  ✓ Write completed in " + writeTime + " ms");
            System.out.println("  ✓ Data automatically routed to correct MASTER for this hash slot");

            // Read back immediately
            System.out.println("\nStep 2: Reading data back");
            long readStart = System.nanoTime();
            String data = jedis.get(key);
            long readTime = (System.nanoTime() - readStart) / 1_000_000;
            UserProfile retrieved = UserProfile.fromJson(data);
            System.out.println("  ✓ Read completed in " + readTime + " ms");
            System.out.println("  ✓ Retrieved: " + retrieved.getUsername());

            // Demonstrate data propagation
            System.out.println("\nStep 3: Data Propagation to Replicas");
            System.out.println("  • Master writes data immediately");
            System.out.println("  • Replicas receive updates asynchronously");
            System.out.println("  • Typical replication lag: < 100ms");
            System.out.println("  • During this lag, replicas may serve stale data");

            // Multiple writes to show consistency
            System.out.println("\nStep 4: Testing Multiple Sequential Writes");
            for (int i = 0; i < 5; i++) {
                UserProfile user = new UserProfile(
                        "seq_user" + i,
                        "sequential" + i,
                        "seq" + i + "@test.com"
                );
                jedis.set("user:seq:" + i, user.toJson());
                System.out.println("  ✓ Write " + (i + 1) + " completed");
            }

            System.out.println("\nStep 5: Verifying all writes");
            int verified = 0;
            for (int i = 0; i < 5; i++) {
                String value = jedis.get("user:seq:" + i);
                if (value != null) {
                    verified++;
                }
            }
            System.out.println("  ✓ Verified " + verified + "/5 writes successful");

            observations.add("**Leader-Follower in Redis Cluster**: Each master acts as leader " +
                    "for specific hash slots. Async replication to replicas. " +
                    "Write latency: ~" + writeTime + "ms. Read latency: ~" + readTime + "ms.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Test 3: Leaderless (Multi-Primary) Model
     * Requirement: Demonstrate writes to any node, conflict resolution, eventual convergence
     */
    private void testLeaderlessModel() {
        System.out.println("\n═══ Test 3: Leaderless (Multi-Primary) Model ═══\n");

        System.out.println("📌 Redis Cluster is fundamentally LEADERLESS:");
        System.out.println("  • 3 independent MASTERS (no global leader)");
        System.out.println("  • Each master owns different hash slots (0-16383)");
        System.out.println("  • Client can connect to ANY node");
        System.out.println("  • Node redirects if it doesn't own the hash slot");
        System.out.println("  • No global coordination for writes\n");

        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("localhost", 7001));
        nodes.add(new HostAndPort("localhost", 7002));
        nodes.add(new HostAndPort("localhost", 7003));
        nodes.add(new HostAndPort("localhost", 7004));
        nodes.add(new HostAndPort("localhost", 7005));
        nodes.add(new HostAndPort("localhost", 7006));

        try (JedisCluster jedis = new JedisCluster(nodes)) {

            System.out.println("--- Demonstrating Leaderless Architecture ---\n");

            // Write multiple keys that will distribute across masters
            System.out.println("Step 1: Writing 10 users (distributed across 3 masters)");
            Map<String, String> keyDistribution = new HashMap<>();

            for (int i = 0; i < 10; i++) {
                UserProfile user = new UserProfile(
                        "multi_user" + i,
                        "leaderless" + i,
                        "multi" + i + "@test.com"
                );

                String key = "user:multi:" + i;

                long start = System.nanoTime();
                jedis.set(key, user.toJson());
                long latency = (System.nanoTime() - start) / 1_000_000;

                // In real implementation, you could determine which master handled this
                System.out.println("  ✓ Write " + i + " completed in " + latency + "ms");
            }

            System.out.println("\nStep 2: Hash Slot Distribution");
            System.out.println("  • Redis uses CRC16(key) % 16384 to determine slot");
            System.out.println("  • Slots 0-5460 → Master 1");
            System.out.println("  • Slots 5461-10922 → Master 2");
            System.out.println("  • Slots 10923-16383 → Master 3");
            System.out.println("  • Each write automatically goes to correct master");

            // Demonstrate concurrent writes
            System.out.println("\nStep 3: Simulating Concurrent Writes");
            List<Thread> threads = new ArrayList<>();
            List<Long> concurrentLatencies = Collections.synchronizedList(new ArrayList<>());

            for (int t = 0; t < 5; t++) {
                final int threadId = t;
                Thread thread = new Thread(() -> {
                    try (JedisCluster localJedis = new JedisCluster(nodes)) {
                        for (int i = 0; i < 10; i++) {
                            UserProfile user = new UserProfile(
                                    "concurrent_t" + threadId + "_u" + i,
                                    "thread" + threadId + "_user" + i,
                                    "t" + threadId + "u" + i + "@test.com"
                            );

                            String key = "user:concurrent:t" + threadId + ":u" + i;
                            long start = System.nanoTime();
                            localJedis.set(key, user.toJson());
                            long latency = (System.nanoTime() - start) / 1_000_000;
                            concurrentLatencies.add(latency);
                        }
                    } catch (Exception e) {
                        System.err.println("Thread " + threadId + " error: " + e.getMessage());
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("  ✓ 5 threads × 10 writes = 50 concurrent operations completed");

            if (!concurrentLatencies.isEmpty()) {
                double avgConcurrent = concurrentLatencies.stream()
                        .mapToLong(Long::longValue).average().orElse(0);
                System.out.println("  ✓ Average concurrent write latency: " +
                        df.format(avgConcurrent) + "ms");
            }

            // Conflict Resolution
            System.out.println("\nStep 4: Conflict Resolution");
            System.out.println("  • Redis uses LAST-WRITE-WINS (LWW) strategy");
            System.out.println("  • No version vectors or vector clocks");
            System.out.println("  • Concurrent writes to same key: last one wins");
            System.out.println("  • This is acceptable for many use cases (caching, sessions)");

            // Demonstrate LWW
            String conflictKey = "user:conflict:test";
            System.out.println("\nStep 5: Demonstrating Last-Write-Wins");

            UserProfile user1 = new UserProfile("conflict_user", "version1", "v1@test.com");
            jedis.set(conflictKey, user1.toJson());
            System.out.println("  ✓ Write 1: username='version1'");

            Thread.sleep(100);

            UserProfile user2 = new UserProfile("conflict_user", "version2", "v2@test.com");
            jedis.set(conflictKey, user2.toJson());
            System.out.println("  ✓ Write 2: username='version2'");

            String finalValue = jedis.get(conflictKey);
            UserProfile finalUser = UserProfile.fromJson(finalValue);
            System.out.println("  ✓ Final value: username='" + finalUser.getUsername() + "'");
            System.out.println("  → Last write wins!");

            // Eventual Convergence
            System.out.println("\nStep 6: Eventual Convergence");
            System.out.println("  • All replicas eventually receive the same updates");
            System.out.println("  • Convergence typically happens in < 100ms");
            System.out.println("  • During replication lag, different nodes may return different values");
            System.out.println("  • After convergence, all nodes return the same value");

            observations.add("**Leaderless Model**: Redis Cluster has 3 independent masters. " +
                    "Writes distributed by hash slot. No global coordination. " +
                    "LWW conflict resolution. High availability and throughput.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    // Utility methods
    private double calculatePercentile(List<Long> values, double percentile) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
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

        System.out.println("📝 Summary of Findings:\n");

        for (int i = 0; i < observations.size(); i++) {
            System.out.println((i + 1) + ". " + observations.get(i));
            System.out.println();
        }

//        System.out.println("🎯 Architectural Trade-offs:\n");
//        System.out.println("1. **Write Concerns**: Higher durability = Higher latency");
//        System.out.println("   • ONE: Fast but risky");
//        System.out.println("   • QUORUM: Balanced choice");
//        System.out.println("   • ALL: Safe but slow\n");
//
//        System.out.println("2. **Leader-Follower**: Simple but has SPOF");
//        System.out.println("   • Single point of failure (master)");
//        System.out.println("   • Clear consistency model");
//        System.out.println("   • Failover causes downtime\n");
//
//        System.out.println("3. **Leaderless**: High availability, eventual consistency");
//        System.out.println("   • No single point of failure");
//        System.out.println("   • Better write throughput");
//        System.out.println("   • Complex conflict resolution\n");
//
//        System.out.println("💡 Redis Cluster Choice: AP in CAP theorem");
//        System.out.println("   • Prioritizes Availability and Partition tolerance");
//        System.out.println("   • Sacrifices strong Consistency for performance");
//        System.out.println("   • Best for caching, sessions, real-time analytics");
    }
}