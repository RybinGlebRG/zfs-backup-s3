package ru.rerumu.zfs.factories.impl;

import ru.rerumu.zfs.consumers.GetDatasetStringStdConsumer;
import ru.rerumu.zfs.consumers.SnapshotListStdConsumer;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.function.Consumer;

public class StdConsumerFactoryImpl implements StdConsumerFactory {
    @Override
    public Consumer<BufferedInputStream> getDatasetStringStdConsumer(List<String> res) {
        return new GetDatasetStringStdConsumer(res);
    }

    @Override
    public Consumer<BufferedInputStream> getSnapshotListStdConsumer(List<Snapshot> snapshotList) {
        return new SnapshotListStdConsumer(snapshotList);
    }
}
