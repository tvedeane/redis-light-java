package dev.tvedeane;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Redis {
    private final ConcurrentHashMap<String, Entry> storage = new ConcurrentHashMap<>();

    public String getEntrySingle(String key) {
        var entry = storage.get(key);
        return entry != null ? entry.getSingleValue() : null;
    }

    public List<String> getEntryMultiple(String key) {
        /*
         This approach addresses the case where another thread might clear the list
         between the time this thread retrieves the entry from storage and subsequently
         accesses the list (ensuring atomic operation).
         */
        final var result = new AtomicReference<List<String>>();
        storage.computeIfPresent(key, (k, v) -> {
            result.set(v.getMultipleValues());
            return v;
        });
        return result.get();
    }

    public void addSingle(String key, String value) {
        storage.put(key, Entry.ofSingle(value));
    }

    public void addMultiple(String key, String value) {
        storage.compute(key, (k, v) -> {
            if (v == null) {
                return Entry.ofMultiple(value);
            }
            v.addMultiple(value);
            return v;
        });
    }

    public int removeMultiple(String key, String value, int count) {
        if (count == 0) {
            var removedCount = new AtomicInteger(0);
            storage.computeIfPresent(key, (k, v) -> {
                var originalSize = v.getMultipleSize();
                v.removeItemsMatching(value);
                removedCount.set(originalSize - v.getMultipleSize());
                return v.getMultipleValues().isEmpty() ? null : v;
            });
            return removedCount.get();
        } else if (count < 0) {
            var removedCount = new AtomicInteger(0);
            storage.computeIfPresent(key, (k, v) -> {
                for (int i = v.getMultipleSize() - 1; i >= 0; i--) {
                    if (removedCount.get() == -count) {
                        break;
                    }
                    if (v.getMultipleValues().get(i).equals(value)) {
                        v.removeAt(i);
                        removedCount.addAndGet(1);
                    }
                }
                return v.getMultipleValues().isEmpty() ? null : v;
            });
            return removedCount.get();
        } else {
            var removedCount = new AtomicInteger(0);
            storage.computeIfPresent(key, (k, v) -> {
                for (String s : v.getMultipleValues()) {
                    if (removedCount.get() == count) {
                        break;
                    }
                    if (s.equals(value)) {
                        v.remove(s);
                        removedCount.addAndGet(1);
                    }
                }
                return v.getMultipleValues().isEmpty() ? null : v;
            });
            return removedCount.get();
        }
    }
}
