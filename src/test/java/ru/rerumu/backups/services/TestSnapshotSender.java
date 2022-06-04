package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.io.ZFSFileWriter;
import ru.rerumu.backups.io.ZFSFileWriterFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.impl.SnapshotSenderImpl;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class TestSnapshotSender {

    @Test
    void shouldSendOneFile() throws CompressorException, IOException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException, NoSuchAlgorithmException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return Paths.get((String)args[0]+".part"+(int)args[1]);
        });
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory);
        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
        snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true);


        InOrder inOrder = Mockito.inOrder(filePartRepository, zfsFileWriter, s3Loader, zfsSend);

        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part0"));

    }

    @Test
    void shouldSendTwoFiles() throws CompressorException, IOException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException, NoSuchAlgorithmException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt()))
                .thenAnswer(invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    return Paths.get((String)args[0]+".part"+(int)args[1]);
                });
        Mockito.doThrow(new FileHitSizeLimitException())
                .doThrow(new ZFSStreamEndedException())
                .when(zfsFileWriter).write(Mockito.any(), Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory);
        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
        snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true);


        InOrder inOrder = Mockito.inOrder(s3Loader);

        Mockito.verify(s3Loader, Mockito.times(2)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part0"));
        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part1"));

    }


    @Test
    void shouldSendIncremental() throws IOException, FileHitSizeLimitException, CompressorException, ZFSStreamEndedException, EncryptException, InterruptedException, NoSuchAlgorithmException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(),Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return Paths.get((String)args[0]+".part"+(int)args[1]);
        });
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory);
        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
        Snapshot incrementalSnapshot = new Snapshot("ExternalPool/Applications@auto-20220327-150000");
        snapshotSender.sendIncrementalSnapshot(baseSnapshot,incrementalSnapshot, s3Loader, true);

        InOrder inOrder = Mockito.inOrder(s3Loader);

        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload("ExternalPool-Applications",
                Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0")
        );


    }

    @Test
    void shouldThrowExceptionBase() throws IOException, CompressorException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.doThrow(new IOException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory);
        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");

        Assertions.assertThrows(Exception.class,()->snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true));

        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsSend);

        inOrder.verify(zfsProcessFactory).getZFSSendFull(Mockito.any());
        inOrder.verify(zfsSend).kill();
        inOrder.verify(zfsSend).close();
    }

    @Test
    void shouldThrowExceptionBaseIncr() throws IOException, CompressorException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(),Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.doThrow(new IOException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory);
        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
        Snapshot incrementalSnapshot = new Snapshot("ExternalPool/Applications@auto-20220327-150000");

        Assertions.assertThrows(Exception.class,()->snapshotSender.sendIncrementalSnapshot(baseSnapshot, incrementalSnapshot,s3Loader, true));

        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsSend);

        inOrder.verify(zfsProcessFactory).getZFSSendIncremental(Mockito.any(),Mockito.any());
        inOrder.verify(zfsSend).kill();
        inOrder.verify(zfsSend).close();
    }
}
