package ru.rerumu.utils.callables;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface CallableExecutor {

    <T> T callWithRetry(Supplier<Callable<T>> callableSupplier);
}
