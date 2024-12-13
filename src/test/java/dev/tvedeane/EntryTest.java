package dev.tvedeane;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntryTest {
    @Test
    void throwsWhenUsingMultipleMethodsOnSingle() {
        var entry = Entry.ofSingle("single");

        assertThrows(IllegalStateException.class, entry::getMultipleSize);
        assertThrows(IllegalStateException.class, () -> entry.addMultiple("second"));
        assertThrows(IllegalStateException.class, () -> entry.remove("single"));
        assertThrows(IllegalStateException.class, () -> entry.removeAt(0));
    }

    @Test
    void throwsWhenUsingSingleMethodsOnMultiple() {
        var entry = Entry.ofMultiple("multiple");

        assertThrows(IllegalStateException.class, entry::getSingleValue);
    }
}
