package ru.rerumu.zfs_backup_s3.utils.callables.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@ThreadSafe
public final class CallableSupplier<T> implements Supplier<Callable<T>> {

    private final Supplier<Callable<T>> supplier;

    public CallableSupplier(@NonNull Supplier<Callable<T>> supplier) {
        Objects.requireNonNull(supplier);
        this.supplier = supplier;
    }

    @Override
    public synchronized Callable<T> get() {
        return supplier.get();
    }
}
