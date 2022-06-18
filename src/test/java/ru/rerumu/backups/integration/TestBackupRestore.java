package ru.rerumu.backups.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import ru.rerumu.backups.controllers.BackupController;
import ru.rerumu.backups.controllers.RestoreController;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.*;
import ru.rerumu.backups.services.impl.*;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSReceive;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestBackupRestore {
    //    private List<String> prepareFilesystems() {
//        List<String> filesystems = new ArrayList<>();
//        filesystems.add("ExternalPool");
//        filesystems.add("ExternalPool/Applications");
//        filesystems.add("ExternalPool/Applications/virtual_box");
//        filesystems.add("ExternalPool/Books");
//        filesystems.add("ExternalPool/Containers");
//        filesystems.add("ExternalPool/VMs");
//        return filesystems;
//    }
//
//    private List<String> prepareSnapshots() {
//        List<String> snapshots = new ArrayList<>();
//        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-150000");
//        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220328-150000");
//        snapshots.add("ExternalPool/Applications@auto-20220326-150000");
//        snapshots.add("ExternalPool/Applications@auto-2022.03.27-06.00.00");
//        snapshots.add("ExternalPool/Applications@auto-20220327-150000");
//        snapshots.add("ExternalPool/Applications@auto-20220328-150000");
//        snapshots.add("ExternalPool/Books@auto-20220326-150000");
//        snapshots.add("ExternalPool/Books@auto-2022.03.27-06.00.00");
//        snapshots.add("ExternalPool/Books@auto-20220327-150000");
//        snapshots.add("ExternalPool/Books@auto-20220328-150000");
//        snapshots.add("ExternalPool/Containers@auto-20220328-150000");
//        snapshots.add("ExternalPool/Containers@auto-20220329-150000");
//        snapshots.add("ExternalPool/VMs@auto-20220328-150000");
//        snapshots.add("ExternalPool@auto-20220326-150000");
//        snapshots.add("ExternalPool@auto-2022.03.27-06.00.00");
//        snapshots.add("ExternalPool@auto-20220327-150000");
//        snapshots.add("ExternalPool@auto-20220328-150000");
//        return snapshots;
//    }
//
//    private List<ZFSStreamTest> prepareZFSStreams() {
//        List<ZFSStreamTest> zfsStreamTests = new ArrayList<>();
//
//
//        // ExternalPool/Applications/virtual_box
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//
//        // ExternalPool/Applications
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//
//        // ExternalPool/Books
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//
//        // ExternalPool/Containers
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//
//        // ExternalPool/VMs
//        zfsStreamTests.add(new ZFSStreamTest(1500));
//
//        // ExternalPool
//        zfsStreamTests.add(new ZFSStreamTest(250));
//        zfsStreamTests.add(new ZFSStreamTest(250));
//        zfsStreamTests.add(new ZFSStreamTest(250));
//        zfsStreamTests.add(new ZFSStreamTest(200));
//
//        return zfsStreamTests;
//    }
//
//    @Test
//    void shouldBackupRestore(@TempDir Path tempDirBackup, @TempDir Path tempDirRestore) throws IOException, InterruptedException {
//        ZFSProcessFactoryTest zfsProcessFactory = new ZFSProcessFactoryTest();
//
//        zfsProcessFactory.setFilesystems(prepareFilesystems());
//
//        List<String> snapshots = prepareSnapshots();
//        zfsProcessFactory.setStringList(snapshots);
//
//        List<ZFSStreamTest> zfsStreamTests = prepareZFSStreams();
//        zfsProcessFactory.setZfsStreamTests(zfsStreamTests);
//
//        zfsProcessFactory.setSnapshots(snapshots,zfsStreamTests);
//
//        List<byte[]> srcList = new ArrayList<>();
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220326-150000"));
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-2022.03.27-06.00.00"));
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220327-150000"));
//
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220326-150000"));
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-2022.03.27-06.00.00"));
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220327-150000"));
//
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220327-150000"));
//
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220326-150000"));
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-2022.03.27-06.00.00"));
//        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220327-150000"));
//
//
//        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
//        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory, zfsSnapshotRepository);
//        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDirBackup);
//        String password = "hMHteFgxdnxBoXD";
//        int chunkSize=700;
//        boolean isLoadAWS = true;
//        long filePartSize = 1000;
//
//
//
//
//        ZFSBackupService zfsBackupService = new ZFSBackupService(
//                password,
//                zfsProcessFactory,
//                chunkSize,
//                isLoadAWS,
//                filePartSize,
//                filePartRepository,
//                zfsFileSystemRepository,
//                zfsSnapshotRepository);
//        S3Loader s3Loader = new S3LoaderTest(tempDirBackup,tempDirRestore);
//        Snapshot targetSnapshot = new Snapshot("ExternalPool@auto-20220327-150000");
//        AtomicBoolean isBackupError = new AtomicBoolean(false);
//        Runnable runnableBackup = ()->{
//            Logger logger = LoggerFactory.getLogger("runnableBackup");
//            logger.info("Starting backup");
//
//            try {
//                zfsBackupService.zfsBackupFull(
//                        s3Loader,
//                        targetSnapshot.getName(),
//                        targetSnapshot.getDataset()
//                );
//            } catch (Exception e) {
//                logger.error(e.toString());
//                isBackupError.set(true);
//            }
//            logger.info("Finished backup");
//        };
//        Thread threadSend = new Thread(runnableBackup);
//
//
//        FilePartRepository restoreFilePartRepository = new FilePartRepositoryImpl(tempDirRestore);
//        ZFSProcessFactoryTest restoreZFSProcessFactory = new ZFSProcessFactoryTest();
//        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
//                password,
//                restoreZFSProcessFactory,
//                true,
//                restoreFilePartRepository
//        );
//
//        AtomicBoolean isRestoreError = new AtomicBoolean(false);;
//        Runnable runnableRestore = ()->{
//            Logger logger = LoggerFactory.getLogger("runnableRestore");
//            logger.info("Starting restore");
//            try {
//               zfsRestoreService.zfsReceive(new ZFSPool("ExternalPool"));
//            } catch (Exception e) {
//                logger.error(e.toString());
//                isRestoreError.set(true);
//            }
//            logger.info("Finished restore");
//        };
//        Thread threadRestore = new Thread(runnableRestore);
//
//
//        threadRestore.start();
//        threadSend.start();
//
//
//        threadSend.join();
//        while (true){
//            boolean isFoundFile = true;
//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirRestore)) {
//                isFoundFile = false;
//                for (Path item : stream) {
//                    isFoundFile = true;
//                }
//            }
//            if (!isFoundFile){
//                break;
//            }
//            Thread.sleep(10000);
//        }
//        Files.createFile(tempDirRestore.resolve("finished"));
//        threadRestore.join();
//
//        List<byte[]> dstList = ZFSReceiveTest.getResultList();
//
//        Assertions.assertFalse(isBackupError.get());
//        Assertions.assertFalse(isRestoreError.get());
//
//        Assertions.assertEquals(srcList.size(),dstList.size());
//
//        for (int i =0; i<srcList.size(); i++){
//            Assertions.assertArrayEquals(srcList.get(i),dstList.get(i));
//        }
//    }

    private byte[] randomBytes(int n) {
        byte[] bytes = new byte[n];
        new Random().nextBytes(bytes);
        return bytes;
    }

    private List<byte[]> srcBytesList;
//    private List<ByteArrayOutputStream> byteArrayOutputStreamList;
    private List<byte[]> resBytes;

    private BackupController setupBackup(Path backupDir, Path restoreDir) throws IOException, NoSuchAlgorithmException, InterruptedException, IncorrectHashException {
        FilePartRepository filePartRepository = new FilePartRepositoryImpl(backupDir);

        // s3Loader
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
//        List<String> sentFiles = new ArrayList<>();

        Mockito.doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            String filename = ((Path) args[1]).getFileName().toString();
            Files.copy((Path) args[1], restoreDir.resolve(filename + ".ready"));
            while (Files.exists(restoreDir.resolve(filename + ".ready"))) {
                Thread.sleep(1000);
            }
            return null;
        }).when(s3Loader).upload(Mockito.any(), Mockito.any());
        Mockito.when(s3Loader.objectsListForDataset("ExternalPool"))
                .thenReturn(
                        List.of(
                                "ExternalPool@auto-20220326-150000.part0",
                                "ExternalPool@auto-20220326-150000__ExternalPool@auto-2022.03.27-06.00.00.part0",
                                "ExternalPool@auto-2022.03.27-06.00.00__ExternalPool@auto-20220327-150000.part0"
                        )
                );
        Mockito.when(s3Loader.objectsListForDataset("ExternalPool-Applications"))
                .thenReturn(
                        List.of(
                                "ExternalPool-Applications@auto-20220326-150000.part0",
                                "ExternalPool-Applications@auto-20220326-150000.part1",
                                "ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-2022.03.27-06.00.00.part0",
                                "ExternalPool-Applications@auto-2022.03.27-06.00.00__ExternalPool-Applications@auto-20220327-150000.part0"
                        )
                );


        // zfsListFilesystems
        ProcessWrapper zfsListFilesystems = Mockito.mock(ProcessWrapper.class);
        String filesystems = "ExternalPool\n" +
                "ExternalPool/Applications\n";
        Mockito.when(zfsListFilesystems.getBufferedInputStream()).thenReturn(
                new BufferedInputStream(new ByteArrayInputStream(filesystems.getBytes(StandardCharsets.UTF_8)))
        );

        // zfsListSnapshots
        List<ProcessWrapper> processWrappers = new ArrayList<>();
        processWrappers.add(Mockito.mock(ProcessWrapper.class));
        processWrappers.add(Mockito.mock(ProcessWrapper.class));

        List<String> stringSnapshots = new ArrayList<>();
        stringSnapshots.add("ExternalPool@auto-20220326-150000\n" +
                "ExternalPool@auto-2022.03.27-06.00.00\n" +
                "ExternalPool@auto-20220327-150000\n" +
                "ExternalPool@auto-20220328-150000\n");
        stringSnapshots.add("ExternalPool/Applications@auto-20220326-150000\n" +
                "ExternalPool/Applications@auto-2022.03.27-06.00.00\n" +
                "ExternalPool/Applications@auto-20220327-150000\n" +
                "ExternalPool/Applications@auto-20220328-150000\n");

        for (int i = 0; i < processWrappers.size(); i++) {
            Mockito.when(processWrappers.get(i).getBufferedInputStream())
                    .thenReturn(
                            new BufferedInputStream(new ByteArrayInputStream(stringSnapshots.get(i).getBytes(StandardCharsets.UTF_8)))
                    );
        }

        // ZFSSend
        List<ZFSSend> zfsSendList = new ArrayList<>();
        // ExternalPool
        zfsSendList.add(Mockito.mock(ZFSSend.class));
        zfsSendList.add(Mockito.mock(ZFSSend.class));
        zfsSendList.add(Mockito.mock(ZFSSend.class));
        // ExternalPool/Applications
        zfsSendList.add(Mockito.mock(ZFSSend.class));
        zfsSendList.add(Mockito.mock(ZFSSend.class));
        zfsSendList.add(Mockito.mock(ZFSSend.class));

        srcBytesList = new ArrayList<>();
        // ExternalPool
        srcBytesList.add(randomBytes(250));
        srcBytesList.add(randomBytes(250));
        srcBytesList.add(randomBytes(250));
        // ExternalPool/Applications
        srcBytesList.add(randomBytes(2000));
        srcBytesList.add(randomBytes(250));
        srcBytesList.add(randomBytes(250));

        for (int i = 0; i < zfsSendList.size(); i++) {
            Mockito.when(zfsSendList.get(i).getBufferedInputStream())
                    .thenReturn(
                            new BufferedInputStream(new ByteArrayInputStream(srcBytesList.get(i)))
                    );
        }

        // zfsProcessFactory
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);

        Mockito.when(zfsProcessFactory.getZFSListFilesystems("ExternalPool"))
                .thenReturn(zfsListFilesystems);

        Mockito.when(zfsProcessFactory.getZFSListSnapshots("ExternalPool"))
                .thenReturn(processWrappers.get(0));
        Mockito.when(zfsProcessFactory.getZFSListSnapshots("ExternalPool/Applications"))
                .thenReturn(processWrappers.get(1));

        Mockito.when(zfsProcessFactory.getZFSSendFull(
                new Snapshot("ExternalPool@auto-20220326-150000")
        )).thenReturn(zfsSendList.get(0));
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-2022.03.27-06.00.00")
        )).thenReturn(zfsSendList.get(1));
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool@auto-2022.03.27-06.00.00"),
                new Snapshot("ExternalPool@auto-20220327-150000")
        )).thenReturn(zfsSendList.get(2));

        Mockito.when(zfsProcessFactory.getZFSSendFull(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000")
        )).thenReturn(zfsSendList.get(3));
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-2022.03.27-06.00.00")
        )).thenReturn(zfsSendList.get(4));
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool/Applications@auto-2022.03.27-06.00.00"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000")
        )).thenReturn(zfsSendList.get(5));


        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory, zfsSnapshotRepository);
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(
                "84fBS1KsChnuaV0",
                1070,
                1024);
        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory,
                true);
        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                zfsFileSystemRepository,
                snapshotSender
        );
        BackupController backupController = new BackupController(zfsBackupService);
        return backupController;
    }

    private RestoreController setupRestore(Path restoreDir) throws IOException, ExecutionException, InterruptedException {
        FilePartRepository filePartRepository = new FilePartRepositoryImpl(restoreDir);

        ZFSReceive zfsReceive = Mockito.mock(ZFSReceive.class);
        List<ByteArrayOutputStream> byteArrayOutputStreamList = new ArrayList<>();
        byteArrayOutputStreamList.add(new ByteArrayOutputStream());

        resBytes = new ArrayList<>();

        List<BufferedOutputStream> bufferedOutputStreamList = new ArrayList<>();
        ByteArrayOutputStream tmpArray1 = new ByteArrayOutputStream();
        byteArrayOutputStreamList.add(tmpArray1);
        BufferedOutputStream tmpBuf1 = new BufferedOutputStream(tmpArray1);
        bufferedOutputStreamList.add(tmpBuf1);

        Mockito.doAnswer(invocationOnMock -> {
            bufferedOutputStreamList.get(bufferedOutputStreamList.size()-1).flush();
            byte[] tmp = byteArrayOutputStreamList.get(byteArrayOutputStreamList.size()-1).toByteArray();
            resBytes.add(tmp);

            ByteArrayOutputStream tmpArray = new ByteArrayOutputStream();
            byteArrayOutputStreamList.add(tmpArray);
            BufferedOutputStream tmpBuf = new BufferedOutputStream(tmpArray);
            bufferedOutputStreamList.add(tmpBuf);


            return null;
        }).when(zfsReceive).close();

        Mockito.when(zfsReceive.getBufferedOutputStream())
                .thenAnswer(invocationOnMock -> {
                    BufferedOutputStream tmpBuf = bufferedOutputStreamList.get(bufferedOutputStreamList.size()-1);
                    return tmpBuf;
                });

        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        Mockito.when(zfsProcessFactory.getZFSReceive(Mockito.any()))
                .thenReturn(zfsReceive);


        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl("84fBS1KsChnuaV0");
        SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
                zfsProcessFactory,
                new ZFSPool("ReceivePool"),
                filePartRepository,
                zfsFileReaderFactory,
                true
        );

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                "84fBS1KsChnuaV0",
                zfsProcessFactory,
                true,
                filePartRepository,
                snapshotReceiver);

        RestoreController restoreController = new RestoreController(zfsRestoreService);
        return restoreController;
    }

    @Test
    void shouldBackupRestore(@TempDir Path backupDir, @TempDir Path restoreDir) throws IOException, NoSuchAlgorithmException, InterruptedException, IncorrectHashException, ExecutionException {

        BackupController backupController = setupBackup(backupDir,restoreDir);
        RestoreController restoreController = setupRestore(restoreDir);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<?> backupFuture = executorService.submit(() -> {
            backupController.backupFull("ExternalPool@auto-20220327-150000");
        });

        Future<?> restoreFuture = executorService.submit( () -> {
            restoreController.restore();
        });

        backupFuture.get();

        while (true) {
            boolean isFoundFile = true;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(restoreDir)) {
                isFoundFile = false;
                for (Path item : stream) {
                    isFoundFile = true;
                }
            }
            if (!isFoundFile) {
                break;
            }
            Thread.sleep(10000);
        }
        Files.createFile(restoreDir.resolve("finished"));

        restoreFuture.get();
//        resBytes.remove(resBytes.size()-1);
//        byteArrayOutputStreamList.remove(byteArrayOutputStreamList.size()-1);

        Assertions.assertEquals(srcBytesList.size(), resBytes.size());

        for (int i = 0; i < srcBytesList.size(); i++) {
            Assertions.assertArrayEquals(srcBytesList.get(i), resBytes.get(i));
        }

    }
}
