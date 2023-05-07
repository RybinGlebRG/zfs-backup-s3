package ru.rerumu.utils.callables.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.callables.CallableExecutor;

import java.util.concurrent.*;

public class CallableExecutorImpl implements CallableExecutor {
    private final static Long DELAY = 10L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public <T> T callWithRetry(Callable<T> callable) {
        Future<T> future = scheduledExecutorService.submit(callable);
        while (true) {
            try {
                return future.get();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            future = scheduledExecutorService.schedule(
                    callable,
                    DELAY,
                    TimeUnit.SECONDS
            );
        }
    }
}
