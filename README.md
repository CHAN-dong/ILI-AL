# ILI-AL: An Inverted Learned Index with Auxiliary Location for Reliable Keyword Queries in Hybrid-Storage Blockchain

AILI-AL is a novel index structure designed for efficient keyword queries and verification in hybrid-storage blockchain environments. This Java implementation provides a reliable and cost-effective solution for keyword queries based on learned index structures.

## Features

- Supports efficient keyword queries in hybrid-storage blockchain.
- Implements an optimized scheme, ILI-AL*, for enhanced query, verification, and arbitration performance.
- Utilizes BLS signatures for secure and reliable query verification.

## Technology Stack

- Java
- SHA-256 for hashing
- BN254 curve for elliptic curve computations

## Usage
We have provided a test.java file to evaluate the algorithm's performance in index construction, querying, and verification.
```java
// Example code to perform a test
invertPath = "./test.txt";
Test(invertPath);
```

## Code Structure

The project is organized into several key directories, each with a specific purpose:

### dataowner
This folder contains the modules related to the Data Owner (DO) entity, responsible for data upload, index construction, and updates.

### server
This folder houses the Cloud Server (CS) related modules, which handle client query requests and return query results.

### SC (Smart Contracts)
This folder contains the blockchain smart contract code for arbitration purposes. The contracts can be compiled and deployed directly to a blockchain platform that supports the Solidity language.
