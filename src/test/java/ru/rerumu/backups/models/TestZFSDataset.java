package ru.rerumu.backups.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;

import java.util.ArrayList;
import java.util.List;

public class TestZFSDataset {

    @Test
    void shouldGetBaseSnapshot() throws BaseSnapshotNotFoundException {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));

        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);
        Snapshot baseSnapshot = zfsDataset.getBaseSnapshot();
        Assertions.assertEquals(baseSnapshot, new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
    }

    @Test
    void shouldGetIncrementalSnapshotsUpper() throws SnapshotNotFoundException {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210108-150000"));
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);

        List<Snapshot> upperLimited = zfsDataset.getSnapshots("auto-20210107-150000");

        List<Snapshot> should = new ArrayList<>();
        should.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        should.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        should.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        should.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));

        Assertions.assertEquals(should, upperLimited);

    }

    @Test
    void shouldGetIncrementalSnapshotsLowerUpper() throws SnapshotNotFoundException {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210108-150000"));
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);

        List<Snapshot> upperLimited = zfsDataset.getSnapshots(
                "auto-20210106-150000",
                "auto-20210107-150000");

        List<Snapshot> should = new ArrayList<>();
        should.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        should.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));

        Assertions.assertEquals(should, upperLimited);

    }

    @Test
    void shouldNotFoundUpper() {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210108-150000"));
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);



        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsDataset.getSnapshots("ExternalPool/Applications@auto-20210107-1500001");
        });

    }

    @Test
    void shouldNotFoundLowerUpper1() {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210108-150000"));
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);

        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsDataset.getSnapshots(
                    "ExternalPool/Applications@auto-20210106-150000",
                    "ExternalPool/Applications@auto-20210107-1500008");
        });

    }

    @Test
    void shouldNotFoundLowerUpper2() {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210108-150000"));
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);

        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsDataset.getSnapshots(
                    "ExternalPool/Applications@auto-20210106-1500009",
                    "ExternalPool/Applications@auto-20210107-150000");
        });

    }

    @Test
    void shouldNotFoundLowerUpper3() {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210107-150000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210108-150000"));
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);

        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsDataset.getSnapshots(
                    "ExternalPool/Applications@auto-20210106-1500009",
                    "ExternalPool/Applications@auto-20210107-1500008");
        });

    }

    @Test
    void shouldNotFindBaseSnapshot() {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        ZFSDataset zfsDataset = new ZFSDataset(fileSystemName, snapshotList, EncryptionProperty.OFF);

        Assertions.assertThrows(BaseSnapshotNotFoundException.class, zfsDataset::getBaseSnapshot);

    }
}
