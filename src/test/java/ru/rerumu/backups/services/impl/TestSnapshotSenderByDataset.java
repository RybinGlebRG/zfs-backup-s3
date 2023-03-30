package ru.rerumu.backups.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.zfs_api.zfs.ZFSSend;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestSnapshotSenderByDataset {

    @Test
    void shouldSendBase()
            throws IOException,
            FileHitSizeLimitException,
            CompressorException,
            ZFSStreamEndedException,
            EncryptException,
            NoSuchAlgorithmException,
            InterruptedException,
            IncorrectHashException,
            S3MissesFileException,
            ExecutionException {
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
        Path tempDir = Paths.get("/tst");

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter(Mockito.any())).thenReturn(zfsFileWriter);
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderByDataset(
                localBackupRepository, zfsProcessFactory, zfsFileWriterFactory,tempDir
        );
        snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000")
        ));


        InOrder inOrder = Mockito.inOrder(localBackupRepository,zfsProcessFactory,zfsFileWriterFactory);

        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendFull(Mockito.any());
        Mockito.verify(zfsFileWriterFactory, Mockito.times(1))
                .getZFSFileWriter(Mockito.any());
        Mockito.verify(localBackupRepository, Mockito.times(1))
                .add(Mockito.any(), Mockito.any(),Mockito.any());

        inOrder.verify(zfsProcessFactory)
                .getZFSSendFull(new Snapshot("ExternalPool/Applications@auto-20220326-150000"));
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0"));
        inOrder.verify(localBackupRepository)
                .add(
                        "ExternalPool-Applications",
                        "ExternalPool-Applications@auto-20220326-150000.part0",
                        tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0")
                );

    }

    @Test
    void shouldSendBaseAndIncremental()
            throws IOException,
            FileHitSizeLimitException,
            CompressorException,
            ZFSStreamEndedException,
            EncryptException,
            NoSuchAlgorithmException,
            InterruptedException,
            IncorrectHashException,
            S3MissesFileException,
            ExecutionException {
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSSend zfsSendIncremental = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
        Path tempDir = Paths.get("/tst");


        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(),Mockito.any())).thenReturn(zfsSendIncremental);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter(Mockito.any())).thenReturn(zfsFileWriter);
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderByDataset(
                localBackupRepository, zfsProcessFactory, zfsFileWriterFactory,tempDir
        );
        snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220328-150000")
        ));


        InOrder inOrder = Mockito.inOrder(localBackupRepository,zfsProcessFactory,zfsFileWriterFactory);

        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendFull(Mockito.any());
        Mockito.verify(zfsFileWriterFactory, Mockito.times(2))
                .getZFSFileWriter(Mockito.any());
        Mockito.verify(localBackupRepository, Mockito.times(2))
                .add(Mockito.any(), Mockito.any(),Mockito.any());
        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendIncremental(Mockito.any(), Mockito.any());

        inOrder.verify(zfsProcessFactory)
                .getZFSSendFull(new Snapshot("ExternalPool/Applications@auto-20220326-150000"));
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0"));
        inOrder.verify(localBackupRepository)
                .add(
                        "ExternalPool-Applications",
                        "ExternalPool-Applications@auto-20220326-150000.part0",
                        tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0")
                );
        inOrder.verify(zfsProcessFactory)
                .getZFSSendIncremental(
                        new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                        new Snapshot("ExternalPool/Applications@auto-20220328-150000")
                );
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(
                        tempDir.resolve(
                                "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0"
                        ));
        inOrder.verify(localBackupRepository)
                .add(
                        "ExternalPool-Applications",
                        "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0",
                        tempDir.resolve("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0")
                );
    }

    @Test
    void shouldSendMultipart() throws IOException, FileHitSizeLimitException, CompressorException, ZFSStreamEndedException, EncryptException, S3MissesFileException, NoSuchAlgorithmException, ExecutionException, InterruptedException, IncorrectHashException {
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
        Path tempDir = Paths.get("/tst");

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter(Mockito.any())).thenReturn(zfsFileWriter);
        Mockito.doThrow(new FileHitSizeLimitException())
                .doThrow(new ZFSStreamEndedException())
                .when(zfsFileWriter).write(Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderByDataset(
                localBackupRepository, zfsProcessFactory, zfsFileWriterFactory,tempDir
        );
        snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000")
        ));


        InOrder inOrder = Mockito.inOrder(localBackupRepository,zfsProcessFactory,zfsFileWriterFactory);

        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendFull(Mockito.any());
        Mockito.verify(zfsFileWriterFactory, Mockito.times(2))
                .getZFSFileWriter(Mockito.any());
        Mockito.verify(localBackupRepository, Mockito.times(2))
                .add(Mockito.any(), Mockito.any(),Mockito.any());

        inOrder.verify(zfsProcessFactory)
                .getZFSSendFull(new Snapshot("ExternalPool/Applications@auto-20220326-150000"));
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0"));
        inOrder.verify(localBackupRepository)
                .add(
                        "ExternalPool-Applications",
                        "ExternalPool-Applications@auto-20220326-150000.part0",
                        tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0")
                );
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part1"));
        inOrder.verify(localBackupRepository)
                .add(
                        "ExternalPool-Applications",
                        "ExternalPool-Applications@auto-20220326-150000.part1",
                        tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part1")
                );
    }

    @Test
    void shouldSendIncremental()
            throws IOException,
            FileHitSizeLimitException,
            CompressorException,
            ZFSStreamEndedException,
            EncryptException,
            NoSuchAlgorithmException,
            InterruptedException,
            IncorrectHashException,
            S3MissesFileException,
            ExecutionException {
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSendIncremental = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
        Path tempDir = Paths.get("/tst");


        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(), Mockito.any())).thenReturn(zfsSendIncremental);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter(Mockito.any())).thenReturn(zfsFileWriter);
        Mockito.doThrow(new ZFSStreamEndedException())
                .when(zfsFileWriter).write(Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderByDataset(
                localBackupRepository,  zfsProcessFactory, zfsFileWriterFactory,tempDir
        );
        snapshotSender.sendStartingFromIncremental("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220328-150000")
        ));


        InOrder inOrder = Mockito.inOrder(localBackupRepository,zfsProcessFactory,zfsFileWriterFactory);

        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendIncremental(Mockito.any(), Mockito.any());
        Mockito.verify(zfsFileWriterFactory, Mockito.times(1))
                .getZFSFileWriter(Mockito.any());
        Mockito.verify(localBackupRepository, Mockito.times(1))
                .add(Mockito.any(), Mockito.any(),Mockito.any());

        inOrder.verify(zfsProcessFactory)
                .getZFSSendIncremental(
                        new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                        new Snapshot("ExternalPool/Applications@auto-20220328-150000")
                );
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(
                        tempDir.resolve(
                                "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0"
                        ));
        inOrder.verify(localBackupRepository)
                .add(
                        "ExternalPool-Applications",
                        "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0",
                        tempDir.resolve("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0")
                );
    }

    @Test
    void shouldThrowException() throws Exception{
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSendFull = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
        Path tempDir = Paths.get("/tst");

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSendFull);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter(Mockito.any())).thenReturn(zfsFileWriter);
        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenThrow(new IOException());

        SnapshotSender snapshotSender = new SnapshotSenderByDataset(
                localBackupRepository, zfsProcessFactory, zfsFileWriterFactory,tempDir
        );

        Assertions.assertThrows(Exception.class,()->{
            snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                    new Snapshot("ExternalPool/Applications@auto-20220326-150000")
            ));
        });

        InOrder inOrder = Mockito.inOrder(localBackupRepository,zfsProcessFactory,zfsFileWriterFactory,zfsSendFull);

        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendFull(Mockito.any());
        Mockito.verify(zfsFileWriterFactory, Mockito.never())
                .getZFSFileWriter(Mockito.any());
        Mockito.verify(localBackupRepository, Mockito.never())
                .add(Mockito.any(), Mockito.any(),Mockito.any());
        Mockito.verify(zfsSendFull, Mockito.never())
                .kill();
        Mockito.verify(zfsSendFull, Mockito.never())
                .close();

        inOrder.verify(zfsProcessFactory)
                .getZFSSendFull(new Snapshot("ExternalPool/Applications@auto-20220326-150000"));
    }

    @Test
    void shouldThrowExceptionLater() throws Exception{
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSendFull = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Path tempDir = Paths.get("/tst");


        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSendFull);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter(Mockito.any())).thenReturn(zfsFileWriter);
        Mockito.doThrow(new IOException())
                .when(zfsFileWriter).write(Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderByDataset(
                localBackupRepository, zfsProcessFactory, zfsFileWriterFactory,tempDir
        );

        Assertions.assertThrows(Exception.class,()->{
            snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                    new Snapshot("ExternalPool/Applications@auto-20220326-150000")
            ));
        });


        InOrder inOrder = Mockito.inOrder(localBackupRepository,zfsProcessFactory,zfsFileWriterFactory,zfsSendFull);

        Mockito.verify(zfsProcessFactory, Mockito.times(1))
                .getZFSSendFull(Mockito.any());
        Mockito.verify(zfsFileWriterFactory, Mockito.times(1))
                .getZFSFileWriter(Mockito.any());
        Mockito.verify(localBackupRepository, Mockito.never())
                .add(Mockito.any(), Mockito.any(),Mockito.any());
        Mockito.verify(zfsSendFull, Mockito.times(1))
                .kill();
        Mockito.verify(zfsSendFull, Mockito.times(1))
                .close();

        inOrder.verify(zfsProcessFactory)
                .getZFSSendFull(new Snapshot("ExternalPool/Applications@auto-20220326-150000"));
        inOrder.verify(zfsFileWriterFactory)
                .getZFSFileWriter(tempDir.resolve("ExternalPool-Applications@auto-20220326-150000.part0"));
        inOrder.verify(zfsSendFull)
                .kill();
        inOrder.verify(zfsSendFull)
                .close();
    }
}
