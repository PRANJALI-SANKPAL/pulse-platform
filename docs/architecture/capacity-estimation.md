# Pulse Platform — Capacity Estimation

## 1. Purpose

This document defines the initial capacity assumptions used to design Pulse Platform.

These figures are design assumptions for the portfolio project and are not production traffic claims.

---

## 2. User Assumptions

| Metric                            | Assumption |
| --------------------------------- | ---------: |
| Registered users                  |    100,000 |
| Monthly active users              |     20,000 |
| Average workflows per active user |         10 |
| Estimated workflows               |   ~200,000 |

---

## 3. Workflow Volume

Assume each workflow executes approximately 20 times per month.

```text
20,000 active users
× 10 workflows/user
= 200,000 workflows

200,000 workflows
× 20 executions/month
= 4,000,000 executions/month
```

Therefore:

**Estimated workflow executions: ~4 million/month**

---

## 4. Average Execution Rate

Assuming 30 days/month:

```text
4,000,000
÷ 30
≈ 133,333 executions/day
```

Per second:

```text
133,333
÷ 86,400
≈ 1.54 executions/sec
```

### Average execution rate

**~1.5 executions/sec**

---

## 5. Peak Execution Rate

Assuming peak traffic is approximately 10× average:

```text
1.54 × 10
≈ 15.4 executions/sec
```

### Target peak execution rate

**~15 executions/sec**

---

## 6. API Traffic

Assume approximately five API interactions per workflow execution.

```text
4M executions
× 5 API interactions
= 20M API requests/month
```

Average:

```text
20M
÷ 30
÷ 86,400
≈ 7.7 requests/sec
```

Peak:

```text
7.7 × 10
≈ 77 requests/sec
```

### API capacity target

* Average: ~8 requests/sec
* Peak: ~80 requests/sec

---

## 7. Event / Kafka Volume

Assume approximately five events per workflow execution.

```text
4M executions
× 5 events
= 20M events/month
```

Average:

**~7.7 events/sec**

Peak:

**~77 events/sec**

The messaging layer should therefore support at least this throughput with sufficient headroom for retries and future growth.

---

## 8. Database Storage

Assume an average workflow execution record size of approximately 2 KB.

```text
4M
× 2 KB
≈ 8 GB/month
```

Annual execution-record storage:

```text
8 GB
× 12
≈ 96 GB/year
```

Additional storage will be required for:

* Workflow definitions
* Users
* Event metadata
* Audit records
* Indexes
* Supporting tables

### Initial planning estimate

**~150–200 GB/year of relational data**

---

## 9. Redis Capacity

Redis will be used selectively rather than as the primary data store.

Potential cached information includes:

* Workflow metadata
* Idempotency keys
* Rate-limit counters
* Short-lived execution state

The initial cache workload is expected to be relatively small compared with the persistent database.

---

## 10. Capacity Summary

| Component           |     Average |          Peak / Planning Target |
| ------------------- | ----------: | ------------------------------: |
| Workflow executions |    ~1.5/sec |                         ~15/sec |
| API requests        |      ~8/sec |                         ~80/sec |
| Events              |      ~8/sec |                         ~80/sec |
| Workflow executions |    4M/month |                               — |
| Execution storage   | ~8 GB/month | ~150–200 GB/year total planning |

---

## 11. Scaling Implications

The capacity assumptions lead to the following architectural requirements:

### Application Layer

Application services should remain as stateless as practical so that multiple instances can be deployed behind a load balancer.

### Messaging Layer

Kafka should decouple API requests from asynchronous workflow processing.

Consumers should be independently scalable.

### Database

PostgreSQL should use appropriate:

* Indexes
* Connection pooling
* Query optimization
* Retention policies

Partitioning can be evaluated as execution history grows.

### Cache

Redis can reduce repeated database reads and support:

* Idempotency
* Rate limiting
* Short-lived state

### Worker Layer

Workflow workers should scale independently from the API layer.

This allows processing capacity to increase without unnecessarily scaling synchronous API infrastructure.

---

## 12. Future Growth Scenario

The architecture should leave room for approximately 10× growth from the initial planning target.

Future considerations include:

* Horizontal scaling
* Kafka partition scaling
* Database read replicas
* Database partitioning
* Distributed caching
* Kubernetes autoscaling
* Event retention policies
* Data archival
