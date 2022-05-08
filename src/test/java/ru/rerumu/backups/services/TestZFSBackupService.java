package ru.rerumu.backups.services;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.helpers.S3LoaderTest;
import ru.rerumu.backups.services.helpers.ZFSProcessFactoryTest;
import ru.rerumu.backups.services.helpers.ZFSStreamTest;
import ru.rerumu.backups.services.impl.S3LoaderImpl;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestZFSBackupService {

    @Test
    void shouldBackupRestore(@TempDir Path tempDirBackup, @TempDir Path tempDirRestore) throws BaseSnapshotNotFoundException, CompressorException, SnapshotNotFoundException, IOException, InterruptedException, EncryptException {
        ZFSProcessFactoryTest zfsProcessFactory = new ZFSProcessFactoryTest();

        List<String> filesystems = new ArrayList<>();
        filesystems.add("ExternalPool");
        filesystems.add("ExternalPool/Applications");
        filesystems.add("ExternalPool/Applications/virtual_box");
        filesystems.add("ExternalPool/Books");
        zfsProcessFactory.setFilesystems(filesystems);

        List<String> snapshots = new ArrayList<>();
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220326-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-060000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220328-150000");
        snapshots.add("ExternalPool/Applications@auto-20220326-150000");
        snapshots.add("ExternalPool/Applications@auto-20220327-060000");
        snapshots.add("ExternalPool/Applications@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications@auto-20220328-150000");
        snapshots.add("ExternalPool/Books@auto-20220326-150000");
        snapshots.add("ExternalPool/Books@auto-20220327-060000");
        snapshots.add("ExternalPool/Books@auto-20220327-150000");
        snapshots.add("ExternalPool/Books@auto-20220328-150000");
        snapshots.add("ExternalPool@auto-20220326-150000");
        snapshots.add("ExternalPool@auto-20220327-060000");
        snapshots.add("ExternalPool@auto-20220327-150000");
        snapshots.add("ExternalPool@auto-20220328-150000");
        zfsProcessFactory.setStringList(snapshots);


        List<ZFSStreamTest> zfsStreamTests = new ArrayList<>();


        // ExternalPool/Applications/virtual_box
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));

        // ExternalPool/Applications
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));

        // ExternalPool/Books
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));

        // ExternalPool
        zfsStreamTests.add(new ZFSStreamTest(10));
        zfsStreamTests.add(new ZFSStreamTest(10));
        zfsStreamTests.add(new ZFSStreamTest(10));
        zfsStreamTests.add(new ZFSStreamTest(10));

        zfsProcessFactory.setZfsStreamTests(zfsStreamTests);
        zfsProcessFactory.setSnapshots(snapshots,zfsStreamTests);

        byte[] src = new byte[0];
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220326-150000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220327-060000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220327-150000"));

        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220326-150000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220327-060000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220327-150000"));

        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220326-150000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220327-060000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220327-150000"));

        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220326-150000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220327-060000"));
        src = ArrayUtils.addAll(src,zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220327-150000"));


        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory, zfsSnapshotRepository);
        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDirBackup);
        String password = "hMHteFgxdnxBoXD";
        int chunkSize=1000;
        boolean isLoadAWS = true;
        long filePartSize = 10000;




        ZFSBackupService zfsBackupService = new ZFSBackupService(
                password,
                zfsProcessFactory,
                chunkSize,
                isLoadAWS,
                filePartSize,
                filePartRepository,
                zfsFileSystemRepository,
                zfsSnapshotRepository);
        S3Loader s3Loader = new S3LoaderTest(tempDirBackup,tempDirRestore);
        Snapshot targetSnapshot = new Snapshot("ExternalPool@auto-20220327-150000");

        Runnable runnableBackup = ()->{
            Logger logger = LoggerFactory.getLogger("runnableBackup");
            logger.info("Starting backup");
            try {
                zfsBackupService.zfsBackupFull(
                        s3Loader,
                        targetSnapshot.getName(),
                        targetSnapshot.getDataset()
                );
            } catch (IOException | InterruptedException | CompressorException | EncryptException | BaseSnapshotNotFoundException | SnapshotNotFoundException e) {
                logger.error(e.toString());
            }
            logger.info("Finished backup");
        };
        Thread threadSend = new Thread(runnableBackup);


        FilePartRepository restoreFilePartRepository = new FilePartRepositoryImpl(tempDirRestore);
        ZFSProcessFactoryTest restoreZFSProcessFactory = new ZFSProcessFactoryTest();
        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                password,
                restoreZFSProcessFactory,
                true,
                restoreFilePartRepository
        );

        Runnable runnableRestore = ()->{
            Logger logger = LoggerFactory.getLogger("runnableRestore");
            logger.info("Starting restore");
            try {
               zfsRestoreService.zfsReceive(new ZFSPool("ExternalPool"));
            } catch (IOException | TooManyPartsException | EncryptException | CompressorException | InterruptedException | ClassNotFoundException | FinishedFlagException | NoMorePartsException e) {
                logger.error(e.toString());
            }
            logger.info("Finished restore");
        };
        Thread threadRestore = new Thread(runnableRestore);


        threadRestore.start();
        threadSend.start();


        threadSend.join();
        while (true){
            boolean isFoundFile = true;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirRestore)) {
                isFoundFile = false;
                for (Path item : stream) {
                    isFoundFile = true;
                }
            }
            if (!isFoundFile){
                break;
            }
            Thread.sleep(10000);
        }
        Files.createFile(tempDirRestore.resolve("finished"));
        threadRestore.join();



//        for (ZFSStreamTest zfsStreamTest: zfsStreamTests){
//            src = ArrayUtils.addAll(src,zfsStreamTest.getData());
//        }

        restoreZFSProcessFactory.getZFSReceive().getBufferedOutputStream().flush();
        byte[] dst = restoreZFSProcessFactory.getZFSReceive().getByteArrayOutputStream().toByteArray();

        Assertions.assertArrayEquals(src,dst);
    }

//    @Disabled
//    @Test
//    void shouldSendS3() throws CompressorException, IOException, InterruptedException, EncryptException, NoMorePartsException, TooManyPartsException, ClassNotFoundException {
//
//        FilePartRepositoryTest filePartRepository = new FilePartRepositoryTest(new ArrayList<>());
//        ZFSBackupService zfsBackupServiceSend = new ZFSBackupService(
//                "gWR9IPAzbSaOfPp0",
//                new ZFSProcessFactory(),
//                40000,
//                true,
//                45000L,
//                filePartRepository,
//                new ZFSFileSystemRepositoryImpl(new ZFSProcessFactory()),
//                new ZFSSnapshotRepositoryImpl(new ZFSProcessFactory())
//        );
//
//        ZFSSendTest zfsSendTest = new ZFSSendTest(100000);
//
//        S3Loader s3LoaderMock = Mockito.mock(S3Loader.class);
//        ArgumentCaptor<Path> argumentCaptorPath = ArgumentCaptor.forClass(Path.class);
//
//        zfsBackupServiceSend.zfsBackupFull(
//                s3LoaderMock,
//                new Snapshot("main@1111")
//        );
//
//        Mockito.verify(s3LoaderMock,Mockito.atLeastOnce()).upload(argumentCaptorPath.capture());
//        List<Path> argumentsPaths = argumentCaptorPath.getAllValues();
//        List<Path> pathList = filePartRepository.getPathList();
//        for (int i=0;i<pathList.size();i++){
//            Assertions.assertEquals(pathList.get(i).toString(),argumentsPaths.get(i).toString());
//        }
//    }

//    @Disabled
//    @Test
//    void shouldSendReceive(@TempDir Path tempDir) throws CompressorException, IOException, InterruptedException, EncryptException, NoMorePartsException, TooManyPartsException, ClassNotFoundException {
//        FilePartRepository filePartRepositorySend = new FilePartRepositoryImpl(tempDir,"MainPool@level0_25_02_2020__20_50");
//        ZFSBackupService zfsBackupServiceSend = new ZFSBackupService(
//                "gWR9IPAzbSaOfPp0",
//                new ZFSProcessFactory(),
//                40000,
//                true,
//                45000L,
//                filePartRepositorySend,
//                new ZFSFileSystemRepositoryImpl(new ZFSProcessFactory()),
//                new ZFSSnapshotRepositoryImpl(new ZFSProcessFactory())
//        );
//
//        ZFSSendTest zfsSendTest = new ZFSSendTest(100000);
//
//        FilePartRepository filePartRepositoryReceive = new FilePartRepositoryImpl(tempDir,"MainPool@level0_25_02_2020__20_50");
//        S3Loader s3LoaderMock = Mockito.mock(S3Loader.class);
//
//        ZFSBackupService zfsBackupServiceReceive = new ZFSBackupService(
//                "gWR9IPAzbSaOfPp0",
//                new ZFSProcessFactory(),
//                40000,
//                true,
//                45000L,
//                filePartRepositoryReceive,
//                new ZFSFileSystemRepositoryImpl(new ZFSProcessFactory()),
//                new ZFSSnapshotRepositoryImpl(new ZFSProcessFactory())
//        );
//        ZFSReceiveTest zfsReceiveTest = new ZFSReceiveTest();
//
//        Runnable runnableSend = ()->{
//            Logger logger = LoggerFactory.getLogger("runnableSend");
//            try {
//                zfsBackupServiceSend.zfsBackupFull(
//                        zfsSendTest,
//                        40000,
//                        false,
//                        45000L,
//                        filePartRepositorySend,
//                        false,
//                        s3LoaderMock
//                );
//            } catch (IOException | InterruptedException | CompressorException | EncryptException e) {
//                logger.error(e.toString());
//            }
//        };
//        Thread threadSend = new Thread(runnableSend);
//
//        Runnable runnableReceive = ()->{
//            Logger logger = LoggerFactory.getLogger("runnableReceive");
//            try {
//                zfsBackupServiceReceive.zfsReceive(
//                        zfsReceiveTest,
//                        filePartRepositoryReceive,
//                        false
//                );
//            } catch (IOException | TooManyPartsException | EncryptException | CompressorException | InterruptedException | ClassNotFoundException e) {
//                logger.error(e.toString());
//            }
//        };
//        Thread threadReceive = new Thread(runnableReceive);
//
//        threadSend.start();
//        threadReceive.start();
//
//        threadSend.join();
//        Files.createFile(tempDir.resolve("finished"));
//        threadReceive.join();
//
//        byte[] src = zfsSendTest.getSrc();
//        zfsReceiveTest.getBufferedOutputStream().flush();
//        byte[] dst = zfsReceiveTest.getByteArrayOutputStream().toByteArray();
//
////        Thread.sleep(120000);
//
//        Assertions.assertArrayEquals(src,dst);
//    }
}
