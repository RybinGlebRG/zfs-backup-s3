package ru.rerumu.backups.services.zfs.impl.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.zfs.Dataset;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ListSnapshotStdConsumer implements Consumer<BufferedInputStream> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Dataset dataset;
    private final List<Snapshot> snapshotList;

    public ListSnapshotStdConsumer(Dataset dataset, List<Snapshot> snapshotList) {
        this.dataset = dataset;
        this.snapshotList = snapshotList;
    }

    @Override
    public void accept(BufferedInputStream stream) {
        try {
            byte[] output = stream.readAllBytes();
            String str = new String(output, StandardCharsets.UTF_8);
            logger.debug(String.format("Got from process: \n%s",str));
            String[] lines = str.split("\\n");
            Arrays.stream(lines)
                    .map(String::strip)
                    .map(Snapshot::new)
                    .filter(item->item.getDataset().equals(dataset.name()))
                    .peek(item -> logger.debug(String.format("Got snapshot: %s",item.getFullName())))
                    .forEach(snapshotList::add);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
