package ru.rerumu.zfs_backup_s3.utils.processes.factories.impl;

import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessFactory;

import java.io.IOException;
import java.util.List;

public final class ProcessFactoryMock implements ProcessFactory {
    @Override
    public Process create(List<String> args) throws IOException {
        return null;
    }
}
