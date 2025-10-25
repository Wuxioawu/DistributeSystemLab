package com.peng.sms.part_c;

import com.peng.sms.part_a.UserProfile;
import redis.clients.jedis.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Part C: Consistency Models
 * Demonstrates strong consistency, eventual consistency, and CAP theorem implications
 */
public class PartC_ConsistencyModels {
    
    private static final String REDIS_HOST = "localhost";
    private static final int PRIMARY_PORT = 7001;
    private static final int REPLICA1_PORT = 7005;

    private List<String> observations = new ArrayList<>();
    
    public static void main(String[] args) {
        PartC_ConsistencyModels partC = new PartC_ConsistencyModels();
        partC.run();
        partC.printObservations();
    }
    
    public void run() {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║          Part C: Consistency Models               ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");
        
        // Test 1: Strong Consistency
//        testStrongConsistency();
        
        // Test 2: Eventual Consistency
//        testEventualConsistency();

        // Test 3: CAP Theorem Demonstration
//        testCAPTheorem();

        // Test 4: Causal Consistency (using versioning)
        testCausalConsistency();
    }
    
    /**
     * Test 1: Strong Consistency
     * Using WAIT to ensure replication before confirming write
     */
    private void testStrongConsistency() {
        System.out.println("═══ Test 1: Strong Consistency ═══\n");
        
        try (Jedis primary = new Jedis(REDIS_HOST, PRIMARY_PORT);
             Jedis replica = new Jedis(REDIS_HOST, REPLICA1_PORT)) {
            
            System.out.println("Configuration: Write with WAIT ALL + Read from Replica");
            System.out.println("This simulates QUORUM/ALL read concern in other systems\n");
            
            // Create test data
            UserProfile user = new UserProfile(
                "strong_001",
                "strong_user",
                "strong@test.com"
            );
            String key = "consistency:strong:001";
            
            System.out.println("Step 1: Write to primary with WAIT for all replicas");
            long writeStart = System.nanoTime();
            primary.set(key, user.toJson());
            long replicatedTo = primary.waitReplicas(1, 2000);
            long writeTime = (System.nanoTime() - writeStart) / 1_000_000;
            
            System.out.println("  ✓ Write completed: " + writeTime + " ms");
            System.out.println("  ✓ Replicated to: " + replicatedTo + " replica(s)");
            
            System.out.println("\nStep 2: Immediately read from replica");
            long readStart = System.nanoTime();
            replica.readonly(); //
            String replicaData = replica.get(key);
            long readTime = (System.nanoTime() - readStart) / 1_000_000;
            
            if (replicaData != null) {
                UserProfile readUser = UserProfile.fromJson(replicaData);
                System.out.println("  ✓ Read completed: " + readTime + " ms");
                System.out.println("  ✓ Data: " + readUser.getUsername());
                System.out.println("  ✓ STRONGLY CONSISTENT: Data immediately visible");
            } else {
                System.out.println("  ✗ Data not found on replica");
            }
            
            // Verify consistency
            String primaryData = primary.get(key);
            boolean consistent = (replicaData != null && replicaData.equals(primaryData));
            
            System.out.println("\nConsistency Check:");
            System.out.println("  Primary == Replica: " + consistent);
            System.out.println("  Consistency Model: STRONG");
            System.out.println("  CAP Trade-off: Chose Consistency + Partition Tolerance");
            System.out.println("  Cost: Higher latency (" + writeTime + " ms vs ~1-2 ms without WAIT)");
            
            observations.add(String.format(
                "**Strong Consistency**: Using WAIT command ensures data is replicated " +
                "before acknowledging write. Latency: %.2f ms. Guarantees immediate " +
                "consistency across all nodes. CAP: CP - sacrifices availability during " +
                "partitions.",(double)writeTime));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test 2: Eventual Consistency
     * Write without waiting, demonstrate stale reads
     */
    private void testEventualConsistency() {
        System.out.println("\n═══ Test 2: Eventual Consistency ═══\n");
        
        try (Jedis primary = new Jedis(REDIS_HOST, PRIMARY_PORT);
             Jedis replica = new Jedis(REDIS_HOST, REPLICA1_PORT)) {
            
            System.out.println("Configuration: Write without WAIT + Immediate read from replica");
            System.out.println("This simulates ONE/eventual consistency in other systems\n");
            
            // Create test data
            String key = "data_for_7005";
            UserProfile user = new UserProfile(
                "eventual_001",
                "eventual_user_v1",
                "eventual@test.com"
            );
            
            System.out.println("Step 1: Write to primary WITHOUT waiting");
            long writeStart = System.nanoTime();
            primary.set(key, user.toJson());
            long writeTime = (System.nanoTime() - writeStart) / 1_000_000;
            System.out.println("  ✓ Write completed: " + writeTime + " ms (very fast!)");
            
            System.out.println("\nStep 2: IMMEDIATELY read from replica (no delay)");
            replica.readonly();
            String replicaData1 = replica.get(key);
            
            if (replicaData1 == null) {
                System.out.println("  ⚠ Data NOT YET visible on replica (stale read!)");
                System.out.println("  This demonstrates EVENTUAL consistency");
            } else {
                System.out.println("  ✓ Data visible (replication was very fast)");
            }
            
            // Demonstrate eventual convergence
            System.out.println("\nStep 3: Polling until data appears (demonstrating 'eventual')");
            int attempts = 0;
            long convergenceStart = System.currentTimeMillis();
            boolean found = false;
            
            while (attempts < 10 && !found) {
                Thread.sleep(10); // 10ms between checks
                String checkData = replica.get(key);
                attempts++;
                
                if (checkData != null) {
                    long convergenceTime = System.currentTimeMillis() - convergenceStart;
                    System.out.println("  ✓ Data appeared after " + convergenceTime + " ms");
                    System.out.println("  ✓ Number of attempts: " + attempts);
                    UserProfile readUser = UserProfile.fromJson(checkData);
                    System.out.println("  ✓ Data: " + readUser.getUsername());
                    found = true;
                }
            }
            
            if (!found) {
                System.out.println("  ⚠ Data still not visible after " + (attempts * 10) + " ms");
            }
            
            System.out.println("\nAnalysis:");
            System.out.println("  Write Latency: " + writeTime + " ms (LOW)");
            System.out.println("  Consistency Model: EVENTUAL");
            System.out.println("  CAP Trade-off: Chose Availability + Partition Tolerance");
            System.out.println("  Benefit: Very fast writes, high availability");
            System.out.println("  Cost: Temporary inconsistency, stale reads possible");
            
            System.out.println("\nWhen to use Eventual Consistency:");
            System.out.println("  ✓ Social media likes/views (exactness not critical)");
            System.out.println("  ✓ Sensor data collection (volume > precision)");
            System.out.println("  ✓ Analytics/logging (aggregated data)");
            System.out.println("  ✓ Content delivery (CDN caching)");
            System.out.println("  ✗ Financial transactions (NOT suitable)");
            System.out.println("  ✗ Inventory management (NOT suitable)");
            
            observations.add(String.format(
                "**Eventual Consistency**: Fast writes (%.2f ms) without waiting. " +
                "Brief inconsistency window (~10-50ms). CAP: AP - prioritizes availability. " +
                "Suitable for non-critical data where speed matters more than immediate consistency.",
                writeTime));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Test 3: CAP Theorem Demonstration
     * Simulate network partition and observe behavior
     */
    private void testCAPTheorem() {
        System.out.println("\n═══ Test 3: CAP Theorem Implications ═══\n");
        
        System.out.println("The CAP Theorem states you can only have 2 of 3:");
        System.out.println("  C - Consistency: All nodes see the same data");
        System.out.println("  A - Availability: System always responds");
        System.out.println("  P - Partition Tolerance: System works despite network failures\n");
        
        try (Jedis primary = new Jedis(REDIS_HOST, PRIMARY_PORT)) {
            
            System.out.println("--- Redis's CAP Profile: CP (when using WAIT) ---\n");
            
            // Test CP behavior
            System.out.println("Scenario 1: Strong Consistency (CP)");
            System.out.println("  Write with WAIT for 2 replicas...");
            
            String key = "cap:test:001";
            UserProfile user = new UserProfile("cap_001", "cap_user", "cap@test.com");
            
            try {
                long start = System.nanoTime();
                primary.set(key, user.toJson());
                long replicated = primary.waitReplicas(2, 5000);
                long latency = (System.nanoTime() - start) / 1_000_000;
                
                System.out.println("  ✓ Success: Replicated to " + replicated + " nodes (" + latency + " ms)");
                System.out.println("  Result: CONSISTENCY maintained");
                System.out.println("  Trade-off: Higher latency");
                
            } catch (Exception e) {
                System.out.println("  ✗ Failed: " + e.getMessage());
                System.out.println("  Result: AVAILABILITY sacrificed to maintain consistency");
                System.out.println("  Trade-off: System blocks/fails during partition");
            }
            
            System.out.println("\n--- Redis's CAP Profile: AP (without WAIT) ---\n");
            
            System.out.println("Scenario 2: Eventual Consistency (AP)");
            System.out.println("  Write without WAIT (fire-and-forget)...");
            
            try {
                long start = System.nanoTime();
                primary.set(key + "_ap", user.toJson());
                long latency = (System.nanoTime() - start) / 1_000_000;
                
                System.out.println("  ✓ Success: Write completed (" + latency + " ms)");
                System.out.println("  Result: HIGH AVAILABILITY maintained");
                System.out.println("  Trade-off: Temporary inconsistency possible");
                
            } catch (Exception e) {
                System.out.println("  ✗ Failed: " + e.getMessage());
            }
            
            System.out.println("\n--- Simulating Partition ---\n");
            System.out.println("To simulate a partition:");
            System.out.println("  1. Stop a replica: docker stop redis-replica1");
            System.out.println("  2. Try writing with WAIT for 2 replicas");
            System.out.println("  3. Observe: System will block/timeout (choosing C over A)");
            System.out.println();
            System.out.println("  4. Try writing WITHOUT WAIT");
            System.out.println("  5. Observe: Write succeeds (choosing A over C)");
            
            observations.add(
                "**CAP Theorem**: Redis can be configured as CP (with WAIT) or AP (without WAIT). " +
                "CP: Blocks during partitions to maintain consistency. " +
                "AP: Accepts writes during partitions but may have inconsistency. " +
                "Partition tolerance (P) is always needed in distributed systems.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Test 4: Causal Consistency (Bonus)
     * Using version numbers to track causality
     */
    private void testCausalConsistency() {
        System.out.println("\n═══ Test 4: Causal Consistency (Bonus) ═══\n");
        
        try (Jedis primary = new Jedis(REDIS_HOST, PRIMARY_PORT)) {
            
            System.out.println("Causal Consistency ensures causally-related operations");
            System.out.println("are seen in order, even if concurrent ops are not.\n");
            
            System.out.println("Simulating causal chain:");
            System.out.println("  Event A → Event B → Event C (causally related)");
            System.out.println("  Event X (concurrent, not causally related)\n");
            
            // Event A: User registration
            UserProfile user = new UserProfile("causal_001", "alice", "alice@test.com");
            String userKey = "data_for_7001";
            
            System.out.println("Event A (t=0): User registration");
            primary.set(userKey, user.toJson());
            primary.waitReplicas(1, 1000);
            System.out.println("  Version: " + user.getVersion());
            Thread.sleep(50);
            
            // Event B: User updates profile (depends on A)
            System.out.println("\nEvent B (t=50ms): User updates email (caused by A)");
            String userData = primary.get(userKey);
            user = UserProfile.fromJson(userData);
            user.setEmail("alice.new@test.com");
            primary.set(userKey, user.toJson());
            primary.waitReplicas(2, 1000);
            System.out.println("  Version: " + user.getVersion());
            Thread.sleep(50);
            
            // Event C: User logs in (depends on B)
            System.out.println("\nEvent C (t=100ms): User login (caused by B)");
            userData = primary.get(userKey);
            user = UserProfile.fromJson(userData);
            user.updateLastLogin();
            primary.set(userKey, user.toJson());
            primary.waitReplicas(2, 1000);
            System.out.println("  Version: " + user.getVersion());
            
            // Concurrent Event X: Another user's action
            System.out.println("\nEvent X (t=75ms): Different user registration (concurrent)");
            UserProfile user2 = new UserProfile("causal_002", "bob", "bob@test.com");
            primary.set("data_for_7005", user2.toJson());
            System.out.println("  Version: " + user2.getVersion());
            
            System.out.println("\nCausal Order Guarantee:");
            System.out.println("  ✓ If you see Event C, you MUST have seen Events A and B");
            System.out.println("  ✓ Version numbers help track causality: " + 
                             "A(v1) → B(v2) → C(v3)");
            System.out.println("  ? Event X may appear before, during, or after A→B→C");
            System.out.println("    (because X is not causally related)");
            
            System.out.println("\nImplementation:");
            System.out.println("  • Using version field in UserProfile");
            System.out.println("  • WAIT command ensures causal chain is preserved");
            System.out.println("  • Readers see operations in causal order");
            
            observations.add(
                "**Causal Consistency**: Operations causally related are seen in order. " +
                "Implemented using version vectors and WAIT command. Weaker than strong " +
                "consistency but stronger than eventual. Good for social apps, collaborative editing.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void printObservations() {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║           Part C: Key Observations                ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");
        
        for (int i = 0; i < observations.size(); i++) {
            System.out.println((i + 1) + ". " + observations.get(i));
            System.out.println();
        }
        
        System.out.println("\n═══ Consistency Model Comparison ═══\n");
        System.out.println("┌─────────────────┬────────────┬──────────────┬─────────────┬──────────────┐");
        System.out.println("│ Model           │ Latency    │ Availability │ Consistency │ Use Case     │");
        System.out.println("├─────────────────┼────────────┼──────────────┼─────────────┼──────────────┤");
        System.out.println("│ Strong          │ High       │ Lower        │ Immediate   │ Banking      │");
        System.out.println("│ Eventual        │ Low        │ Higher       │ Delayed     │ Social Media │");
        System.out.println("│ Causal          │ Medium     │ Medium       │ Ordered     │ Collaborative│");
        System.out.println("└─────────────────┴────────────┴──────────────┴─────────────┴──────────────┘");
    }
}