package dev.tvedeane;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

abstract class ExpiringEntry {
    private LocalDateTime expiryTime;

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}

abstract class Entry<V> extends ExpiringEntry {
    protected V value;

    public static Entry<String> ofSingle(String s) {
        return new SingleEntry(s);
    }

    public static Entry<List<String>> ofMultiple(String s) {
        var list = new ArrayList<String>();
        list.add(s);
        return new MultipleEntry(list);
    }
}

final class SingleEntry extends Entry<String> {
    public SingleEntry(String v) {
        this.value = v;
    }

    public String getSingleValue() {
        return value;
    }
}

final class MultipleEntry extends Entry<List<String>> {
    public MultipleEntry(List<String> v) {
        this.value = v;
    }

    public int getMultipleSize() {
        return value.size();
    }

    public List<String> getMultipleValues() {
        return List.copyOf(value);
    }

    public void addMultiple(String s) {
        value.addFirst(s);
    }

    public void remove(String s) {
        value.remove(s);
    }

    public void removeAt(int i) {
        value.remove(i);
    }

    public void removeItemsMatching(String s) {
        value.removeIf(v -> v.equals(s));
    }
}
