package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.impl.ProcessWrapperImpl;

import java.util.List;

public class ProcessWrapperFactoryImpl implements ProcessWrapperFactory {

    @Override
    public ProcessWrapper getProcessWrapper(List<String> args) {
        return new ProcessWrapperImpl(args);
    }
}
