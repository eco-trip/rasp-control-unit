package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.execution.Futures;

public class ExecutionTest {
    @Test
    public void testInstantInMicros() {
        var ins1 = Execution.instantInMicros();
        assertTrue(ins1 > Execution.SECOND_IN_MICRO);
        var ins2 = Execution.instantInMicros();
        assertNotEquals(ins1, ins2);
        assertTrue(ins1 < ins2);
    }

    @Test
    public void testExecutionTimes() throws InterruptedException {
        Runnable sleep = () -> Execution.safeSleep(Execution.SECOND_IN_MILLIS);
        var time1 = Execution.getComputationalTimeInMillis(sleep);
        assertTrue(time1 >= Execution.SECOND_IN_MILLIS);
        var hasThrown = new AtomicBoolean(false);
        Runnable sleep2 = () -> Execution.safeSleep(Execution.SECOND_IN_MILLIS, e -> hasThrown.set(true));
        var tmp = new Thread(() -> Execution.getComputationalTimeInMillis(sleep2));
        tmp.start();
        tmp.interrupt();
        tmp.join();
        assertTrue(hasThrown.get());
    }

    @Test
    public void testBusyWaiting() {
        Runnable sleep = () -> Execution.delayMicroseconds(Execution.SECOND_IN_MICRO);
        var time1 = Execution.getComputationalTimeInMillis(sleep);
        assertTrue(time1 >= Execution.SECOND_IN_MILLIS);
    }

    @Test
    public void testFuturesOps() {
        var fut1 = CompletableFuture.completedFuture(5);
        assertEquals(5, Futures.safeGet(fut1, Execution.SECOND_IN_MILLIS));
        var fut2 = CompletableFuture.completedFuture(2);
        var sum = Futures.thenAll(List.of(fut1, fut2),
                numbers -> numbers.stream().reduce(Integer::sum)).join();
        assertTrue(sum.isPresent());
        assertEquals(7, sum.get());
    }
}
