package ru.rerumu.zfs.factories;

import ru.rerumu.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.function.Consumer;

public interface StdConsumerFactory {

    Consumer<BufferedInputStream> getDatasetStringStdConsumer(
            List<String> res
    );

    Consumer<BufferedInputStream> getSnapshotListStdConsumer(
            List<Snapshot> snapshotList
    );
}
