package dev.tvedeane;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Redis {
    private final ConcurrentHashMap<String, Entry> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public Redis() {
        initializeScheduledExecutor(60);
    }

    // needed for testing
    Redis(int seconds) {
        initializeScheduledExecutor(seconds);
    }

    private void initializeScheduledExecutor(int seconds) {
        cleanupExecutor
                .scheduleAtFixedRate(this::removeExpiredEntries, seconds, seconds, TimeUnit.SECONDS);
    }

    private void removeExpiredEntries() {
        storage.forEach((key, v) ->
                storage.computeIfPresent(key, (k, entry) -> {
                    if (isEntryExpired(entry)) {
                        return null;
                    }
                    return entry;
                }));
    }

    // needed for testing
    int getStorageSize() {
        return storage.size();
    }

    public String getEntrySingle(String key) {
        /*
         This approach addresses the case where another thread might clear the list
         between the time this thread retrieves the entry from storage and subsequently
         accesses the list (ensuring atomic operation).
         */
        final var result = new AtomicReference<String>();
        storage.computeIfPresent(key, (k, entry) -> {
            if (isEntryExpired(entry)) {
                return null;
            }
            result.set(entry.getSingleValue());
            return entry;
        });
        return result.get();
    }

    public List<String> getEntryMultiple(String key) {
        final var result = new AtomicReference<List<String>>();
        storage.computeIfPresent(key, (k, entry) -> {
            if (isEntryExpired(entry)) {
                return null;
            }
            result.set(entry.getMultipleValues());
            return entry;
        });
        return result.get();
    }

    public void addSingle(String key, String value) {
        storage.put(key, Entry.ofSingle(value));
    }

    public void addMultiple(String key, String value) {
        storage.compute(key, (k, entry) -> {
            if (entry == null || isEntryExpired(entry)) {
                return Entry.ofMultiple(value);
            }
            entry.addMultiple(value);
            return entry;
        });
    }

    public int removeMultiple(String key, String value, int count) {
        var removedCount = new AtomicInteger(0);
        storage.computeIfPresent(key, (k, entry) -> {
            if (isEntryExpired(entry)) {
                return null;
            }
            if (count == 0) {
                return removeAllMatching(value, entry, removedCount);
            } else if (count < 0) {
                return removeOldestMatching(value, entry, count, removedCount);
            } else {
                return removeNewestMatching(value, entry, count, removedCount);
            }
        });
        return removedCount.get();
    }

    private Entry removeAllMatching(String value, Entry entry, AtomicInteger removedCount) {
        var originalSize = entry.getMultipleSize();
        entry.removeItemsMatching(value);
        removedCount.set(originalSize - entry.getMultipleSize());
        return entry.getMultipleValues().isEmpty() ? null : entry;
    }

    private Entry removeOldestMatching(String value, Entry entry, int count, AtomicInteger removedCount) {
        for (int i = entry.getMultipleSize() - 1; i >= 0; i--) {
            if (removedCount.get() == -count) {
                break;
            }
            if (entry.getMultipleValues().get(i).equals(value)) {
                entry.removeAt(i);
                removedCount.addAndGet(1);
            }
        }
        return entry.getMultipleValues().isEmpty() ? null : entry;
    }

    private Entry removeNewestMatching(String value, Entry entry, int count, AtomicInteger removedCount) {
        for (String s : entry.getMultipleValues()) {
            if (removedCount.get() == count) {
                break;
            }
            if (s.equals(value)) {
                entry.remove(s);
                removedCount.addAndGet(1);
            }
        }
        return entry.getMultipleValues().isEmpty() ? null : entry;
    }

    public void setExpiryTime(String key, LocalDateTime expiryTime) {
        storage.computeIfPresent(key, (k, entry) -> {
            entry.setExpiryTime(expiryTime);
            return entry;
        });
    }

    private static boolean isEntryExpired(Entry entry) {
        var expiryTime = entry.getExpiryTime();
        if (expiryTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
