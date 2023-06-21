package ru.rerumu.zfs_backup_s3.utils.processes.factories;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessFactoryImpl;

import java.io.IOException;
import java.util.List;

@ThreadSafe
public sealed interface ProcessFactory permits ProcessFactoryImpl {

    Process create(List<String> args) throws IOException;
}
