package ru.rerumu.backups.services.zfs.impl.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class SnapshotListStdConsumer implements TriConsumer<BufferedInputStream,Runnable,Runnable> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<Snapshot> snapshotList;

    public SnapshotListStdConsumer(List<Snapshot> snapshotList) {
        this.snapshotList = snapshotList;
    }

    @Override
    public void accept(BufferedInputStream bufferedInputStream, Runnable close, Runnable kill) {
        try {
            byte[] output = bufferedInputStream.readAllBytes();
            String str = new String(output, StandardCharsets.UTF_8);
            logger.debug(String.format("Got from process: \n%s",str));
            String[] lines = str.split("\\n");

             Arrays.stream(lines)
                    .map(String::strip)
                    .map(Snapshot::new)
                    .peek(item -> logger.debug(String.format("Got snapshot: %s",item.getFullName())))
                     .forEach(snapshotList::add);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            kill.run();
            throw new RuntimeException(e);
        }
    }
}
