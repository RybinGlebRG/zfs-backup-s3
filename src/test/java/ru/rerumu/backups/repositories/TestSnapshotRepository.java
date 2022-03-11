package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.SnapshotRepository;

import java.io.IOException;

public class TestSnapshotRepository {

    @Test
    void shouldGetLast() {
        Snapshot srcSnapshot = new Snapshot("MainPool@level_0_25_02_2020__20_50");
        SnapshotRepository snapshotRepository = new SnapshotRepository(srcSnapshot);
        Snapshot lastFullSnapshot = snapshotRepository.getLastFullSnapshot();

        Assertions.assertEquals(srcSnapshot,lastFullSnapshot);
    }
}
