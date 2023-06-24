package ru.rerumu.zfs_backup_s3.utils.callables;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableSupplier;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

@ThreadSafe
public sealed interface CallableExecutor permits CallableExecutorImpl {

    <T> T callWithRetry(CallableSupplier<T> callableSupplier);
}
