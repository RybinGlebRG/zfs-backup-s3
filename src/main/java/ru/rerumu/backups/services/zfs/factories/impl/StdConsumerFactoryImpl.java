package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.zfs.consumers.GetDatasetStringStdConsumer;
import ru.rerumu.backups.services.zfs.consumers.SnapshotListStdConsumer;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.consumers.SendStdoutConsumer;
import ru.rerumu.backups.services.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.function.Consumer;

public class StdConsumerFactoryImpl implements StdConsumerFactory {
    @Override
    public Consumer<BufferedInputStream> getSendStdoutConsumer(S3StreamRepositoryImpl s3StreamRepository, String prefix) {
        return new SendStdoutConsumer(s3StreamRepository,prefix);
    }

    @Override
    public Consumer<BufferedInputStream> getDatasetStringStdConsumer(List<String> res) {
        return new GetDatasetStringStdConsumer(res);
    }

    @Override
    public Consumer<BufferedInputStream> getSnapshotListStdConsumer(List<Snapshot> snapshotList) {
        return new SnapshotListStdConsumer(snapshotList);
    }
}
