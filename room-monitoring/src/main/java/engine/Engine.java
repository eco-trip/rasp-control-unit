package engine;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class Engine {
    private final List<CompletableFuture<Void>> backgroundJobs;
    private final CompletableFuture<Void> execution;

    Engine() {
        backgroundJobs = List.of();
        execution = new CompletableFuture<>();
    }

    public void stop() {
        backgroundJobs.forEach(c -> c.complete(null));
        execution.complete(null);
    }

    public void stop(final Throwable ex) {
        backgroundJobs.forEach(c -> c.completeExceptionally(ex));
        execution.complete(null);
    }

    public abstract void schedule(Runnable job, long repeatEvery);

    public abstract void submit(final Runnable job);

    public void waitExecution() {
        execution.join();
    }

    public boolean isRunning() {
        return backgroundJobs.stream().anyMatch(c -> !c.isDone());
    }
}
