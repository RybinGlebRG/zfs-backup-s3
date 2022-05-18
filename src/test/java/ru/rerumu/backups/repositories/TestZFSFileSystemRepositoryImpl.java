package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.helpers.ZFSProcessFactoryTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestZFSFileSystemRepositoryImpl {

    @Test
    void shouldGetFileSystems() throws IOException, InterruptedException {
        ZFSProcessFactoryTest zfsProcessFactoryTest = new ZFSProcessFactoryTest();
        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactoryTest);
        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(
                zfsProcessFactoryTest,
                zfsSnapshotRepository);

        List<String> snapshots = new ArrayList<>();
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220326-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-060000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications@auto-20200321-173000");
        snapshots.add("ExternalPool/Applications@auto-20210106-060000");
        snapshots.add("ExternalPool/Applications@auto-20210106-150000");

        List<String> filesystems = new ArrayList<>();
        filesystems.add("ExternalPool/Applications");
        filesystems.add("ExternalPool/Applications/virtual_box");

        zfsProcessFactoryTest.setStringList(snapshots);
        zfsProcessFactoryTest.setFilesystems(filesystems);

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList("ExternalPool/Applications");



        List<Snapshot> src1 = new ArrayList<>();
        src1.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220326-150000"));
        src1.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-060000"));
        src1.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-150000"));

        List<Snapshot> src2 = new ArrayList<>();
        src2.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        src2.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        src2.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));

        List<ZFSFileSystem> src = new ArrayList<>();
        src.add(new ZFSFileSystem("ExternalPool/Applications",src2));
        src.add(new ZFSFileSystem("ExternalPool/Applications/virtual_box",src1));


        Assertions.assertEquals(src,zfsFileSystemList);
    }
}
