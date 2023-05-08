package ru.rerumu.zfs;

import ru.rerumu.utils.processes.factories.ProcessFactory;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.impl.ProcessFactoryImpl;
import ru.rerumu.utils.processes.factories.impl.ProcessWrapperFactoryImpl;
import ru.rerumu.zfs.impl.ZFSServiceImpl;
import ru.rerumu.zfs.services.SnapshotService;
import ru.rerumu.zfs.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.zfs.factories.impl.ZFSCallableFactoryImpl;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs.services.impl.SnapshotServiceImpl;

public class ZFSServiceFactoryImpl implements ZFSServiceFactory {
    @Override
    public ZFSService getZFSService() {
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl();
        ProcessFactory processFactory = new ProcessFactoryImpl();
        ProcessWrapperFactory processWrapperFactory = new ProcessWrapperFactoryImpl(processFactory);
        ZFSCallableFactory zfsCallableFactory = new ZFSCallableFactoryImpl(
                processWrapperFactory,
                stdConsumerFactory
        );
        SnapshotService snapshotService = new SnapshotServiceImpl(zfsCallableFactory);
        return new ZFSServiceImpl(zfsCallableFactory, snapshotService);
    }
}
