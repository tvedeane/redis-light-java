# Redis-like implementation in Java
This storage solution offers a thread-safe way to manage key-value pairs, where each key is associated with one or more string values.

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
