package ru.rerumu.zfs_backup_s3.utils;

import java.util.concurrent.Callable;

@NotThreadSafe
public abstract class CallableOnlyOnce<T> implements Callable<T> {

    private boolean isCalled = false;

    @Synchronized
    protected abstract T callOnce() throws Exception;

    @Synchronized
    public synchronized T call() throws Exception {
        if (!isCalled) {
            isCalled = true;
            return callOnce();
        } else {
            throw new CalledMoreThanOnceException();
        }
    }
}
