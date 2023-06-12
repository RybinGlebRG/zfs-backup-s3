package ru.rerumu.zfs_backup_s3.zfs;

import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessFactoryImpl;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessWrapperFactoryImpl;
import ru.rerumu.zfs_backup_s3.zfs.impl.ZFSServiceImpl;
import ru.rerumu.zfs_backup_s3.zfs.services.SnapshotService;
import ru.rerumu.zfs_backup_s3.zfs.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.zfs_backup_s3.zfs.factories.impl.ZFSCallableFactoryImpl;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.services.impl.SnapshotServiceImpl;

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
