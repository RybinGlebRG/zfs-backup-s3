package ru.rerumu.zfs.factories;

import ru.rerumu.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.function.Consumer;

public interface StdConsumerFactory {

    Consumer<BufferedInputStream> getSendStdoutConsumer(
            S3StreamRepositoryImpl s3StreamRepository,
            String prefix
    );

    Consumer<BufferedInputStream> getDatasetStringStdConsumer(
            List<String> res
    );

    Consumer<BufferedInputStream> getSnapshotListStdConsumer(
            List<Snapshot> snapshotList
    );
}
