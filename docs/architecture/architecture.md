# Pulse Platform — Initial Architecture

## 1. Document Status

**Status:** Initial Design
**Version:** 0.2
**Last Updated:** October 2026

This document defines the initial high-level architecture for Pulse Platform based on the current product requirements and capacity estimates.

The architecture will evolve as implementation progresses and additional system-design decisions are validated.

---

## 2. System Objective

Pulse Platform is a production-oriented backend platform designed to provide asynchronous workflow execution and operational visibility.

The system is designed to demonstrate:

* Backend engineering
* Scalable system design
* Distributed systems
* Event-driven architecture
* Asynchronous processing
* Cloud infrastructure
* Observability
* AI-assisted operations

The platform prioritizes reliability, scalability, maintainability, security, and observability.

---

## 3. System Requirements Driving the Architecture

The architecture is driven by the following requirements:

* Users must be able to create and trigger workflows.
* Workflow execution should be asynchronous.
* API requests should not remain blocked by long-running operations.
* Workflow execution status must be trackable.
* Failed operations should support retries.
* Event processing should be idempotent.
* Workflow and execution information must be persisted.
* API and background-processing workloads should be independently scalable.
* System behavior should be observable through logs, metrics, and traces.
* AI-assisted operational capabilities will be added after the core platform is stable.

---

## 4. Capacity Planning

The initial design targets approximately:

| Metric                       |           Target |
| ---------------------------- | ---------------: |
| Registered users             |          100,000 |
| Monthly active users         |           20,000 |
| Workflows                    |         ~200,000 |
| Workflow executions          | ~4 million/month |
| Average executions           |         ~1.5/sec |
| Peak executions              |          ~15/sec |
| Average API requests         |           ~8/sec |
| Peak API requests            |          ~80/sec |
| Average events               |           ~8/sec |
| Peak events                  |          ~80/sec |
| Estimated relational storage | ~150–200 GB/year |

Detailed assumptions and calculations are documented in:

`docs/architecture/capacity-estimation.md`

These figures represent design assumptions for the project and are not production traffic claims.

---

## 5. High-Level Architecture

```text
                              ┌─────────────────┐
                              │     Client      │
                              └────────┬────────┘
                                       │
                                       ▼
                              ┌─────────────────┐
                              │  Load Balancer  │
                              └────────┬────────┘
                                       │
                                       ▼
                         ┌──────────────────────────┐
                         │       API Service        │
                         │      Spring Boot         │
                         └────────────┬─────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
             ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
             │ PostgreSQL  │   │    Redis    │   │    Kafka    │
             │             │   │             │   │             │
             │ System of   │   │ Cache /     │   │ Async Event │
             │ Record      │   │ Idempotency │   │ Backbone    │
             └─────────────┘   └─────────────┘   └──────┬──────┘
                                                        │
                                                        ▼
                                              ┌──────────────────┐
                                              │  Worker Service  │
                                              │   Spring Boot    │
                                              └────────┬─────────┘
                                                       │
                                                       ▼
                                                ┌─────────────┐
                                                │ PostgreSQL  │
                                                └─────────────┘
```

The API layer accepts and validates requests, while asynchronous workflow execution is delegated to worker services through Kafka.

API and worker workloads can therefore be scaled independently.

---

## 6. Core Components

### 6.1 API Service

The API Service is responsible for synchronous client interactions.

Responsibilities include:

* REST APIs
* Authentication and authorization
* Request validation
* Workflow creation
* Workflow retrieval
* Workflow triggering
* Execution status queries
* Publishing workflow events
* API-level error handling

The service should remain stateless wherever practical so that multiple instances can run behind a load balancer.

---

### 6.2 PostgreSQL

PostgreSQL acts as the primary system of record.

It will store information such as:

* Users
* Workflows
* Workflow configurations
* Workflow executions
* Execution status
* Retry information
* Execution history
* Audit information

PostgreSQL is selected because the core domain contains relational data requiring transactions, consistency, indexing, and structured querying.

---

### 6.3 Redis

Redis will be used selectively for low-latency and short-lived data.

Potential use cases include:

* Frequently accessed workflow metadata
* Idempotency keys
* Rate-limit counters
* Short-lived execution state
* Temporary caching

Redis will not be treated as the primary source of truth for workflow data.

---

### 6.4 Kafka

Apache Kafka will provide the asynchronous event backbone.

Kafka will be used for:

* Workflow execution events
* Decoupling API and worker services
* Asynchronous processing
* Event buffering
* Consumer-based scaling
* Retry and failure-processing workflows

The initial system is expected to handle approximately 80 events/sec at peak based on current capacity assumptions, with additional headroom for future growth.

---

### 6.5 Worker Service

Worker services consume workflow execution events from Kafka.

Responsibilities include:

* Consuming workflow events
* Executing workflow operations
* Updating execution status
* Handling retryable failures
* Handling non-retryable failures
* Maintaining idempotency
* Publishing follow-up events where required

Worker instances should be independently scalable from API instances.

---

### 6.6 Observability Layer

Observability will be implemented across the system.

The platform will eventually provide:

* Structured application logs
* Metrics
* Distributed tracing
* Health checks
* Workflow execution metrics
* Error monitoring
* Alerts

Observability is considered a core architectural requirement rather than a final-stage add-on.

---

## 7. Request and Execution Flow

A typical workflow execution will follow this flow:

```text
Client
   │
   │ POST /workflows/{id}/execute
   ▼
API Service
   │
   ├── Authenticate
   ├── Validate request
   ├── Create execution record
   │
   └── Publish execution event
             │
             ▼
           Kafka
             │
             ▼
       Worker Service
             │
             ├── Consume event
             ├── Validate / deduplicate
             ├── Execute operation
             │
             ├── Success ───────► Update execution
             │
             └── Failure
                    │
                    ├── Retryable ──► Retry
                    │
                    └── Permanent ─► Failed
```

The API returns an acknowledgement without waiting for the complete workflow execution.

---

## 8. Scalability Strategy

### API Layer

API services will be stateless wherever possible.

Multiple instances can be deployed behind a load balancer.

```text
                    Load Balancer
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
           API-1      API-2      API-3
```

This allows API capacity to increase independently based on request traffic.

---

### Worker Layer

Workers will consume Kafka events independently from the API layer.

```text
                 Kafka
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
     Worker-1   Worker-2   Worker-3
```

Additional workers can be introduced as event-processing volume increases.

---

### Kafka

Kafka partitions can be used to increase consumer parallelism and distribute event-processing workload.

---

### Database

PostgreSQL scalability will initially focus on:

* Proper indexing
* Query optimization
* Connection pooling
* Efficient schema design
* Data retention policies

Future scaling options may include:

* Read replicas
* Partitioning
* Archival
* Database scaling strategies

---

### Redis

Redis reduces repeated reads for suitable data and can provide low-latency access to:

* Cached data
* Idempotency keys
* Rate-limit counters

---

## 9. Reliability Strategy

The system should tolerate failures without causing unnecessary cascading failures.

Key mechanisms include:

### Retry Handling

Retryable operations can be retried according to configured retry policies.

### Idempotency

Repeated processing of the same event should not create unintended duplicate side effects.

### Failure Isolation

Failure of a worker should not make the entire API layer unavailable.

### Dead-Letter Handling

Messages that cannot be successfully processed after configured retries should be isolated for investigation and recovery.

### Health Checks

Services should expose health information for infrastructure and orchestration systems.

---

## 10. Security Strategy

Security considerations include:

* Authentication
* Authorization
* Input validation
* Secure API access
* Secret management
* Environment-based configuration
* Protection of sensitive data
* Avoiding credentials in source control

Secrets such as database passwords, AWS credentials, and API keys must never be committed to the repository.

---

## 11. Deployment Strategy

The system is planned to evolve through the following deployment stages:

### Local Development

```text
Docker
   │
   ├── Spring Boot API
   ├── Spring Boot Worker
   ├── PostgreSQL
   ├── Redis
   └── Kafka
```

### Cloud Deployment

The application will eventually be deployed on AWS.

### Kubernetes

Kubernetes will be introduced to manage:

* API replicas
* Worker replicas
* Service discovery
* Configuration
* Health checks
* Horizontal scaling

The exact AWS architecture will be finalized during the cloud-infrastructure phase.

---

## 12. AI Integration

AI capabilities will be introduced after the core workflow platform is stable.

Potential capabilities include:

* Workflow failure summarization
* Failure explanation
* Corrective-action suggestions
* Natural-language workflow queries
* Operational assistance
* Context-aware workflow analysis

The AI layer should interact with the platform through controlled APIs/tools rather than directly accessing internal infrastructure.

---

## 13. Architectural Principles

The system will follow these principles:

1. **Separation of concerns**
2. **Stateless services where practical**
3. **API-first design**
4. **Horizontal scalability**
5. **Asynchronous processing where appropriate**
6. **Failure isolation**
7. **Idempotent event processing**
8. **Observability by design**
9. **Secure handling of credentials and sensitive data**
10. **Automated testing**
11. **Infrastructure as code where practical**
12. **Incremental architecture evolution**
13. **Depth over unnecessary architectural complexity**

---

## 14. Key Architecture Decisions

The following decisions have been made based on the current requirements.

| Decision              | Choice                             | Reason                                           |
| --------------------- | ---------------------------------- | ------------------------------------------------ |
| Backend               | Java + Spring Boot                 | Strong ecosystem for production backend services |
| Primary database      | PostgreSQL                         | Relational consistency and structured querying   |
| Cache                 | Redis                              | Low-latency caching and short-lived state        |
| Messaging             | Kafka                              | Asynchronous event processing and decoupling     |
| API style             | REST                               | Simple and widely supported integration model    |
| Background processing | Dedicated Worker Service           | Independent scaling and failure isolation        |
| Containerization      | Docker                             | Reproducible local and deployment environments   |
| Orchestration         | Kubernetes                         | Container orchestration and horizontal scaling   |
| Cloud                 | AWS                                | Cloud deployment and infrastructure experience   |
| Observability         | Logs + Metrics + Traces            | Production visibility and troubleshooting        |
| AI                    | Controlled service/API integration | Keeps AI capabilities decoupled from core system |

---

## 15. Current Architecture Status

The current architecture represents **HLD v0.2**.

Completed:

* Product requirements
* Functional requirements
* Non-functional requirements
* Initial API requirements
* Capacity estimation
* High-level component design
* Scalability strategy
* Reliability strategy

Next design activities:

1. Database schema design
2. Detailed API contract
3. Kafka topic design
4. Event schema design
5. Idempotency strategy
6. Retry and dead-letter strategy
7. Detailed service boundaries
8. Local Docker architecture

The architecture will be updated as these decisions are finalized.

---

## 16. Related Documentation

* `docs/requirements/requirements.md`
* `docs/architecture/capacity-estimation.md`
* `README.md`
