package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.zfs.ZFSListSnapshots;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestZFSSnapshotRepository {
    @Test
    void shouldGetShort() throws IOException, InterruptedException, ExecutionException {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSListSnapshots zfsListSnapshots = Mockito.mock(ZFSListSnapshots.class);

        Mockito.when(zfsProcessFactory.getZFSListSnapshots("ExternalPool/Applications"))
                .thenReturn(zfsListSnapshots);
        Mockito.when(zfsListSnapshots.getBufferedInputStream())
                .thenAnswer(invocationOnMock -> {
                   String tmp =  "ExternalPool/Applications@auto-20200321-173000"+"\n"
                           +"ExternalPool/Applications@auto-20210106-060000"+"\n"
                           +"ExternalPool/Applications@auto-20210106-150000"+"\n";
                    byte[] buf = tmp.getBytes(StandardCharsets.UTF_8);
                    return new BufferedInputStream(new ByteArrayInputStream(buf));
                });

        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
        List<Snapshot> dst = zfsSnapshotRepository.getAllSnapshotsOrdered("ExternalPool/Applications");

        List<Snapshot> src = new ArrayList<>();
        src.add(new Snapshot("ExternalPool/Applications@auto-20200321-173000"));
        src.add(new Snapshot("ExternalPool/Applications@auto-20210106-060000"));
        src.add(new Snapshot("ExternalPool/Applications@auto-20210106-150000"));

        Assertions.assertEquals(src,dst);

    }
}
