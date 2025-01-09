# Polyglot Distributed Framework

## Introduction

The Polyglot Distributed Framework is a versatile, multi-language framework designed to implement and explore distributed systems concepts. It incorporates the use of Rust, Java, Python, and C++ to develop various components such as distributed log search, group membership protocols, and stream processing.

## Features

- **Multi-language Support**: Implementations in Rust, Java, Python, and C++.
- **Distributed Log Search**: A system for querying distributed logs using Rust.
- **Group Membership**: Failure detection and dynamic membership updates using protocols like Ping-Ack and Ping-Ack+S.
- **Stream Processing Framework**: Real-time analytics on data streams with fault tolerance and exactly-once semantics.
- **Scalability**: Designed to scale across multiple machines in a distributed environment.

## Components

1. **Log Search**:
   - Client-server architecture for querying distributed logs.
   - Multi-threaded implementation for parallel search across nodes.

2. **Group Membership**:
   - Implements SWIM-inspired protocols for failure detection.
   - Supports membership updates and recovery mechanisms.

3. **Stream Processing**:
   - A framework similar to RainStorm for real-time data analytics.
   - Includes operators for transformation, filtering, and aggregation.

4. **Utilities**:
   - Scripts for data preprocessing, simulation, and visualization.

## Requirements

- **Languages & Tools**:
  - Rust (cargo, rustc)
  - Java (JDK 11+)
  - Python (3.8+)
  - C++ (GCC/Clang with C++17)
- **Libraries**:
  - Rust: `serde`, `serde_json`, `regex`
  - Java: Standard networking libraries
  - Python: `numpy`, `matplotlib`
  - C++: STL
