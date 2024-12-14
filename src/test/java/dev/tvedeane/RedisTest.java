package dev.tvedeane;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class RedisTest {
    @Test
    void handlesNotFoundKey() {
        var redis = new Redis();

        assertThat(redis.getEntrySingle("key1")).isNull();
        assertThat(redis.getEntryMultiple("key1")).isNull();
    }

    @Test
    void addSingleAndMultiple() {
        var redis = new Redis();

        redis.addSingle("key1", "value1");
        redis.addMultiple("key2", "value2");

        assertThat(redis.getEntrySingle("key1")).isEqualTo("value1");
        assertThat(redis.getEntryMultiple("key2").size()).isEqualTo(1);
    }

    @Test
    void removesNothingIfNotMatched() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");

        var removedCount1 = redis.removeMultiple("key1", "value2", 0);
        var removedCount2 = redis.removeMultiple("key2", "value1", 0);
        assertThat(removedCount1).isEqualTo(0);
        assertThat(removedCount2).isEqualTo(0);
        assertThat(redis.getEntryMultiple("key1")).containsOnly("value1");
    }

    @Test
    void removesMatchingElements() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value2");

        var removedCount = redis.removeMultiple("key1", "value1", 0);
        assertThat(removedCount).isEqualTo(2);
        assertThat(redis.getEntryMultiple("key1")).containsOnly("value2");
    }

    @Test
    void removesNewestMatchingElements() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value2");
        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value1");

        var removedCount = redis.removeMultiple("key1", "value1", 2);
        assertThat(removedCount).isEqualTo(2);
        assertThat(redis.getEntryMultiple("key1")).containsExactly("value2", "value1");
    }

    @Test
    void removesNewestMatchingElements_countHigher() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value2");
        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value1");

        var removedCount = redis.removeMultiple("key1", "value1", 9);
        assertThat(removedCount).isEqualTo(3);
        assertThat(redis.getEntryMultiple("key1")).containsExactly("value2");
    }

    @Test
    void removesOldestMatchingElements() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value2");
        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value1");

        var removedCount = redis.removeMultiple("key1", "value1", -1);
        assertThat(removedCount).isEqualTo(1);
        assertThat(redis.getEntryMultiple("key1")).containsExactly("value1", "value1", "value2");
    }

    @Test
    void removesOldestMatchingElements_countHigher() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value2");
        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value1");

        var removedCount = redis.removeMultiple("key1", "value1", -9);
        assertThat(removedCount).isEqualTo(3);
        assertThat(redis.getEntryMultiple("key1")).containsExactly("value2");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1})
    void returnsNullWhenAllElementsRemoved(int removalCount) {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");

        var removedCount = redis.removeMultiple("key1", "value1", removalCount);
        assertThat(removedCount).isEqualTo(1);
        assertThat(redis.getEntryMultiple("key1")).isNull();
    }

    @Nested
    class MultiThreading {
        @RepeatedTest(value = 1_000)
        void handlesAtomicGettingAndRemoval() throws InterruptedException {
            var redis = new Redis();

            for (int i = 0; i < 10; i++) {
                redis.addMultiple("key1", "value1");
            }

            final var result = new ArrayList<String>();
            var latch = new CountDownLatch(1);
            var t1 = new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                redis.removeMultiple("key1", "value1", 0);
            });
            var t2 = new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                var entries = redis.getEntryMultiple("key1");
                if (entries != null) {
                    result.addAll(entries);
                }
            });
            t1.start();
            t2.start();
            latch.countDown();
            t1.join();
            t2.join();

            if (!List.of(0, 10).contains(result.size())) {
                fail("Non-atomic operation encountered, entries count: " + result.size());
            }
        }
    }
}