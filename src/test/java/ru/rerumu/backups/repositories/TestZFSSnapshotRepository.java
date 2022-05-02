package ru.rerumu.backups.repositories;

import junit.framework.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.services.ZFSProcessFactoryTest;
import ru.rerumu.backups.services.impl.ZFSProcessFactoryImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestZFSSnapshotRepository {
    @Test
    void shouldGetShort() throws IOException, InterruptedException {
        ZFSProcessFactoryTest zfsProcessFactoryTest = new ZFSProcessFactoryTest();
        List<String> snapshots = new ArrayList<>();
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220326-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-060000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications@auto-20200321-173000");
        snapshots.add("ExternalPool/Applications@auto-20210106-060000");
        snapshots.add("ExternalPool/Applications@auto-20210106-150000");
        zfsProcessFactoryTest.setStringList(snapshots);
        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactoryTest);
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem("ExternalPool/Applications");
        List<Snapshot> dst = zfsSnapshotRepository.getAllSnapshotsOrdered(zfsFileSystem);

        List<Snapshot> src = new ArrayList<>();
        src.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        src.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        src.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));

        Assertions.assertEquals(src,dst);

    }

    @Test
    void shouldGetLong() throws IOException, InterruptedException {
        ZFSProcessFactoryTest zfsProcessFactoryTest = new ZFSProcessFactoryTest();
        List<String> snapshots = new ArrayList<>();
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220326-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-060000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220328-150000");
        snapshots.add("ExternalPool/Applications@auto-20200321-173000");
        snapshots.add("ExternalPool/Applications@auto-20210106-060000");
        snapshots.add("ExternalPool/Applications@auto-20210106-150000");
        zfsProcessFactoryTest.setStringList(snapshots);
        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactoryTest);
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem("ExternalPool/Applications/virtual_box");
        List<Snapshot> dst = zfsSnapshotRepository.getAllSnapshotsOrdered(zfsFileSystem);

        List<Snapshot> src = new ArrayList<>();
        src.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220326-150000"));
        src.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-060000"));
        src.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-150000"));
        src.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220328-150000"));

        Assertions.assertEquals(src,dst);

    }
}
