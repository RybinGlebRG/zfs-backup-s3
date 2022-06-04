package ru.rerumu.backups.services;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.helpers.S3LoaderTest;
import ru.rerumu.backups.services.helpers.ZFSProcessFactoryTest;
import ru.rerumu.backups.services.helpers.ZFSReceiveTest;
import ru.rerumu.backups.services.helpers.ZFSStreamTest;
import ru.rerumu.backups.services.impl.SnapshotSenderImpl;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Matchers.*;

public class TestZFSBackupService {

    private List<String> prepareFilesystems() {
        List<String> filesystems = new ArrayList<>();
        filesystems.add("ExternalPool");
        filesystems.add("ExternalPool/Applications");
        filesystems.add("ExternalPool/Applications/virtual_box");
        filesystems.add("ExternalPool/Books");
        filesystems.add("ExternalPool/Containers");
        filesystems.add("ExternalPool/VMs");
        return filesystems;
    }

    private List<String> prepareSnapshots() {
        List<String> snapshots = new ArrayList<>();
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications/virtual_box@auto-20220328-150000");
        snapshots.add("ExternalPool/Applications@auto-20220326-150000");
        snapshots.add("ExternalPool/Applications@auto-2022.03.27-06.00.00");
        snapshots.add("ExternalPool/Applications@auto-20220327-150000");
        snapshots.add("ExternalPool/Applications@auto-20220328-150000");
        snapshots.add("ExternalPool/Books@auto-20220326-150000");
        snapshots.add("ExternalPool/Books@auto-2022.03.27-06.00.00");
        snapshots.add("ExternalPool/Books@auto-20220327-150000");
        snapshots.add("ExternalPool/Books@auto-20220328-150000");
        snapshots.add("ExternalPool/Containers@auto-20220328-150000");
        snapshots.add("ExternalPool/Containers@auto-20220329-150000");
        snapshots.add("ExternalPool/VMs@auto-20220328-150000");
        snapshots.add("ExternalPool@auto-20220326-150000");
        snapshots.add("ExternalPool@auto-2022.03.27-06.00.00");
        snapshots.add("ExternalPool@auto-20220327-150000");
        snapshots.add("ExternalPool@auto-20220328-150000");
        return snapshots;
    }

    private List<ZFSStreamTest> prepareZFSStreams() {
        List<ZFSStreamTest> zfsStreamTests = new ArrayList<>();


        // ExternalPool/Applications/virtual_box
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

        // ExternalPool/Containers
        zfsStreamTests.add(new ZFSStreamTest(1500));
        zfsStreamTests.add(new ZFSStreamTest(1500));

        // ExternalPool/VMs
        zfsStreamTests.add(new ZFSStreamTest(1500));

        // ExternalPool
        zfsStreamTests.add(new ZFSStreamTest(250));
        zfsStreamTests.add(new ZFSStreamTest(250));
        zfsStreamTests.add(new ZFSStreamTest(250));
        zfsStreamTests.add(new ZFSStreamTest(200));

        return zfsStreamTests;
    }
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

    @Test
    void shouldBackupInOrder() throws CompressorException, IOException, InterruptedException, EncryptException, IllegalArgumentException, BaseSnapshotNotFoundException, NoSuchAlgorithmException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);
        InOrder inOrder = Mockito.inOrder(mockedSnapshotSender);


        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220327-150000",
                "ExternalPool"
        );


        inOrder.verify(mockedSnapshotSender).sendBaseSnapshot(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                mockedS3Loader,
                true
        );
        inOrder.verify(mockedSnapshotSender).sendIncrementalSnapshot(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-20220327-060000"),
                mockedS3Loader,
                true
        );
        inOrder.verify(mockedSnapshotSender).sendIncrementalSnapshot(
                new Snapshot("ExternalPool@auto-20220327-060000"),
                new Snapshot("ExternalPool@auto-20220327-150000"),
                mockedS3Loader,
                true
        );
        inOrder.verify(mockedSnapshotSender).checkSent(
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000")
                ),
                mockedS3Loader
        );
    }

    @Test
    void shouldBackupOnlyBase() throws IOException, InterruptedException, CompressorException, EncryptException, BaseSnapshotNotFoundException, NoSuchAlgorithmException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);
        InOrder inOrder = Mockito.inOrder(mockedSnapshotSender);


        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220326-150000",
                "ExternalPool"
        );

        inOrder.verify(mockedSnapshotSender).sendBaseSnapshot(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                mockedS3Loader,
                true
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendIncrementalSnapshot(any(),any(),any(),anyBoolean());
        inOrder.verify(mockedSnapshotSender).checkSent(
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000")
                ),
                mockedS3Loader
        );
    }

    @Test
    void shouldNotBackupAny() throws IOException, InterruptedException, CompressorException, EncryptException, BaseSnapshotNotFoundException, NoSuchAlgorithmException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220325-150000",
                "ExternalPool"
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendBaseSnapshot(any(),any(),anyBoolean());
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendIncrementalSnapshot(any(),any(),any(),anyBoolean());
        Mockito.verify(mockedSnapshotSender,Mockito.never()).checkSent(any(),any());
    }

    @Test
    void shouldNotBackupAny1() throws IOException, InterruptedException, CompressorException, EncryptException, BaseSnapshotNotFoundException, NoSuchAlgorithmException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
               new ArrayList<>()
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220326-150000",
                "ExternalPool"
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendBaseSnapshot(any(),any(),anyBoolean());
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendIncrementalSnapshot(any(),any(),any(),anyBoolean());
        Mockito.verify(mockedSnapshotSender,Mockito.never()).checkSent(any(),any());
    }
}
