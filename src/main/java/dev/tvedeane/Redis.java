package dev.tvedeane;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Redis {
    private final ConcurrentHashMap<String, Entry> storage = new ConcurrentHashMap<>();

    public String getEntrySingle(String key) {
        return storage.get(key).getSingleValue();
    }

    public List<String> getEntryMultiple(String key) {
        return storage.get(key).getMultipleValues();
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
                return v;
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
                return v;
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
                return v;
            });
            return removedCount.get();
        }
    }
}
