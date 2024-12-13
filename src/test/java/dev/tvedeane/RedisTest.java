package dev.tvedeane;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisTest {
    @Test
    void addSingleAndMultiple() {
        var redis = new Redis();

        redis.addSingle("key1", "value1");
        redis.addMultiple("key2", "value2");

        assertThat(redis.getEntry("key1").getSingleValue()).isEqualTo("value1");
        assertThat(redis.getEntry("key2").getMultipleSize()).isEqualTo(1);
    }

    @Test
    void removesNothingIfNotMatched() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");

        var removedCount1 = redis.removeMultiple("key1", "value2", 0);
        var removedCount2 = redis.removeMultiple("key2", "value1", 0);
        assertThat(removedCount1).isEqualTo(0);
        assertThat(removedCount2).isEqualTo(0);
        assertThat(redis.getEntry("key1").getMultipleValues()).containsOnly("value1");
    }

    @Test
    void removesAllMatchingElements_sizeOne() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");

        var removedCount = redis.removeMultiple("key1", "value1", 0);
        assertThat(removedCount).isEqualTo(1);
        assertThat(redis.getEntry("key1").getMultipleValues()).isEmpty();
    }

    @Test
    void removesAllMatchingElements_sizeMany() {
        var redis = new Redis();

        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value1");
        redis.addMultiple("key1", "value2");

        var removedCount = redis.removeMultiple("key1", "value1", 0);
        assertThat(removedCount).isEqualTo(2);
        assertThat(redis.getEntry("key1").getMultipleValues()).containsOnly("value2");
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
        assertThat(redis.getEntry("key1").getMultipleValues()).containsExactly("value2", "value1");
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
        assertThat(redis.getEntry("key1").getMultipleValues()).containsExactly("value2");
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
        assertThat(redis.getEntry("key1").getMultipleValues()).containsExactly("value1", "value1", "value2");
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
        assertThat(redis.getEntry("key1").getMultipleValues()).containsExactly("value2");
    }
}