package engine;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Engine<T> {
    private final List<ScheduledFuture<?>> backgroundJobs;
    private final CompletableFuture<Void> execution;

    Engine() {
        backgroundJobs = List.of();
        execution = new CompletableFuture<>();
    }

    public void stop() {
        backgroundJobs.forEach(c -> c.cancel(true));
        execution.complete(null);
    }

    protected List<ScheduledFuture<?>> getBackgroundJobs() {
        return backgroundJobs;
    }

    public abstract void schedule(Runnable job, long repeatEvery);

    public abstract void submit(final Runnable job);

    public abstract CompletableFuture<T> submitAndRepeat(final Function<CompletableFuture<T>, CompletableFuture<T>> job,
                                                final CompletableFuture<T> accumulator,
                                                final int repetitions, final int delayInSeconds);

    public void waitExecution() {
        execution.join();
    }

    public boolean isRunning() {
        return backgroundJobs.stream().anyMatch(c -> !c.isDone());
    }
}
