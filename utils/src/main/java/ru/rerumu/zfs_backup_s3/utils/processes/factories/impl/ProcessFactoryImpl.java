package ru.rerumu.zfs_backup_s3.utils.processes.factories.impl;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessFactory;

import java.io.IOException;
import java.util.List;

@ThreadSafe
public final class ProcessFactoryImpl implements ProcessFactory {
    @Override
    public Process create(List<String> args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        return pb.start();
    }
}
