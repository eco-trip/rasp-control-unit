package engine;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class Engine {
    private final List<CompletableFuture<Void>> backgroundJobs;

    Engine() {
        backgroundJobs = List.of();
    }

    public void stop() {
        backgroundJobs.forEach(c -> c.complete(null));
    }

    public void stop(final Throwable ex) {
        backgroundJobs.forEach(c -> c.completeExceptionally(ex));
    }

    public abstract void schedule(Runnable job, long repeatEvery);

    public abstract void schedule(final Runnable job);

    public void waitScheduledJobs() {
        backgroundJobs.forEach(CompletableFuture::join);
    }

    public boolean isRunning() {
        return backgroundJobs.stream().anyMatch(c -> !c.isDone());
    }
}
