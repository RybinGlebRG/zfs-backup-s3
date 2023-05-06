package ru.rerumu.zfs.factories.impl;

import ru.rerumu.utils.processes.factories.ProcessFactory;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.utils.processes.factories.impl.ProcessFactoryImpl;
import ru.rerumu.utils.processes.factories.impl.ProcessWrapperFactoryImpl;
import ru.rerumu.utils.processes.factories.impl.StdProcessorFactoryImpl;
import ru.rerumu.zfs.ZFSService;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs.factories.ZFSServiceFactory;
import ru.rerumu.zfs.impl.ZFSServiceImpl;

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
