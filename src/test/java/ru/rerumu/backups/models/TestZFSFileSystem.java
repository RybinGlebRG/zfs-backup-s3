package ru.rerumu.backups.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class TestZFSFileSystem {

    @Test
    void shouldGetBaseSnapshot() throws BaseSnapshotNotFoundException {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));

        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);
        Snapshot baseSnapshot = zfsFileSystem.getBaseSnapshot();
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
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);

        List<Snapshot> upperLimited = zfsFileSystem.getIncrementalSnapshots("ExternalPool/Applications@auto-20210107-150000");

        List<Snapshot> should = new ArrayList<>();
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
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);

        List<Snapshot> upperLimited = zfsFileSystem.getIncrementalSnapshots(
                "ExternalPool/Applications@auto-20210106-150000",
                "ExternalPool/Applications@auto-20210107-150000");

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
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);



        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsFileSystem.getIncrementalSnapshots("ExternalPool/Applications@auto-20210107-1500001");
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
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);

        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsFileSystem.getIncrementalSnapshots(
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
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);

        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsFileSystem.getIncrementalSnapshots(
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
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);

        Assertions.assertThrows(SnapshotNotFoundException.class, () -> {
            zfsFileSystem.getIncrementalSnapshots(
                    "ExternalPool/Applications@auto-20210106-1500009",
                    "ExternalPool/Applications@auto-20210107-1500008");
        });

    }

    @Test
    void shouldNotFindBaseSnapshot() {
        String fileSystemName = "ExternalPool/Applications";
        List<Snapshot> snapshotList = new ArrayList<>();
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(fileSystemName, snapshotList);

        Assertions.assertThrows(BaseSnapshotNotFoundException.class, zfsFileSystem::getBaseSnapshot);

    }


}
