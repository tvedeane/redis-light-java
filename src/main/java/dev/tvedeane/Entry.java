package dev.tvedeane;

import java.util.ArrayList;
import java.util.List;

public class Entry {
    private final String value;
    private final List<String> values;

    public Entry(String value, List<String> values) {
        this.value = value;
        this.values = values;
    }

    public static Entry ofSingle(String s) {
        return new Entry(s, null);
    }

    public static Entry ofMultiple(String s) {
        var list = new ArrayList<String>();
        list.add(s);
        return new Entry(null, list);
    }

    public String getSingleValue() {
        return value;
    }

    public int getMultipleSize() {
        if (value != null) {
            throw new IllegalStateException("Cannot get multiple size for single value.");
        }
        return values.size();
    }

    public List<String> getMultipleValues() {
        return List.copyOf(values);
    }

    public void addMultiple(String s) {
        if (value != null) {
            throw new IllegalStateException("Cannot add multiple for single value.");
        }
        values.addFirst(s);
    }

    public void removeItemsMatching(String s) {
        values.removeIf(v -> v.equals(s));
    }
}
