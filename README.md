# Polyglot Distributed Framework

## Introduction

The Polyglot Distributed Framework is a multi-language project that explores distributed systems concepts using Rust, Java, Python, and C++. It integrates functionalities like distributed log search, group membership, stream processing, and a fault-tolerant distributed file system (HyDFS).

## Features

1. **Distributed Log Search**:
   - A system to query distributed logs across multiple nodes using multi-threading and TCP communication.

2. **Group Membership Protocol**:
   - Implements SWIM-inspired protocols for dynamic membership updates and failure detection.

3. **Stream Processing Framework**:
   - Real-time analytics with fault tolerance, exactly-once semantics, and custom user-defined transformations.

4. **Distributed File System (HyDFS)**:
   - Fault-tolerant file replication with read and write operations optimized for latency and consistency.
   - Supports write quorum and sequence-number-based consistency for replicated files.

5. **Caching Mechanism**:
   - Implements a cache for read-heavy workloads, with performance optimized for both uniform and Zipfian distributions.

## Functional Overview

### Distributed File System (HyDFS)

1. **Replication**:
   - Files are replicated on three servers to tolerate up to two simultaneous failures.
   - Uses a pull-based approach to check for missing replicas every 5 seconds.

2. **Read Operation**:
   - Reads are served using a per-file sequence number to ensure the latest data is retrieved.
   - Sequential querying of replicas until the most recent version is found.

3. **Write Operation**:
   - Writes are coordinated by a per-key coordinator.
   - The coordinator ensures total ordering and propagates updates to replicas with acknowledgments for consistency.

4. **Cache Performance**:
   - Optimizes read latency by caching frequently accessed files.
   - Handles cache invalidation effectively for workloads with append-heavy operations.

### Integration with Other Components

- **Log Search**: Log querying integrated with the distributed file system for efficient storage and access.
- **Group Membership**: HyDFS leverages membership updates to handle dynamic changes in the cluster.
- **Stream Processing**: Uses HyDFS for checkpointing and intermediate storage during stream processing.

## Requirements

- **Languages**:
  - Rust, Java, Python, C++
- **Dependencies**:
  - Rust: `serde`, `serde_json`, `regex`
  - Python: `numpy`, `matplotlib`
  - Java: Standard libraries
  - C++: STL
