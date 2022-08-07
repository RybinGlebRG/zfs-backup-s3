package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.exceptions.NoBackupMetaException;
import ru.rerumu.backups.exceptions.NoDatasetMetaException;
import ru.rerumu.backups.exceptions.NoPartFoundException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.meta.PartMeta;
import ru.rerumu.backups.repositories.impl.S3Repository;
import ru.rerumu.backups.services.S3Manager;
import ru.rerumu.backups.services.impl.ListManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TestS3Repository {

    @Mock
    S3ManagerFactory s3ManagerFactory;
    @Mock
    S3ClientFactory s3ClientFactory;
    @Mock
    S3Client s3Client;
    @Mock
    ListManager listManager;
    @Mock
    S3Manager s3Manager;


    @Test
    void shouldCheckFileExists(@TempDir Path tmpDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);

        boolean isExists = remoteBackupRepository.isFileExists("Test", "part0");

        Mockito.verify(s3ManagerFactory).getListManager(Mockito.any(), Mockito.eq("TestPrefix/Test/part0"), Mockito.any());

        Assertions.assertTrue(isExists);
    }

    @Test
    void shouldCheckFileNotExists(@TempDir Path tmpDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.doThrow(new S3MissesFileException()).when(listManager).run();

        boolean isExists = remoteBackupRepository.isFileExists("Test", "part0");

        Mockito.verify(s3ManagerFactory).getListManager(Mockito.any(), Mockito.eq("TestPrefix/Test/part0"), Mockito.any());

        Assertions.assertFalse(isExists);
    }

    @Test
    void shouldAdd(@TempDir Path tmpDir, @TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getUploadManager(
                Mockito.any(),
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any())
        ).thenReturn(s3Manager);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);

        Files.createFile(otherDir.resolve("part0"));

        remoteBackupRepository.add("Test/", otherDir.resolve("part0"));

        Mockito.verify(s3ManagerFactory).getUploadManager(
                Mockito.any(),
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any()
        );

    }

    @Test
    void shouldAddButFail(@TempDir Path tmpDir, @TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getUploadManager(
                Mockito.any(),
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any())
        ).thenReturn(s3Manager);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.doThrow(new S3MissesFileException()).when(listManager).run();

        Files.createFile(otherDir.resolve("part0"));

        Assertions.assertThrows(
                S3MissesFileException.class,
                ()->remoteBackupRepository.add("Test/", otherDir.resolve("part0"))
        );

        Mockito.verify(s3ManagerFactory).getUploadManager(
                Mockito.any(),
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any()
        );

    }
    @Test
    void shouldGetPart(@TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getDownloadManager(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any())
        ).thenReturn(s3Manager);

        PartMeta partMeta = new PartMeta("part0",0L,"Test","1111");

        remoteBackupRepository.getPart("Test","part0",otherDir,partMeta);

        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory).getDownloadManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any(),
                Mockito.eq(otherDir.resolve("part0")),
                Mockito.any()
        );
    }

    @Test
    void shouldGetPartButNoFile(@TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.doThrow(new S3MissesFileException()).when(listManager).run();

        PartMeta partMeta = new PartMeta("part0",0L,"Test","1111");

        Assertions.assertThrows(
                NoPartFoundException.class,
                ()->remoteBackupRepository.getPart("Test","part0",otherDir,partMeta)
        );

        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/part0"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory, Mockito.never()).getDownloadManager(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );
    }

    @Test
    void shouldGetBackupMeta(@TempDir Path tmpDir, @TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.when(s3ManagerFactory.getDownloadManager(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any())
        ).thenReturn(s3Manager);


        Path res = remoteBackupRepository.getBackupMeta(otherDir);

        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/_meta.json"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory).getDownloadManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/_meta.json"),
                Mockito.any(),
                Mockito.eq(otherDir.resolve("_meta.json"))
        );

        Assertions.assertEquals(otherDir.resolve("_meta.json"),res);

    }

    @Test
    void shouldGetBackupMetaButNoFile(@TempDir Path tmpDir, @TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.doThrow(new S3MissesFileException()).when(listManager).run();


        Assertions.assertThrows(
                NoBackupMetaException.class,
                ()->remoteBackupRepository.getBackupMeta(otherDir)
        );

        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/_meta.json"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory, Mockito.never()).getDownloadManager(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );
    }

    @Test
    void shouldGetDatasetMeta(@TempDir Path tmpDir, @TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.when(s3ManagerFactory.getDownloadManager(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any())
        ).thenReturn(s3Manager);


        Path res = remoteBackupRepository.getDatasetMeta("Test",otherDir.resolve("Test"));

        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/_meta.json"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory).getDownloadManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/_meta.json"),
                Mockito.any(),
                Mockito.eq(otherDir.resolve("Test").resolve("_meta.json"))
        );

        Assertions.assertEquals(otherDir.resolve("Test").resolve("_meta.json"),res);
    }

    @Test
    void shouldGetDatasetMetaButNoFile(@TempDir Path tmpDir, @TempDir Path otherDir) throws Exception {
        S3Storage s3Storage = new S3Storage(
                null,
                "TestBucket",
                "TestKeyId",
                "TestSecretKey",
                Paths.get("TestPrefix"),
                null,
                "TestStorageClass");
        RemoteBackupRepository remoteBackupRepository = new S3Repository(
                List.of(s3Storage),
                s3ManagerFactory,
                s3ClientFactory
        );

        Mockito.when(s3ClientFactory.getS3Client(Mockito.any()))
                .thenReturn(s3Client);
        Mockito.when(s3ManagerFactory.getListManager(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(listManager);
        Mockito.doThrow(new S3MissesFileException()).when(listManager).run();


        Assertions.assertThrows(
                NoDatasetMetaException.class,
                ()->remoteBackupRepository.getDatasetMeta("Test",otherDir.resolve("Test"))
        );

        Mockito.verify(s3ManagerFactory).getListManager(
                Mockito.any(),
                Mockito.eq("TestPrefix/Test/_meta.json"),
                Mockito.any()
        );
        Mockito.verify(s3ManagerFactory, Mockito.never()).getDownloadManager(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );
    }
}
