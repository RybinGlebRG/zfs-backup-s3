package ru.rerumu.backups.services;

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
import ru.rerumu.backups.services.helpers.ZFSReceiveTest;
import ru.rerumu.backups.services.helpers.ZFSStreamTest;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestZFSBackupService {

    @Test
    void shouldBackupRestore(@TempDir Path tempDirBackup, @TempDir Path tempDirRestore) throws BaseSnapshotNotFoundException, CompressorException, SnapshotNotFoundException, IOException, InterruptedException, EncryptException {
        ZFSProcessFactoryTest zfsProcessFactory = new ZFSProcessFactoryTest();

        List<String> filesystems = new ArrayList<>();
        filesystems.add("ExternalPool");
        filesystems.add("ExternalPool/Applications");
        filesystems.add("ExternalPool/Applications/virtual_box");
        filesystems.add("ExternalPool/Books");
        filesystems.add("ExternalPool/Containers");
        filesystems.add("ExternalPool/VMs");
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
        snapshots.add("ExternalPool/Containers@auto-20220328-150000");
        snapshots.add("ExternalPool/Containers@auto-20220329-150000");
        snapshots.add("ExternalPool/VMs@auto-20220328-150000");
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

        zfsProcessFactory.setZfsStreamTests(zfsStreamTests);
        zfsProcessFactory.setSnapshots(snapshots,zfsStreamTests);

        List<byte[]> srcList = new ArrayList<>();
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220326-150000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220327-060000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool@auto-20220327-150000"));

        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220326-150000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220327-060000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications@auto-20220327-150000"));

        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220326-150000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220327-060000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Applications/virtual_box@auto-20220327-150000"));

        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220326-150000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220327-060000"));
        srcList.add(zfsProcessFactory.getSnapshotsWithStream().get("ExternalPool/Books@auto-20220327-150000"));


        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory, zfsSnapshotRepository);
        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDirBackup);
        String password = "hMHteFgxdnxBoXD";
        int chunkSize=100;
        boolean isLoadAWS = true;
        long filePartSize = 1000;




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
        AtomicBoolean isBackupError = new AtomicBoolean(false);
        Runnable runnableBackup = ()->{
            Logger logger = LoggerFactory.getLogger("runnableBackup");
            logger.info("Starting backup");

            try {
                zfsBackupService.zfsBackupFull(
                        s3Loader,
                        targetSnapshot.getName(),
                        targetSnapshot.getDataset()
                );
            } catch (Exception e) {
                logger.error(e.toString());
                isBackupError.set(true);
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

        AtomicBoolean isRestoreError = new AtomicBoolean(false);;
        Runnable runnableRestore = ()->{
            Logger logger = LoggerFactory.getLogger("runnableRestore");
            logger.info("Starting restore");
            try {
               zfsRestoreService.zfsReceive(new ZFSPool("ExternalPool"));
            } catch (Exception e) {
                logger.error(e.toString());
                isRestoreError.set(true);
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

        List<byte[]> dstList = ZFSReceiveTest.getResultList();

        Assertions.assertFalse(isBackupError.get());
        Assertions.assertFalse(isRestoreError.get());

        Assertions.assertEquals(srcList.size(),dstList.size());

        for (int i =0; i<srcList.size(); i++){
            Assertions.assertArrayEquals(srcList.get(i),dstList.get(i));
        }
    }
}
