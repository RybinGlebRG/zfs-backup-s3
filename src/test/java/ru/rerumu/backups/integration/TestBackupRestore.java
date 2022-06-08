package ru.rerumu.backups.integration;

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
}