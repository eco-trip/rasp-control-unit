package engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EngineFactory {
    public static Engine createScheduledExecutor() {
        return new Engine() {
            private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            /**
             *
             * @param toBeScheduled is the job which has to be scheduled
             * @param repeatEvery time after which the runnable must be rescheduled
             */
            @Override
            public void schedule(Runnable toBeScheduled, final long repeatEvery) {
                executor.scheduleAtFixedRate(toBeScheduled,0, repeatEvery, TimeUnit.SECONDS);
            }

            @Override
            public void schedule(Runnable toBeScheduled) {
                executor.schedule(toBeScheduled, 0 , TimeUnit.SECONDS);
            }
        };
    }
}
