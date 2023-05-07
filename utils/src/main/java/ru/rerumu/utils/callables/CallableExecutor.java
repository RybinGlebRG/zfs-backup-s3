package ru.rerumu.utils.callables;

import java.util.concurrent.Callable;

public interface CallableExecutor {

    <T> T callWithRetry(Callable<T> callable);
}
