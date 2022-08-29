package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.object.ObjectUtils;


public class ObjectUtilsTest {
    @Test
    public void testIfNotNull() {
        AtomicInteger counter = new AtomicInteger();
        ObjectUtils.ifNotNull(0, v -> counter.getAndIncrement());
        assertEquals(counter.get(), 1);
        ObjectUtils.ifNotNull(1, v -> counter.getAndIncrement());
        assertEquals(counter.get(), 2);
        ObjectUtils.ifNotNull(null, v -> counter.getAndIncrement());
        assertEquals(counter.get(), 2);
    }
}
