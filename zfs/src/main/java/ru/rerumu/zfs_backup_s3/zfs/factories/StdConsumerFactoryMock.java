package ru.rerumu.zfs_backup_s3.zfs.factories;

import ru.rerumu.zfs_backup_s3.utils.Generated;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.function.Consumer;

@Generated
public final class StdConsumerFactoryMock implements StdConsumerFactory {
    @Override
    public Consumer<BufferedInputStream> getDatasetStringStdConsumer(List<String> res) {
        return null;
    }

    @Override
    public Consumer<BufferedInputStream> getSnapshotListStdConsumer(List<Snapshot> snapshotList) {
        return null;
    }
}
