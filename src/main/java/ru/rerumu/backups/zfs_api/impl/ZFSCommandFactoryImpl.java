package ru.rerumu.backups.zfs_api.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.services.zfs.impl.CreateSnapshot;
import ru.rerumu.backups.services.zfs.impl.ListSnapshots;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.zfs_api.ZFSCommandFactory;

public class ZFSCommandFactoryImpl implements ZFSCommandFactory {

    private final ProcessWrapperFactory processWrapperFactory;
    private final ProcessFactory processFactory;

    public ZFSCommandFactoryImpl(ProcessWrapperFactory processWrapperFactory, ProcessFactory processFactory) {
        this.processWrapperFactory = processWrapperFactory;
        this.processFactory = processFactory;
    }

    //    @Override
//    public SnapshotCommand getSnapshotCommand(ZFSDataset dataset, String name, Boolean isRecursive) {
//        SnapshotCommandImpl.Builder builder = new SnapshotCommandImpl.Builder()
//                .dataset(dataset)
//                .name(name)
//                .processWrapperFactory(processWrapperFactory);
//        if (isRecursive){
//            builder.recursive();
//        }
//        return builder.build();
//    }

    @Override
    public CreateSnapshot getSnapshotCommand(Dataset dataset, String name, Boolean isRecursive) {
        if(isRecursive == null){
            throw new IllegalArgumentException();
        }
        CreateSnapshot.Builder builder= new CreateSnapshot.Builder()
                .dataset(dataset)
                .name(name)
                .processWrapperFactory(processWrapperFactory)
                ;
        if (isRecursive){
            builder.recursive();
        }
        return builder.build();
    }

    @Override
    public ListSnapshots getListSnapshotsCommand(Dataset dataset, Boolean isRecursive) {
        if (isRecursive){
            throw new IllegalArgumentException();
        }
        return new ListSnapshots(processFactory, dataset);
    }
}
