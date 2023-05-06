package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.factories.ZFSServiceFactory;
import ru.rerumu.backups.services.zfs.impl.ZFSServiceImpl;
import ru.rerumu.backups.utils.processes.factories.ProcessFactory;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.backups.utils.processes.factories.impl.ProcessFactoryImpl;
import ru.rerumu.backups.utils.processes.factories.impl.ProcessWrapperFactoryImpl;
import ru.rerumu.backups.utils.processes.factories.impl.StdProcessorFactoryImpl;

import java.util.concurrent.ExecutorService;

public class ZFSServiceFactoryImpl implements ZFSServiceFactory {
    @Override
    public ZFSService getZFSService() {
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl();
        StdProcessorFactory stdProcessorFactory = new StdProcessorFactoryImpl();
        ProcessFactory processFactory = new ProcessFactoryImpl();
        ProcessWrapperFactory processWrapperFactory = new ProcessWrapperFactoryImpl(processFactory);
        ZFSCallableFactory zfsCallableFactory = new ZFSCallableFactoryImpl(
                processWrapperFactory,
                stdConsumerFactory,
                stdProcessorFactory
        );
        return new ZFSServiceImpl(zfsCallableFactory);
    }
}
