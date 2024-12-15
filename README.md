# Redis-like implementation in Java
This storage solution offers a thread-safe way to manage key-value pairs, 
where each key is associated with one or more string values.

### Features
- **Single or Multiple Values**: Store a single string or a list of strings under a key.
- **Flexible Value Management**:
    - Add new strings to an existing list.
    - Remove strings selectively based on matching criteria:
        - Remove all matching strings (count == 0).
        - Remove the first matching strings (count < 0).
        - Remove the last matching strings (count > 0).
    - Set an expiration time to mark the entry for removal at a specified time.

### Dependencies

- jUnit 5 - testing framework.
- AssertJ - fluent assertions in tests.
- Awaitility - testing asynchronous operations.

### Implementation Details

Internally, the data is stored in a `ConcurrentHashMap`, ensuring thread-safe operations in a multi-threaded 
environment. All operations on this structure are atomic and inherently thread-safe.

Entry expiration is managed using a `LocalDateTime` timestamp for each entry. A `ScheduledExecutorService` periodically 
executes a cleanup task at a fixed rate, iterating over the map to remove expired entries. The cleanup uses 
`computeIfPresent` to atomically validate and remove entries. Since the map's iterators are weakly consistent, 
modifications to entries during iteration might not be visible in the current run but will be handled in subsequent 
runs or during direct access (which also validates expiration).

While iterating over all entries to identify expired ones can be time-consuming, it is a trade-off for simplicity and 
performance. Alternatives like a `PriorityQueue` with expiration times as keys (for quick access to the oldest entries) 
could optimize expiration checks but require synchronization with the `ConcurrentHashMap`, potentially reducing 
concurrency. Similarly, using `ReentrantReadWriteLock` for finer-grained control could add complexity and overhead. The 
current implementation avoids explicit locks or synchronization constructs, leveraging the sophisticated internal 
mechanisms of `ConcurrentHashMap` for optimal concurrency.
