package ru.rerumu.backups.zfs_api.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.zfs_api.ZFSCommandFactory;
import ru.rerumu.backups.zfs_api.zfs.ListSnapshotsCommand;
import ru.rerumu.backups.zfs_api.zfs.SnapshotCommand;
import ru.rerumu.backups.zfs_api.zfs.impl.ListSnapshotsCommandImpl;
import ru.rerumu.backups.zfs_api.zfs.impl.SnapshotCommandImpl;

public class ZFSCommandFactoryImpl implements ZFSCommandFactory {

    private final ProcessWrapperFactory processWrapperFactory;

    public ZFSCommandFactoryImpl(ProcessWrapperFactory processWrapperFactory) {
        this.processWrapperFactory = processWrapperFactory;
    }

    @Override
    public SnapshotCommand getSnapshotCommand(ZFSDataset dataset, String name, Boolean isRecursive) {
        SnapshotCommandImpl.Builder builder = new SnapshotCommandImpl.Builder()
                .dataset(dataset)
                .name(name)
                .processWrapperFactory(processWrapperFactory);
        if (isRecursive){
            builder.recursive();
        }
        return builder.build();
    }

    @Override
    public ListSnapshotsCommand getListSnapshotsCommand(ZFSDataset dataset) {
        return new ListSnapshotsCommandImpl(dataset, processWrapperFactory);
    }
}
