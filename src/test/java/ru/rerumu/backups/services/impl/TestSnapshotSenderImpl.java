package ru.rerumu.backups.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.services.S3Loader;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.services.ZFSFileWriterFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestSnapshotSenderImpl {

//    @Test
//    void shouldSendOneFile() throws CompressorException, IOException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
//        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
//        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
//        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
//
//        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
//        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
//        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
//            Object[] args = invocationOnMock.getArguments();
//            return Paths.get((String)args[0]+".part"+(int)args[1]);
//        });
//        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
//
//        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
//        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
//        snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true);
//
//
//        InOrder inOrder = Mockito.inOrder(filePartRepository, zfsFileWriter, s3Loader, zfsSend);
//
//        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
//        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part0"));
//
//    }
//
//    @Test
//    void shouldSendTwoFiles() throws CompressorException, IOException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
//        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
//        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
//        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
//
//        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
//        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
//        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt()))
//                .thenAnswer(invocationOnMock -> {
//                    Object[] args = invocationOnMock.getArguments();
//                    return Paths.get((String)args[0]+".part"+(int)args[1]);
//                });
//        Mockito.doThrow(new FileHitSizeLimitException())
//                .doThrow(new ZFSStreamEndedException())
//                .when(zfsFileWriter).write(Mockito.any(), Mockito.any());
//
//        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
//        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
//        snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true);
//
//
//        InOrder inOrder = Mockito.inOrder(s3Loader);
//
//        Mockito.verify(s3Loader, Mockito.times(2)).upload(Mockito.any(), Mockito.any());
//        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part0"));
//        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part1"));
//
//    }
//
//
//    @Test
//    void shouldSendIncremental() throws IOException, FileHitSizeLimitException, CompressorException, ZFSStreamEndedException, EncryptException, InterruptedException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
//        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
//        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
//        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
//
//        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(),Mockito.any())).thenReturn(zfsSend);
//        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
//        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
//            Object[] args = invocationOnMock.getArguments();
//            return Paths.get((String)args[0]+".part"+(int)args[1]);
//        });
//        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
//
//        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
//        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
//        Snapshot incrementalSnapshot = new Snapshot("ExternalPool/Applications@auto-20220327-150000");
//        snapshotSender.sendIncrementalSnapshot(baseSnapshot,incrementalSnapshot, s3Loader, true);
//
//        InOrder inOrder = Mockito.inOrder(s3Loader);
//
//        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
//        inOrder.verify(s3Loader).upload("ExternalPool-Applications",
//                Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0")
//        );
//
//
//    }
//
//    @Test
//    void shouldThrowExceptionBase() throws IOException, CompressorException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException, ExecutionException {
//        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
//        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
//        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
//
//        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
//        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
//        Mockito.doThrow(new IOException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
//
//        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
//        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
//
//        Assertions.assertThrows(Exception.class,()->snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true));
//
//        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsSend);
//
//        inOrder.verify(zfsProcessFactory).getZFSSendFull(Mockito.any());
//        inOrder.verify(zfsSend).kill();
//        inOrder.verify(zfsSend).close();
//    }
//
//    @Test
//    void shouldThrowExceptionBaseIncr() throws IOException, CompressorException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException, ExecutionException {
//        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
//        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
//        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
//
//        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(),Mockito.any())).thenReturn(zfsSend);
//        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
//        Mockito.doThrow(new IOException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
//
//        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
//        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
//        Snapshot incrementalSnapshot = new Snapshot("ExternalPool/Applications@auto-20220327-150000");
//
//        Assertions.assertThrows(Exception.class,()->snapshotSender.sendIncrementalSnapshot(baseSnapshot, incrementalSnapshot,s3Loader, true));
//
//        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsSend);
//
//        inOrder.verify(zfsProcessFactory).getZFSSendIncremental(Mockito.any(),Mockito.any());
//        inOrder.verify(zfsSend).kill();
//        inOrder.verify(zfsSend).close();
//    }

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
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSSend zfsSendIncremental = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return Paths.get("/tmp/"+(String)args[0]+".part"+(int)args[1]);
        });
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
        Mockito.when(s3Loader.objectsListForDataset("ExternalPool-Applications")).thenReturn(
                List.of("ExternalPool-Applications@auto-20220326-150000.part0")
        );

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
        snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000")
        ));


        InOrder inOrder = Mockito.inOrder( s3Loader);

        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220326-150000.part0")
        );
        inOrder.verify(s3Loader,Mockito.times(1)).objectsListForDataset("ExternalPool-Applications");
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
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSSend zfsSendIncremental = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(),Mockito.any())).thenReturn(zfsSendIncremental);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return Paths.get("/tmp/"+(String)args[0]+".part"+(int)args[1]);
        });
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
        Mockito.when(s3Loader.objectsListForDataset("ExternalPool-Applications")).thenReturn(
                List.of(
                        "ExternalPool-Applications@auto-20220326-150000.part0",
                        "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0",
                        "ExternalPool-Applications@auto-20220327-150000__ExternalPool-Applications@auto-20220328-150000.part0"
                )
        );

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
        snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220328-150000")
        ));


        InOrder inOrder = Mockito.inOrder( s3Loader);

        Mockito.verify(s3Loader, Mockito.times(3)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220326-150000.part0")
        );
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0")
        );
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220327-150000__ExternalPool-Applications@auto-20220328-150000.part0")
        );
        inOrder.verify(s3Loader,Mockito.times(1)).objectsListForDataset("ExternalPool-Applications");
    }

    @Test
    void shouldSendMultipart() throws IOException, FileHitSizeLimitException, CompressorException, ZFSStreamEndedException, EncryptException, S3MissesFileException, NoSuchAlgorithmException, ExecutionException, InterruptedException, IncorrectHashException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSSend zfsSendIncremental = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return Paths.get("/tmp/"+(String)args[0]+".part"+(int)args[1]);
        });
        Mockito.doThrow(new FileHitSizeLimitException())
                .doThrow(new ZFSStreamEndedException())
                .when(zfsFileWriter).write(Mockito.any(), Mockito.any());
        Mockito.when(s3Loader.objectsListForDataset("ExternalPool-Applications")).thenReturn(
                List.of(
                        "ExternalPool-Applications@auto-20220326-150000.part0",
                        "ExternalPool-Applications@auto-20220326-150000.part1"
                )
        );

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
        snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000")
        ));


        InOrder inOrder = Mockito.inOrder( s3Loader);

        Mockito.verify(s3Loader, Mockito.times(2)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220326-150000.part0")
        );
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220326-150000.part1")
        );
        inOrder.verify(s3Loader,Mockito.times(1)).objectsListForDataset("ExternalPool-Applications");
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
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSSend zfsSendIncremental = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

//        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(Mockito.any(), Mockito.any())).thenReturn(zfsSendIncremental);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return Paths.get("/tmp/"+(String)args[0]+".part"+(int)args[1]);
        });
        Mockito.doThrow(new ZFSStreamEndedException())
                .when(zfsFileWriter).write(Mockito.any(), Mockito.any());
        Mockito.when(s3Loader.objectsListForDataset("ExternalPool-Applications")).thenReturn(
                List.of(
                        "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0"
                )
        );

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
        snapshotSender.sendStartingFromIncremental("ExternalPool/Applications", List.of(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000")
        ));


        InOrder inOrder = Mockito.inOrder( s3Loader);

        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload(
                "ExternalPool-Applications",
                Paths.get("/tmp/ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0")
        );
        inOrder.verify(s3Loader,Mockito.times(1)).objectsListForDataset("ExternalPool-Applications");
    }

    @Test
    void shouldThrowException()
            throws IOException,
            FileHitSizeLimitException,
            CompressorException,
            ZFSStreamEndedException,
            EncryptException,
            NoSuchAlgorithmException,
            InterruptedException,
            IncorrectHashException,
            S3MissesFileException,
            ExecutionException{
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenThrow(new IOException());


        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);

        Assertions.assertThrows(Exception.class,()->{
            snapshotSender.sendStartingFromFull("ExternalPool/Applications", List.of(
                    new Snapshot("ExternalPool/Applications@auto-20220326-150000")
            ));
        });

//        Mockito.verify()
    }

//    @Test
//    void shouldSend() throws IOException, FileHitSizeLimitException, CompressorException, ZFSStreamEndedException, EncryptException {
//        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
//        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
//        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
//        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
//        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);
//
//        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
//        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
//        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
//            Object[] args = invocationOnMock.getArguments();
//            return Paths.get((String)args[0]+".part"+(int)args[1]);
//        });
//        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());
//
//        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,true);
//        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
//        snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true);
//        snapshotSender.sendStartingFromFull("",);
//    }
}
