package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.execution.engine.EngineFactory;


public class EngineTest {
    @Test
    public void testSingleThreadEngine() {
        var engine = EngineFactory.createScheduledEngine(1);
        var fut1 = engine.submit(() -> Execution.delayMicroseconds(Execution.SECOND_IN_MICRO));
        assertTrue(Execution.getComputationalTimeInMillis(fut1::join) >= Execution.SECOND_IN_MILLIS);
        var counter = new AtomicInteger(0);
        var counting = engine.schedule(counter::incrementAndGet, 100);
        Execution.safeSleep(500);
        counting.complete(null);
        assertTrue(counter.get() >= 5);
        var futureCounter = new AtomicInteger(0);
        var count = CompletableFuture.completedFuture(futureCounter.get());
        var finalCount = engine.submitAndRepeat(c -> c.thenCombine(
                CompletableFuture.supplyAsync(futureCounter::incrementAndGet), Integer::sum), count, 5, 0);
        assertEquals(finalCount.join(), 15);
    }
}
