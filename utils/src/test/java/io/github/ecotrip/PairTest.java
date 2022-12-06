package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.object.Pair;

public class PairTest {
    @Test
    public void testIfNotNull() {
        Pair<Integer, Integer> pair = Pair.of(1, 2);

        assertEquals(pair.value1(), 1);
        assertEquals(pair.value2(), 2);
    }
}
