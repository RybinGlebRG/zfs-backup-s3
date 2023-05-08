package ru.rerumu.utils.callables.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.callables.CallableExecutor;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class CallableExecutorImpl implements CallableExecutor {
    private final static Long DELAY = 10L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public <T> T callWithRetry(Supplier<Callable<T>> callableSupplier) {
        Future<T> future = scheduledExecutorService.submit(callableSupplier.get());
        while (true) {
            try {
                return future.get();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            future = scheduledExecutorService.schedule(
                    callableSupplier.get(),
                    DELAY,
                    TimeUnit.SECONDS
            );
        }
    }
}
