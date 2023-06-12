package ru.rerumu.zfs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.util.List;

public class TestDataset {

    @Test
    void shouldCreate() {


        Dataset dataset = new Dataset(
                "TestDataset",
                List.of(
                        new Snapshot("TestPool@tmp1"),
                        new Snapshot("TestPool@tmp2"),
                        new Snapshot("TestPool@tmp3")
                ));

        Assertions.assertEquals("TestDataset",dataset.name());
        Assertions.assertEquals(
                List.of(
                        new Snapshot("TestPool@tmp1"),
                        new Snapshot("TestPool@tmp2"),
                        new Snapshot("TestPool@tmp3")
                ),
                dataset.snapshotList()
        );
    }

}
