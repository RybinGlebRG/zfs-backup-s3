package ru.rerumu.utils.callables;

import org.junit.jupiter.api.Test;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableSupplier;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;

public class TestCallableExecutorImpl {

    @Test
    void shouldCall() throws Exception{
        CallableExecutorImpl callableExecutor = new CallableExecutorImpl();

        Callable<String> callable = (Callable<String>) mock(Callable.class);

        when(callable.call()).thenReturn("Test");

        callableExecutor.callWithRetry(new CallableSupplier<>(()->callable));

        verify(callable,times(1)).call();
    }

    @Test
    void shouldCallWithRetry() throws Exception{
        CallableExecutorImpl callableExecutor = new CallableExecutorImpl();

        Callable<String> callable = (Callable<String>) mock(Callable.class);

        when(callable.call())
                .thenThrow(IllegalArgumentException.class)
                .thenReturn("Test");

        callableExecutor.callWithRetry(new CallableSupplier<>(()->callable));

        verify(callable,times(2)).call();
    }
}
