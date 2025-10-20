
Redis Cluster Architecture

          ┌───────────────┐
          │ Master 7001   │
          │ Slots 0-5460  │
          └───────┬───────┘
                  │
                  ▼
             Slave 7005
                  │
                  ▼
             (replicates 7001)

          ┌───────────────┐
          │ Master 7002   │
          │ Slots 5461-10922 │
          └───────┬───────┘
                  │
                  ▼
             Slave 7006
                  │
                  ▼
             (replicates 7002)

          ┌───────────────┐
          │ Master 7003   │
          │ Slots 10923-16383 │
          └───────┬───────┘
                  │
                  ▼
             Slave 7004
                  │
                  ▼
             (replicates 7003)
