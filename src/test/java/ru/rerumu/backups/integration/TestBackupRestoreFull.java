package ru.rerumu.backups.integration;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.controllers.BackupController;
import ru.rerumu.backups.controllers.RestoreController;
import ru.rerumu.backups.factories.SnapshotSenderFactory;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.factories.impl.*;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.LocalBackupRepositoryImpl;
import ru.rerumu.backups.repositories.impl.S3Repository;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.DatasetPropertiesChecker;
import ru.rerumu.backups.services.SnapshotReceiver;
import ru.rerumu.backups.services.ZFSBackupService;
import ru.rerumu.backups.services.ZFSRestoreService;
import ru.rerumu.backups.services.impl.SnapshotReceiverImpl;
import ru.rerumu.backups.zfs_api.zfs.*;
import software.amazon.awssdk.regions.Region;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Disabled
public class TestBackupRestoreFull {

    private byte[] randomBytes(int n) {
        byte[] bytes = new byte[n];
        new Random().nextBytes(bytes);
        return bytes;
    }

    private List<byte[]> srcBytesList;
    private List<byte[]> resBytes;

    private final String prefix = RandomStringUtils.random(10, true, false);

    private BackupController setupBackup(Path localRepositoryDir, Path senderTmpDir)
            throws Exception {

        RemoteBackupRepository remoteBackupRepository = new S3Repository(List.of(
                new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0/"+prefix),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                )),
                new S3ManagerFactoryImpl(6_000_000),
                new S3ClientFactoryImpl(List.of( new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0/"+prefix),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                ))));
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                localRepositoryDir,
                remoteBackupRepository,
                true
        );


        // zfsListFilesystems
        ZFSListFilesystems zfsListFilesystems = Mockito.mock(ZFSListFilesystems.class);
        String filesystems = "ExternalPool\n" +
                "ExternalPool/Applications\n";
        Mockito.when(zfsListFilesystems.getBufferedInputStream()).thenReturn(
                new BufferedInputStream(new ByteArrayInputStream(filesystems.getBytes(StandardCharsets.UTF_8)))
        );

        // zfsListSnapshots
        List<ZFSListSnapshots> processWrappers = new ArrayList<>();
        processWrappers.add(Mockito.mock(ZFSListSnapshots.class));
        processWrappers.add(Mockito.mock(ZFSListSnapshots.class));

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
        // ExternalPool/Applications
        zfsSendList.add(Mockito.mock(ZFSSend.class));
        zfsSendList.add(Mockito.mock(ZFSSend.class));

        srcBytesList = new ArrayList<>();
        // ExternalPool
        srcBytesList.add(randomBytes(250));
        srcBytesList.add(randomBytes(250));
        // ExternalPool/Applications
        srcBytesList.add(randomBytes(6291456));
        srcBytesList.add(randomBytes(250));

        for (int i = 0; i < zfsSendList.size(); i++) {
            Mockito.when(zfsSendList.get(i).getBufferedInputStream())
                    .thenReturn(
                            new BufferedInputStream(new ByteArrayInputStream(srcBytesList.get(i)))
                    );
        }

        // ZFSGetDatasetProperty
        ZFSGetDatasetProperty zfsGetDatasetProperty = Mockito.mock(ZFSGetDatasetProperty.class);
        Mockito.when(zfsGetDatasetProperty.getBufferedInputStream())
                .thenAnswer(invocationOnMock -> {
                    String tmp = "on" + "\n";
                    byte[] buf = tmp.getBytes(StandardCharsets.UTF_8);
                    return new BufferedInputStream(new ByteArrayInputStream(buf));
                });

        // zfsProcessFactory
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);

        Mockito.when(zfsProcessFactory.getZFSListFilesystems("ExternalPool"))
                .thenReturn(zfsListFilesystems);

        Mockito.when(zfsProcessFactory.getZFSGetDatasetProperty(Mockito.any(),Mockito.eq("encryption")))
                .thenReturn(zfsGetDatasetProperty);

        Mockito.when(zfsProcessFactory.getZFSListSnapshots("ExternalPool"))
                .thenReturn(processWrappers.get(0));
        Mockito.when(zfsProcessFactory.getZFSListSnapshots("ExternalPool/Applications"))
                .thenReturn(processWrappers.get(1));

        Mockito.when(zfsProcessFactory.getZFSSendFull(
                new Snapshot("ExternalPool@auto-20220326-150000")
        )).thenReturn(zfsSendList.get(0));
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-20220327-150000")
        )).thenReturn(zfsSendList.get(1));


        Mockito.when(zfsProcessFactory.getZFSSendFull(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000")
        )).thenReturn(zfsSendList.get(2));
        Mockito.when(zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000")
        )).thenReturn(zfsSendList.get(3));


        ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
        ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory, zfsSnapshotRepository);
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(
                6_000_000);
        SnapshotSenderFactory snapshotSenderFactory = new SnapshotSenderFactoryImpl(
                localBackupRepository,
                zfsProcessFactory,
                zfsFileWriterFactory,
                senderTmpDir
        );
        ZFSBackupService zfsBackupService = new ZFSBackupService(
                zfsFileSystemRepository,
                snapshotSenderFactory.getSnapshotSender(),
                new DatasetPropertiesChecker()
        );
        BackupController backupController = new BackupController(zfsBackupService);
        return backupController;
    }

    private RestoreController setupRestore(Path localRepositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = new S3Repository(List.of(
                new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0/"+prefix),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                )),
                new S3ManagerFactoryImpl(6_000_000),
                new S3ClientFactoryImpl(List.of(new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0/"+prefix),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                ))));
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                localRepositoryDir,
                remoteBackupRepository,
                true
        );

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


        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
        SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
                zfsProcessFactory,
                new ZFSPool("ReceivePool"),
                zfsFileReaderFactory
        );

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                localBackupRepository,
                snapshotReceiver,
                List.of("ExternalPool","ExternalPool-Applications")
                );

        RestoreController restoreController = new RestoreController(zfsRestoreService);
        return restoreController;
    }



    @Test
    void shouldBackupRestore(
            @TempDir Path localRepositoryDir,
            @TempDir Path senderTmpDir)
            throws Exception {

        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class))
                .setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.http.apache.internal.conn.IdleConnectionReaper.class))
                .setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class))
                .setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.http.apache.internal.conn.SdkTlsSocketFactory.class))
                .setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.http.apache.internal.net.SdkSslSocket.class))
                .setLevel(Level.INFO);


        BackupController backupController = setupBackup(localRepositoryDir,senderTmpDir);
        int backupRes = backupController.backupFull("ExternalPool@auto-20220327-150000");

        Assertions.assertEquals(0,backupRes);

        RestoreController restoreController = setupRestore(localRepositoryDir);
        int restoreRes = restoreController.restore();

        Assertions.assertEquals(0,restoreRes);

        Assertions.assertEquals(srcBytesList.size(), resBytes.size());

        for (int i = 0; i < srcBytesList.size(); i++) {
            Assertions.assertArrayEquals(srcBytesList.get(i), resBytes.get(i));
        }

    }
}
