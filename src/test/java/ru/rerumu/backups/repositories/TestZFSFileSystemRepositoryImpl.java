package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSGetDatasetProperty;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestZFSFileSystemRepositoryImpl {

    @Test
    void shouldGetFileSystems() throws IOException, InterruptedException, ExecutionException {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSSnapshotRepository zfsSnapshotRepository = Mockito.mock(ZFSSnapshotRepository.class);
        ProcessWrapper zfsListFilesystems = Mockito.mock(ProcessWrapper.class);
        ZFSGetDatasetProperty zfsProperty = Mockito.mock(ZFSGetDatasetProperty.class);

        // Datasets
        Mockito.when(zfsProcessFactory.getZFSListFilesystems("ExternalPool/Applications"))
                .thenReturn(zfsListFilesystems);
        Mockito.when(zfsListFilesystems.getBufferedInputStream())
                .thenAnswer(invocationOnMock -> {
                    String tmp = "ExternalPool/Applications" + "\n"
                            + "ExternalPool/Applications/virtual_box" + "\n";
                    byte[] buf = tmp.getBytes(StandardCharsets.UTF_8);
                    return new BufferedInputStream(new ByteArrayInputStream(buf));
                });

        // Properties
        Mockito.when(zfsProcessFactory.getZFSGetDatasetProperty(
                "ExternalPool/Applications", "encryption"
                ))
                .thenReturn(zfsProperty);
        Mockito.when(zfsProcessFactory.getZFSGetDatasetProperty(
                                "ExternalPool/Applications/virtual_box",
                                "encryption"
                ))
                .thenReturn(zfsProperty);
        Mockito.when(zfsProperty.getBufferedInputStream())
                .thenAnswer(invocationOnMock -> {
                    String tmp = "off" + "\n";
                    byte[] buf = tmp.getBytes(StandardCharsets.UTF_8);
                    return new BufferedInputStream(new ByteArrayInputStream(buf));
                });

        // Snapshots
        Mockito.when(zfsSnapshotRepository.getAllSnapshotsOrdered("ExternalPool/Applications"))
                .thenAnswer(invocationOnMock -> {
                    List<Snapshot> snapshotList = new ArrayList<>();
                    snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
                    snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
                    snapshotList.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));
                    return snapshotList;
                });
        Mockito.when(zfsSnapshotRepository.getAllSnapshotsOrdered("ExternalPool/Applications/virtual_box"))
                .thenAnswer(invocationOnMock -> {
                    List<Snapshot> snapshotList = new ArrayList<>();
                    snapshotList.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220326-150000"));
                    snapshotList.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-060000"));
                    snapshotList.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-150000"));
                    return snapshotList;
                });

        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(
                zfsProcessFactory,
                zfsSnapshotRepository);

        List<ZFSDataset> zfsDatasetList = zfsFileSystemRepository.getFilesystemsTreeList("ExternalPool/Applications");


        List<Snapshot> src1 = new ArrayList<>();
        src1.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220326-150000"));
        src1.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-060000"));
        src1.add(new Snapshot("ExternalPool/Applications/virtual_box@auto-20220327-150000"));

        List<Snapshot> src2 = new ArrayList<>();
        src2.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        src2.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        src2.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));

        List<ZFSDataset> src = new ArrayList<>();
        src.add(new ZFSDataset("ExternalPool/Applications", src2));
        src.add(new ZFSDataset("ExternalPool/Applications/virtual_box", src1));


        Assertions.assertEquals(src, zfsDatasetList);
    }
}
