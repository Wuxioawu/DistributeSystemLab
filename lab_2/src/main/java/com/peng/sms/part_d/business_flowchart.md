# Distributed Transaction Business Flowcharts (English Version)

## Flowchart Symbols Legend

```
┌─────────┐
│Rectangle│  = Process/Operation
└─────────┘

◇ Diamond = Decision Point

⬭ Rounded Rectangle = Start/End

║ Document Symbol = Data/Document

⬡ Hexagon = Preparation/Initialize

→ Arrow = Flow Direction

// Comment text
```

---

## Flowchart 1: ACID Transaction (Two-Phase Commit)

### Main Flow

```
        ⬭ Start
         │
         ↓
    ┌─────────────┐
    │Customer     │
    │Places Order │
    └──────┬──────┘
           ↓
    ⬡ Initialize Distributed Transaction
           │
           ↓
    ┌──────────────┐
    │Coordinator   │
    │Starts        │
    └──────┬───────┘
           │
           ↓
╔════════════════════════════╗
║   Phase 1: PREPARE         ║
╚════════════════════════════╝
           │
           ├──────────┬──────────┬──────────┐
           ↓          ↓          ↓          ↓
    ┌───────────┐ ┌──────────┐ ┌───────────┐
    │Order      │ │Payment   │ │Inventory  │
    │Service    │ │Service   │ │Service    │
    │Prepare TX │ │Prepare TX│ │Prepare TX │
    └─────┬─────┘ └────┬─────┘ └─────┬─────┘
          │            │              │
          │  🔒Lock    │  🔒Lock     │  🔒Lock
          │  Resources │  Resources  │  Resources
          │            │              │
          ↓            ↓              ↓
    ┌───────────┐ ┌──────────┐ ┌───────────┐
    │Check      │ │Check     │ │Check      │
    │Feasibility│ │Balance   │ │Stock      │
    └─────┬─────┘ └────┬─────┘ └─────┬─────┘
          │            │              │
          ↓            ↓              ↓
         ◇            ◇              ◇
        /OK?\        /OK?\          /OK?\
       /    \       /    \         /    \
     YES    NO    YES    NO      YES    NO
      │      │     │      │       │      │
      └──────┴─────┴──────┴───────┴──────┘
                    │
            Return READY / ABORT
                    │
                    ↓
             ◇ Coordinator Decides
            / All READY? \
           /             \
         YES              NO
          │               │
          ↓               ↓
    ╔═══════════╗   ╔═══════════╗
    ║  Phase 2  ║   ║  Phase 2  ║
    ║  COMMIT   ║   ║  ABORT    ║
    ╚═══════════╝   ╚═══════════╝
          │               │
          ↓               ↓
    ┌──────────┐    ┌──────────┐
    │Send      │    │Send      │
    │COMMIT to │    │ABORT to  │
    │All       │    │All       │
    └────┬─────┘    └────┬─────┘
         │               │
         ↓               ↓
    ┌──────────┐    ┌──────────┐
    │Services  │    │Services  │
    │Commit    │    │Rollback  │
    │Release   │    │Release   │
    │Locks     │    │Locks     │
    └────┬─────┘    └────┬─────┘
         │               │
         ↓               ↓
      ⬭ Success      ⬭ Failed
```

### Problem Scenario Flow

```
    ⬭ Start Transaction
         │
         ↓
    ┌─────────────┐
    │Phase 1      │
    │In Progress..│
    └──────┬──────┘
           │
           ↓
       ◇ Coordinator Status
      / Normal? \
     /           \
   YES            NO
    │             │
    │             ↓
    │      ┌──────────────┐
    │      │Coordinator   │
    │      │Crashes       │ ⚠️
    │      └──────┬───────┘
    │             │
    │             ↓
    │      ┌──────────────┐
    │      │All Services  │
    │      │Hold Locks 🔒 │
    │      └──────┬───────┘
    │             │
    │             ↓
    │      ┌──────────────┐
    │      │Cannot Release│
    │      │Stuck Waiting │ ❌
    │      └──────┬───────┘
    │             │
    │             ↓
    │      ⬭ System Blocked
    │
    ↓
  (Continue Normal Flow)
```

---

## Flowchart 2: Saga Orchestration Pattern

### Success Path Flowchart

```
        ⬭ Customer Places Order
                  │
                  ↓
           ┌──────────────┐
           │Saga          │
           │Orchestrator  │
           │Starts TX     │
           └──────┬───────┘
                  │
                  ↓
           ┌──────────────┐
           │  Step 1:     │
           │Create Order  │
           └──────┬───────┘
                  │
                  ↓
           ┌──────────────┐
           │OrderService  │
           │Execute Local │
           │Transaction   │
           └──────┬───────┘
                  │
                  ↓
              ◇ Success?
             /           \
           YES            NO ──────┐
            │                      │
            ↓                      │
       ║ Save State ║              │
        orderId: 123               │
            │                      │
            ↓                      │
       ┌──────────────┐            │
       │  Step 2:     │            │
       │Process       │            │
       │Payment       │            │
       └──────┬───────┘            │
              │                    │
              ↓                    │
       ┌──────────────┐            │
       │PaymentService│            │
       │Execute Local │            │
       │Transaction   │            │
       └──────┬───────┘            │
              │                    │
              ↓                    │
          ◇ Success?               │
         /           \             │
       YES            NO ──────┐   │
        │                      │   │
        ↓                      │   │
   ║ Save State ║              │   │
    paymentId: P456            │   │
        │                      │   │
        ↓                      │   │
   ┌──────────────┐            │   │
   │  Step 3:     │            │   │
   │Reserve       │            │   │
   │Inventory     │            │   │
   └──────┬───────┘            │   │
          │                    │   │
          ↓                    │   │
   ┌──────────────┐            │   │
   │InventoryService│          │   │
   │Execute Local │            │   │
   │Transaction   │            │   │
   └──────┬───────┘            │   │
          │                    │   │
          ↓                    │   │
      ◇ Success?               │   │
     /           \             │   │
   YES            NO           │   │
    │             │            │   │
    ↓             ↓            ↓   ↓
║ Save State ║   ╔═════════════════╗
reservationId: R789 ║Trigger          ║
    │              ║Compensation     ║
    ↓              ╚═════════════════╝
┌──────────────┐           │
│  Step 4:     │           │
│Confirm Order │           │
└──────┬───────┘           │
       │                   │
       ↓                   │
┌──────────────┐           │
│OrderService  │           │
│Update Status │           │
│= CONFIRMED   │           │
└──────┬───────┘           │
       │                   │
       ↓                   │
   ⬭ Order Success         │
                           │
                           ↓
                    (Jump to Compensation)
```

### Compensation Flow (Payment Failed)

```
    ⬭ Payment Failed
         │
         ↓
    ┌──────────────┐
    │Detect Failure│
    │paymentId=null│
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │Orchestrator  │
    │Trigger       │
    │Compensation  │
    └──────┬───────┘
           │
           ↓
╔══════════════════════════╗
║Start Compensation (Reverse)║
╚══════════════════════════╝
           │
           ↓
    ┌──────────────────┐
    │Compensate Step 1:│
    │Cancel Order      │
    └─────────┬────────┘
              │
              ↓
    ┌──────────────┐
    │OrderService  │
    │CancelOrder() │
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │Update Order  │
    │Status =      │
    │CANCELLED     │
    └──────┬───────┘
           │
           ↓
       ◇ Compensation Success?
      /                    \
    YES                     NO
     │                      │
     ↓                      ↓
║ Record ║           ┌──────────┐
 Status:             │Record    │
 Cancelled           │Failure   │
     │               │Manual    │⚠️
     │               │Intervention│
     │               └────┬─────┘
     │                    │
     ↓                    ↓
 ⬭ Compensation      ⬭ Needs
    Complete            Handling
```

### Compensation Flow (Inventory Failed - Multi-Step)

```
    ⬭ Inventory Failed
         │
         ↓
    ┌──────────────┐
    │Executed Steps:│
    │✓ Order Created│
    │✓ Payment Done│
    │✗ Inventory   │
    │  Failed      │
    └──────┬───────┘
           │
           ↓
╔═══════════════════════════╗
║Start Compensation (Reverse)║
╚═══════════════════════════╝
           │
           ↓
    ┌──────────────────┐
    │Compensate Step 1:│
    │Refund Payment    │
    └─────────┬────────┘
              │
              ↓
    ┌──────────────┐
    │PaymentService│
    │RefundPayment()│
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │Execute Refund│
    │paymentId:P456│
    │amount: $50   │
    └──────┬───────┘
           │
           ↓
       ◇ Refund Success?
      /              \
    YES               NO
     │                │
     ↓                ↓
║ Record: ║    ┌──────────┐
 Refund            │Refund    │⚠️
 Success           │Failed    │
     │             │Trigger   │
     │             │Retry     │
     │             └────┬─────┘
     │                  │
     │                  ↓
     │             ◇ Retry Success?
     │            /              \
     │          YES               NO
     │           │                │
     │           ↓                ↓
     │       (Continue)    ┌──────────┐
     │                     │Mark as   │
     │                     │Manual    │
     │                     │Handling  │
     │                     └────┬─────┘
     │                          │
     ├──────────────────────────┘
     │
     ↓
┌──────────────────┐
│Compensate Step 2:│
│Cancel Order      │
└─────────┬────────┘
          │
          ↓
    ┌──────────────┐
    │OrderService  │
    │CancelOrder() │
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │Update Status │
    │= CANCELLED   │
    │Reason:       │
    │Out of Stock  │
    └──────┬───────┘
           │
           ↓
       ◇ Cancel Success?
      /                \
    YES                 NO
     │                  │
     ↓                  ↓
║ Compensation ║  ⚠️ Data Inconsistency
   Complete           Manual Intervention
     │                   Required
     ↓
 ⬭ Order Failed
  (Compensated)
```

---

## Flowchart 3: Saga Choreography Pattern (Event-Driven)

### Success Path Flowchart

```
        ⬭ Customer Places Order
                  │
                  ↓
           ┌──────────────┐
           │ OrderService │
           │ Receive      │
           │ Request      │
           └──────┬───────┘
                  │
                  ↓
           ┌──────────────┐
           │ Create Local │
           │ Order        │
           │Status=PENDING│
           └──────┬───────┘
                  │
                  ↓
           ┌──────────────┐
           │ Publish Event│
           │OrderCreated  │
           └──────┬───────┘
                  │
                  ↓
           ║ Event Bus ║
                  │
          ┌───────┴───────┐
          │               │
          ↓               ↓
    ┌──────────┐   ┌───────────┐
    │Payment   │   │Inventory  │
    │Service   │   │Service    │
    │Listens   │   │Listens    │
    └────┬─────┘   └─────┬─────┘
         │               │
         │(Executes First)│
         ↓               │
    ┌──────────────┐     │
    │PaymentService│     │
    │Process       │     │
    │Payment       │     │
    └──────┬───────┘     │
           │             │
           ↓             │
       ◇ Payment Success?│
      /                \ │
    YES                NO│
     │                 │ │
     ↓                 │ │
┌──────────────┐       │ │
│Publish Event │       │ │
│Payment       │       │ │
│Processed     │       │ │
└──────┬───────┘       │ │
       │               │ │
       ↓               │ │
  ║ Event Bus ║        │ │
       │               │ │
       └───────────────┼─┘
                       │
          ┌────────────┴────────┐
          │                     │
          ↓                     ↓
    ┌──────────┐         ┌──────────┐
    │Inventory │         │Payment   │
    │Service   │         │Failed    │
    │Listens   │         │Event     │
    └────┬─────┘         └────┬─────┘
         │                    │
         ↓                    ↓
┌──────────────┐      (Jump to Failure Flow)
│Reserve       │
│Inventory     │
└──────┬───────┘
       │
       ↓
   ◇ Reserve Success?
  /                 \
YES                  NO
 │                   │
 ↓                   ↓
┌──────────────┐  (Jump to Failure Flow)
│Publish Event │
│Inventory     │
│Reserved      │
└──────┬───────┘
       │
       ↓
  ║ Event Bus ║
       │
       ↓
┌──────────────┐
│ OrderService │
│ Listens      │
└──────┬───────┘
       │
       ↓
┌──────────────┐
│ Update Status│
│ = COMPLETED  │
└──────┬───────┘
       │
       ↓
┌──────────────┐
│Publish Event │
│Order         │
│Completed     │
└──────┬───────┘
       │
       ↓
   ⬭ Order Success
```

### Failure Compensation Flow (Payment Failed)

```
    ⬭ Payment Failed Point
         │
         ↓
    ┌──────────────┐
    │PaymentService│
    │Payment Failed│
    │Reason:       │
    │Insufficient  │
    │Funds         │
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │ Publish Event│
    │PaymentFailed │
    │{orderId,     │
    │ reason}      │
    └──────┬───────┘
           │
           ↓
    ║ Event Bus ║
           │
           ↓
    ┌──────────────┐
    │ OrderService │
    │ Listen to    │
    │ Failed Event │
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │ Execute      │
    │ Compensation │
    │ Cancel Order │
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │ Update Local │
    │ Status =     │
    │ CANCELLED    │
    │ Reason:      │
    │ Payment      │
    │ Failed       │
    └──────┬───────┘
           │
           ↓
    ┌──────────────┐
    │ Publish Event│
    │Order         │
    │Cancelled     │
    └──────┬───────┘
           │
           ↓
    ║ Event Bus ║
           │
           ↓
    ┌──────────────┐
    │ Other        │
    │ Services     │
    │ May Listen   │
    │(Notification)│
    └──────────────┘
           │
           ↓
   ⬭ Compensation Complete
```

### Failure Compensation Flow (Inventory Failed - Cascading)

```
    ⬭ Inventory Failed Point
         │
         ↓
    ┌────────────────┐
    │InventoryService│
    │Stock Check     │
    │Failed          │
    │Reason: Out of  │
    │Stock           │
    └──────┬─────────┘
           │
           ↓
    ┌──────────────┐
    │ Publish Event│
    │Inventory     │
    │Unavailable   │
    └──────┬───────┘
           │
           ↓
    ║ Event Bus ║
           │
     ┌─────┴─────┐
     │           │
     ↓           ↓
┌──────────┐ ┌──────────┐
│Payment   │ │Order     │
│Service   │ │Service   │
│Listens   │ │Listens   │
└────┬─────┘ └────┬─────┘
     │            │
     ↓            │
┌──────────────┐  │
│PaymentService│  │
│Trigger       │  │
│Compensation  │  │
│Execute Refund│  │
└──────┬───────┘  │
       │          │
       ↓          │
┌──────────────┐  │
│RefundPayment │  │
│paymentId:P456│  │
│amount: $50   │  │
└──────┬───────┘  │
       │          │
       ↓          │
   ◇ Refund Success?│
  /              \  │
YES              NO │
 │               │  │
 ↓               ↓  │
┌──────────────┐ ┌──────────┐
│Publish Event │ │Mark      │⚠️
│Payment       │ │Failed    │
│Refunded      │ │Manual    │
└──────┬───────┘ │Intervention│
       │         └────┬─────┘
       ↓              │
  ║ Event Bus ║       ↓
       │          ⬭ Needs
       ↓             Handling
┌──────────────┐
│ OrderService │
│ Listens      │
└──────┬───────┘
       │
       ↓
┌──────────────┐
│Execute       │
│Compensation  │
│Cancel Order  │
└──────┬───────┘
       │
       ↓
┌──────────────┐
│Update Local  │
│Status =      │
│CANCELLED     │
│Reason:       │
│Out of Stock  │
└──────┬───────┘
       │
       ↓
┌──────────────┐
│Publish Event │
│Order         │
│Cancelled     │
└──────┬───────┘
       │
       ↓
  ║ Event Bus ║
       │
       ↓
   ⬭ Compensation Complete
```

---

## Flowchart 4: Decision Flow (Choosing Pattern)

```
        ⬭ Start Designing Distributed Transaction
                        │
                        ↓
                 ┌──────────────┐
                 │ Analyze      │
                 │ Requirements │
                 └──────┬───────┘
                        │
                        ↓
                 ◇ Microservices Architecture?
                /                           \
              NO                            YES
               │                             │
               ↓                             ↓
         ┌──────────┐               ◇ Strong Consistency Required?
         │Use       │              /                            \
         │Traditional│            YES                            NO
         │ACID TX   │             │                              │
         └────┬─────┘             ↓                              ↓
              │            ┌──────────────┐              ◇ Number of Services?
              │            │❌ Not        │             /                    \
              │            │Suitable for  │          Few(2-5)            Many(5+)
              │            │Microservices │            │                    │
              │            │Reconsider    │            ↓                    ↓
              │            │Requirements  │     ┌──────────┐        ┌──────────┐
              │            └──────────────┘     │  Saga    │        │  Saga    │
              │                                 │Orchestra-│        │Choreo-   │
              │                                 │tion      │        │graphy    │
              │                                 └────┬─────┘        └────┬─────┘
              │                                      │                   │
              │                                      ↓                   ↓
              │                            ┌───────────────┐   ┌───────────────┐
              │                            │Pros:          │   │Pros:          │
              │                            │- Easy debug   │   │- High         │
              │                            │- Clear flow   │   │  availability │
              │                            │- Central      │   │- Scalable     │
              │                            │  control      │   │- Loose        │
              │                            │               │   │  coupling     │
              │                            │Cons:          │   │               │
              │                            │- Orchestrator │   │Cons:          │
              │                            │  SPOF         │   │- Hard to      │
              │                            │- Medium       │   │  debug        │
              │                            │  complexity   │   │- High         │
              │                            │               │   │  complexity   │
              │                            └───────┬───────┘   └───────┬───────┘
              │                                    │                   │
              └────────────────────────────────────┴───────────────────┘
                                                   │
                                                   ↓
                                            ┌──────────────┐
                                            │ Implement    │
                                            │ Selected     │
                                            │ Approach     │
                                            └──────┬───────┘
                                                   │
                                                   ↓
                                               ⬭ Complete
```

---

## Flowchart 5: Exception Handling Flow

```
        ⬭ Transaction Executing
                │
                ↓
         ┌──────────────┐
         │ Execute      │
         │ Business Step│
         └──────┬───────┘
                │
                ↓
            ◇ Exception Occurred?
           /                      \
         NO                       YES
          │                        │
          ↓                        ↓
    ┌──────────┐           ┌──────────────┐
    │Continue  │           │ Classify     │
    │Next Step │           │ Exception    │
    └────┬─────┘           │ Type         │
         │                 └──────┬───────┘
         ↓                        │
         │               ┌────────┼────────┐
         │               ↓        ↓        ↓
         │         ┌──────────┐ ┌───────┐ ┌───────┐
         │         │Business  │ │Tech   │ │Timeout│
         │         │Exception │ │Error  │ │Error  │
         │         │(e.g. Out │ │       │ │       │
         │         │of Stock) │ │       │ │       │
         │         └────┬─────┘ └───┬───┘ └───┬───┘
         │              │           │         │
         │              ↓           ↓         ↓
         │         ┌──────────┐ ┌───────┐ ┌───────┐
         │         │Trigger   │ │Retry? │ │Retry? │
         │         │Compen-   │ │       │ │       │
         │         │sation    │ │       │ │       │
         │         └────┬─────┘ └───┬───┘ └───┬───┘
         │              │           │         │
         │              ↓           ↓         ↓
         │         ╔═══════════╗  ◇        ◇
         │         ║Compensation║  /Retry\ /Retry\
         │         ║Transaction ║  Success? Success?
         │         ║Flow       ║  │  │    │  │
         │         ╚═══════════╝ YES NO  YES NO
         │              │         │  │    │  │
         │              ↓         ↓  ↓    ↓  ↓
         │         ┌──────────┐(Continue)(Compensate)(Continue)(Compensate)
         │         │Execute   │
         │         │Compen-   │
         │         │sations   │
         │         │in Reverse│
         │         └────┬─────┘
         │              │
         │              ↓
         │          ◇ Compensation Success?
         │         /                        \
         │       YES                         NO
         │        │                          │
         │        ↓                          ↓
         │   ┌──────────┐            ┌──────────┐
         │   │Record    │            │Mark for  │
         │   │State     │            │Manual    │⚠️
         │   │Compen-   │            │Intervention│
         │   │sated     │            └────┬─────┘
         │   └────┬─────┘                 │
         │        │                       ↓
         │        ↓                   ⬭ Needs
         │    ⬭ Failed                  Manual
         │   (Compensated)               Handling
         │
         ↓
    ◇ All Steps Completed?
   /                        \
 YES                         NO
  │                          │
  ↓                          │
⬭ Transaction Success        │
                             └─(Return to Execute)
```

---

## Flowchart 6: Monitoring and Recovery Flow

```
        ⬭ System Running
                │
                ↓
         ┌──────────────┐
         │ Monitor      │
         │ Transaction  │
         │ Status       │
         │(Continuous)  │
         └──────┬───────┘
                │
                ↓
            ◇ Anomaly Detected?
           /                    \
         NO                     YES
          │                      │
          ↓                      ↓
    ┌──────────┐          ┌──────────────┐
    │Continue  │          │ Log Exception│
    │Monitoring│          │ Information  │
    │(Polling) │          └──────┬───────┘
    └────┬─────┘                 │
         │                       ↓
         │                ┌──────────────┐
         │                │ Analyze      │
         │                │ Exception    │
         │                │ Type         │
         │                └──────┬───────┘
         │                       │
         │              ┌────────┼────────┐
         │              ↓        ↓        ↓
         │        ┌──────────┐ ┌──────┐ ┌──────┐
         │        │Incomplete│ │TX    │ │Data  │
         │        │Compen-   │ │Timeout│Incon- │
         │        │sation    │ │      │sistency│
         │        └────┬─────┘ └──┬───┘ └──┬───┘
         │             │          │        │
         │             ↓          ↓        ↓
         │      ┌──────────┐ ┌────────┐ ┌────────┐
         │      │Resume    │ │Retry   │ │Invoke  │
         │      │Compen-   │ │TX      │ │Recon-  │
         │      │sation    │ │        │ │ciliation│
         │      └────┬─────┘ └───┬────┘ └───┬────┘
         │           │           │          │
         │           ↓           ↓          ↓
         │      ┌──────────┐ ┌────────┐ ┌────────┐
         │      │Execute   │ │Re-     │ │Compare │
         │      │Remaining │ │execute │ │States  │
         │      │Steps     │ │Failed  │ │Across  │
         │      │          │ │Step    │ │Services│
         │      └────┬─────┘ └───┬────┘ └───┬────┘
         │           │           │          │
         │           ↓           ↓          ↓
         │       ◇ Success?  ◇ Success? ◇ Match?
         │      /         \ /         \/        \
         │    YES         NO YES      NO YES     NO
         │     │          │  │        │  │       │
         │     ↓          ↓  ↓        ↓  ↓       ↓
         │ ┌────────┐ ┌─────┐ ┌────┐ ┌──┐ ┌───┐ ┌───┐
         │ │Log     │ │Alert│ │Log ││Alert│Log││Alert│
         │ │Success │ │Ops  │⚠️│OK  ││Ops │⚠️│OK││Ops│⚠️
         │ └───┬────┘ └──┬──┘ └─┬──┘└─┬─┘└─┬─┘└─┬─┘
         │     │         │      │    │   │   │
         │     └─────────┴──────┴────┴───┴───┘
         │                      │
         └──────────────────────┘
                                │
                                ↓
                         ┌──────────────┐
                         │ Update       │
                         │ Monitoring   │
                         │ Dashboard    │
                         └──────┬───────┘
                                │
                                ↓
                            ⬭ Recovery
                             Attempted
```

---

## Flowchart 7: Comparison Matrix (Table Format)

```
┌─────────────────┬──────────────┬──────────────┬──────────────┐
│   Feature       │ ACID (2PC)   │   Saga       │   Saga       │
│                 │              │Orchestration │Choreography  │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Coordination    │ Centralized  │ Centralized  │ Decentralized│
│ Method          │ Coordinator  │ Orchestrator │ Event-Driven │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Consistency     │ Strong       │ Eventual     │ Eventual     │
│                 │ (ACID)       │              │              │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Availability    │ ❌ Low        │ ✅ High       │ ✅ Very High  │
│                 │ (Blocking)   │              │              │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Complexity      │ Low          │ Medium       │ High         │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Single Point    │ ❌ Yes        │ ⚠️  Yes       │ ✅ No         │
│ of Failure      │ (Coordinator)│(Orchestrator)│              │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Performance     │ ❌ Poor       │ ✅ Good       │ ✅ Excellent  │
│                 │ (Lock wait)  │              │              │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Debugging       │ Easy         │ Medium       │ ❌ Hard       │
│ Difficulty      │              │              │ (Distributed)│
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Transaction     │ Automatic    │ Compensating │ Compensating │
│ Rollback        │ Rollback     │ Transaction  │ Transaction  │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Use Case        │ Monolithic/  │ Medium-scale │ Large-scale  │
│                 │ Small-scale  │ Microservices│ Microservices│
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Coupling        │ Tight        │ Medium       │ Loose        │
├─────────────────┼──────────────┼──────────────┼──────────────┤
│ Scalability     │ ❌ Limited    │ ✅ Good       │ ✅ Excellent  │
└─────────────────┴──────────────┴──────────────┴──────────────┘
```

---

## Flowchart 8: CAP Theorem Triangle

```
                    Consistency (C)
                          △
                         ╱│╲
                        ╱ │ ╲
                       ╱  │  ╲
                      ╱   │   ╲
                     ╱    │    ╲
                    ╱ ACID(2PC)╲
                   ╱     (CP)   ╲
                  ╱      │       ╲
                 ╱       │        ╲
                ╱────────┼─────────╲
               △         │          △
         Availability    │      Partition
              (A)        │      Tolerance (P)
                         │
                    Saga Pattern
                    (AP System)

Legend:
━━━━  Strong preference
╌╌╌╌  Weak/Sacrificed

ACID 2PC Position:
• Chooses: Consistency + Partition Tolerance (CP)
• Sacrifices: Availability (blocks during partitions)

Saga Position:
• Chooses: Availability + Partition Tolerance (AP)
• Sacrifices: Strong Consistency (eventual only)
```

---

## Flowchart 9: Use Case Recommendation Matrix

```
                    High Consistency Required
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   High │    ⚠️ Difficult    │    ✅ Saga        │
   Availability│    Tradeoff      │  Orchestration│
   Required │  (Reconsider)    │   (Recommended)│
        │                   │                   │
        ├───────────────────┼───────────────────┤
        │                   │                   │
   Low  │   ❌ ACID (2PC)    │   ✅ Simple       │
   Availability│   (Not          │   Async        │
   Required │   Recommended)   │   (Good enough)│
        │                   │                   │
        └───────────────────┴───────────────────┘
                    Low Consistency Required


Alternative View - Service Count Based:

        ┌───────────────────┬───────────────────┐
        │  Few Services     │  Many Services    │
        │  (2-5 services)   │  (5+ services)    │
        ├───────────────────┼───────────────────┤
  High  │  ✅ Saga          │  ✅ Saga          │
  Cons- │  Orchestration    │  Choreography     │
  isten-│                   │                   │
  cy    │  - Clear workflow │  - Scalable       │
        │  - Easy debug     │  - Loose coupling │
        ├───────────────────┼───────────────────┤
  Low   │  ✅ Simple Async  │  ✅ Event-Driven  │
  Cons- │                   │                   │
  isten-│  - Minimal        │  - Highly         │
  cy    │    complexity     │    distributed    │
        └───────────────────┴───────────────────┘
```

---

## Flowchart 10: Real-World Examples

```
        ⬭ Choose Pattern Based on Domain

                    │
        ┌───────────┼───────────┐
        │           │           │
        ↓           ↓           ↓
┌────────────┐ ┌──────────┐ ┌──────────┐
│  ACID 2PC  │ │  Saga    │ │  Saga    │
│  (Avoid in │ │Orchestra-│ │Choreo-   │
│  Micro-    │ │  tion    │ │  graphy  │
│  services) │ │          │ │          │
└─────┬──────┘ └────┬─────┘ └────┬─────┘
      │             │             │
      ↓             ↓             ↓
┌──────────┐  ┌──────────┐  ┌──────────┐
│Only Use: │  │Examples: │  │Examples: │
│          │  │          │  │          │
│• Single  │  │• E-commerce│ │• Netflix │
│  Database│  │  Order    │  │  Video   │
│  TX      │  │  System   │  │  Pipeline│
│          │  │           │  │          │
│• Monolith│  │• Travel   │  │• Large   │
│  Internal│  │  Booking  │  │  Logistics│
│  TX      │  │  (Flight+ │  │  System  │
│          │  │  Hotel)   │  │          │
│• NOT for │  │           │  │• IoT Data│
│  Distri- │  │• Financial│  │  Proces- │
│  buted   │  │  Transfer │  │  sing    │
│  Systems │  │  with     │  │          │
│          │  │  Audit    │  │• Social  │
│          │  │  Trail    │  │  Media   │
│          │  │           │  │  Platforms│
└──────────┘  └──────────┘  └──────────┘
```

---

## Flowchart 11: Key Insights Summary

### Why ACID Doesn't Work for Microservices

```
        ⬭ ACID Transaction Starts
                │
                ↓
         ┌──────────────┐
         │ Service A    │
         │ Locks        │
         │ Resources 🔒 │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ Coordinator  │
         │ Coordinates  │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ Service B    │
         │ Locks        │
         │ Resources 🔒 │
         └──────┬───────┘
                │
                ↓
            ◇ Coordinator Crashes?
           /                       \
         NO                        YES
          │                         │
          ↓                         ↓
    ┌──────────┐            ┌──────────────┐
    │Continue  │            │ ❌ PROBLEM:   │
    │Normal    │            │               │
    │Flow      │            │• Services     │
    └──────────┘            │  blocked      │
                            │• Resources    │
                            │  locked       │
                            │• Cannot serve │
                            │  other requests│
                            │• System down  │
                            └──────────────┘
                                    │
                                    ↓
                              ⬭ Unacceptable
                               in Production
```

### How Saga Solves This

```
        ⬭ Saga Transaction Starts
                │
                ↓
         ┌──────────────┐
         │ Service A    │
         │ Local TX     │
         │ Commits      │
         │ Immediately  │
         └──────┬───────┘
                │
                ↓ ✅ No locks held
                │
         ┌──────────────┐
         │ Orchestrator │
         │ (or Event)   │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ Service B    │
         │ Local TX     │
         │ Commits      │
         │ Immediately  │
         └──────┬───────┘
                │
                ↓ ✅ No locks held
                │
            ◇ Step Fails?
           /              \
         NO               YES
          │                │
          ↓                ↓
    ┌──────────┐    ┌──────────────┐
    │Continue  │    │ ✅ SOLUTION:  │
    │to Next   │    │               │
    │Step      │    │• Execute      │
    └──────────┘    │  compensating │
                    │  transactions │
                    │• No resources │
                    │  locked       │
                    │• Services     │
                    │  available    │
                    │• Graceful     │
                    │  degradation  │
                    └──────────────┘
                            │
                            ↓
                      ⬭ Acceptable
                       in Production
```

---

## Flowchart 12: Compensating Transaction Design

```
        ⬭ Design Forward Transaction
                │
                ↓
         ┌──────────────┐
         │ Example:     │
         │ CreateOrder()│
         │              │
         │ Action:      │
         │ • Insert     │
         │   order      │
         │   record     │
         │ • Status=    │
         │   PENDING    │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ Design       │
         │ Compensating │
         │ Transaction  │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ ⚠️ NOT:      │
         │ DELETE order │
         │ (Technical   │
         │  rollback)   │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ ✅ INSTEAD:  │
         │ CancelOrder()│
         │              │
         │ Action:      │
         │ • UPDATE     │
         │   order      │
         │ • Status=    │
         │   CANCELLED  │
         │ • Reason=    │
         │   [why]      │
         └──────┬───────┘
                │
                ↓
         ┌──────────────┐
         │ Why?         │
         │              │
         │ • Maintains  │
         │   audit trail│
         │ • Preserve   │
         │   history    │
         │ • Business   │
         │   semantics  │
         │ • Compliance │
         └──────────────┘
                │
                ↓
         ┌──────────────┐
         │ Key Principle│
         │              │
         │ Compensation │
         │ = Business   │
         │   Reversal   │
         │              │
         │ NOT          │
         │ = Technical  │
         │   Undo       │
         └──────────────┘
                │
                ↓
            ⬭ Design Complete
```

---

## Flowchart 13: Trade-offs Summary

```
                    ⬭ Distributed Transaction Design
                              │
                              ↓
                      ◇ Choose Your Priority
                     /         │         \
                    /          │          \
                   /           │           \
                  ↓            ↓            ↓
        ┌────────────┐  ┌──────────┐  ┌──────────┐
        │Consistency │  │Availability│ │Simplicity│
        │First       │  │First      │  │First     │
        └─────┬──────┘  └─────┬────┘  └─────┬────┘
              │               │              │
              ↓               ↓              ↓
        ┌────────────┐  ┌──────────┐  ┌──────────┐
        │Use:        │  │Use:      │  │Use:      │
        │Saga        │  │Saga      │  │Async     │
        │Orchestra-  │  │Choreo-   │  │Messaging │
        │tion        │  │graphy    │  │          │
        └─────┬──────┘  └─────┬────┘  └─────┬────┘
              │               │              │
              ↓               ↓              ↓
        ┌────────────┐  ┌──────────┐  ┌──────────┐
        │Tradeoff:   │  │Tradeoff: │  │Tradeoff: │
        │            │  │          │  │          │
        │+ Clear     │  │+ No SPOF │  │+ Easy    │
        │  workflow  │  │+ Highly  │  │  to      │
        │+ Audit     │  │  scalable│  │  implement│
        │  trail     │  │          │  │          │
        │            │  │          │  │          │
        │- Medium    │  │- Complex │  │- No      │
        │  complexity│  │  debug   │  │  trans-  │
        │- Orchestra-│  │- No      │  │  action  │
        │  tor SPOF  │  │  central │  │  guarantee│
        │            │  │  view    │  │          │
        └────────────┘  └──────────┘  └──────────┘
              │               │              │
              └───────────────┴──────────────┘
                              │
                              ↓
                      ⬭ Implementation Starts
```

---

## Summary Flow: Complete Transaction Lifecycle

```
⬭ START: Business Transaction Request
    │
    ↓
┌───────────────────────────────────────┐
│ Phase 1: Transaction Initiation       │
│                                       │
│ • Receive request                     │
│ • Validate input                      │
│ • Generate transaction ID             │
└─────────────┬─────────────────────────┘
              │
              ↓
┌───────────────────────────────────────┐
│ Phase 2: Execute Steps                │
│                                       │
│ For each service:                     │
│   ◇ Execute local transaction         │
│  /                          \         │
│ SUCCESS                    FAILURE    │
│  │                          │         │
│  • Commit locally          • Trigger │
│  • Record state            compensation│
│  • Continue                • Stop     │
│                            forward    │
└─────────────┬─────────────────────────┘
              │
              ↓
          ◇ All Steps Successful?
         /                      \
       YES                       NO
        │                        │
        ↓                        ↓
┌─────────────────┐    ┌─────────────────┐
│ Phase 3A:       │    │ Phase 3B:       │
│ Success Path    │    │ Compensation    │
│                 │    │ Path            │
│ • Mark complete │    │                 │
│ • Send          │    │ • Execute       │
│   confirmation  │    │   compensations │
│ • Update        │    │   (reverse)     │
│   all states    │    │ • Mark failed   │
│ • Notify user   │    │ • Notify user   │
└────────┬────────┘    └────────┬────────┘
         │                      │
         │                      │
         └──────────┬───────────┘
                    │
                    ↓
┌───────────────────────────────────────┐
│ Phase 4: Monitoring & Recovery        │
│                                       │
│ • Log transaction outcome             │
│ • Update metrics                      │
│ • Check for anomalies                 │
│ • Trigger alerts if needed            │
└─────────────┬─────────────────────────┘
              │
              ↓
        ⬭ END: Transaction Complete
```

---

## Quick Reference Card

```
╔═══════════════════════════════════════════════════╗
║      DISTRIBUTED TRANSACTION PATTERNS             ║
║              QUICK REFERENCE                      ║
╚═══════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────┐
│ When to Use What?                               │
├─────────────────────────────────────────────────┤
│                                                 │
│ Single Database/Monolith:                      │
│   ✅ Traditional ACID transactions              │
│                                                 │
│ 2-5 Microservices + Need Audit:                │
│   ✅ Saga Orchestration                         │
│                                                 │
│ 5+ Microservices + High Scale:                 │
│   ✅ Saga Choreography                          │
│                                                 │
│ Distributed Microservices + ACID:              │
│   ❌ Not recommended (2PC has issues)           │
│   💡 Reconsider requirements                    │
│                                                 │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ Key Design Principles                           │
├─────────────────────────────────────────────────┤
│                                                 │
│ 1. Design compensations upfront                │
│ 2. Make operations idempotent                  │
│ 3. Use timeouts everywhere                     │
│ 4. Log everything for debugging                │
│ 5. Monitor transaction states                  │
│ 6. Plan for partial failures                   │
│ 7. Test failure scenarios                      │
│                                                 │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ Common Pitfalls to Avoid                        │
├─────────────────────────────────────────────────┤
│                                                 │
│ ❌ Using DELETE for compensation               │
│    ✅ Use UPDATE with status change            │
│                                                 │
│ ❌ No idempotency                              │
│    ✅ Use transaction IDs                      │
│                                                 │
│ ❌ No timeout handling                         │
│    ✅ Set explicit timeouts                    │
│                                                 │
│ ❌ Synchronous chains                          │
│    ✅ Use async where possible                 │
│                                                 │
└─────────────────────────────────────────────────┘
```

---