# Redis-like implementation in Java
This storage solution offers a thread-safe way to manage key-value pairs, where each key is associated with one or more string values.

### Features
- **Single or Multiple Values**: Store a single string or a list of strings under a key.
- **Flexible Value Management**:
    - Add new strings to an existing list.
    - Remove strings selectively based on matching criteria:
        - Remove all matching strings.
        - Remove the first matching strings.
        - Remove the last matching strings.
