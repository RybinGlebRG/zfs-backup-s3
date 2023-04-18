package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoBackupMetaException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.models.meta.BackupMeta;
import ru.rerumu.backups.models.meta.DatasetMeta;
import ru.rerumu.backups.models.meta.PartMeta;
import ru.rerumu.backups.repositories.impl.LocalBackupRepositoryImpl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Disabled
@Deprecated
public class TestLocalBackupRepositoryWithRemote {

    @Test
    void shouldClone(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    BackupMeta backupMeta = new BackupMeta();
                    backupMeta.addDataset("Test");
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
                            backupMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });
        Mockito.when(remoteBackupRepository.getDatasetMeta(
                        Mockito.any(), Mockito.any())
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L,"Test","1111"));
                    Files.writeString(
                            repositoryDir.resolve("Test").resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });

        Files.createFile(repositoryDir.resolve("testFile"));
        Files.createDirectory(repositoryDir.resolve("testDir"));
        Files.createFile(repositoryDir.resolve("testDir").resolve("testFile"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );

        Assertions.assertFalse(Files.exists(repositoryDir.resolve("testFile")));
        Assertions.assertFalse(Files.exists(repositoryDir.resolve("testDir").resolve("testFile")));
        Assertions.assertFalse(Files.exists(repositoryDir.resolve("testDir")));

        Assertions.assertTrue(Files.exists(repositoryDir.resolve("_meta.json")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));

        InOrder inOrder = Mockito.inOrder(remoteBackupRepository);

        inOrder.verify(remoteBackupRepository).getBackupMeta(repositoryDir);
        inOrder.verify(remoteBackupRepository).getDatasetMeta("Test",repositoryDir.resolve("Test"));
    }

    @Test
    void shouldCloneEmpty(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.any()))
                .thenThrow(new NoBackupMetaException());

        Files.createFile(repositoryDir.resolve("testFile"));
        Files.createDirectory(repositoryDir.resolve("testDir"));
        Files.createFile(repositoryDir.resolve("testDir").resolve("testFile"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );


        Assertions.assertFalse(Files.exists(repositoryDir.resolve("_meta.json")));

        Mockito.verify(remoteBackupRepository, Mockito.times(1)).getBackupMeta(repositoryDir);
        Mockito.verify(remoteBackupRepository,Mockito.never()).getDatasetMeta(Mockito.any(),Mockito.any());

    }

    @Test
    void shouldPush(@TempDir Path repositoryDir, @TempDir Path tmpDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    BackupMeta backupMeta = new BackupMeta();
                    backupMeta.addDataset("Test");
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
                            backupMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });
        Mockito.when(remoteBackupRepository.getDatasetMeta(
                        Mockito.any(), Mockito.any())
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L,"Test","1111"));
                    Files.writeString(
                            repositoryDir.resolve("Test").resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );

        Files.createFile(tmpDir.resolve("part1"));

        localBackupRepository.add("Test1","part1",tmpDir.resolve("part1"));

        InOrder inOrder = Mockito.inOrder(remoteBackupRepository);

        inOrder.verify(remoteBackupRepository).add(
                "Test1/",
                repositoryDir.resolve("Test1").resolve("part1")
        );
        inOrder.verify(remoteBackupRepository).add(
                "Test1/",
                repositoryDir.resolve("Test1").resolve("_meta.json")
        );
        inOrder.verify(remoteBackupRepository).add(
                "",
                repositoryDir.resolve("_meta.json")
        );

        Assertions.assertFalse(Files.exists(tmpDir.resolve("part1")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("_meta.json")));
        Assertions.assertFalse(Files.exists(repositoryDir.resolve("Test").resolve("part1")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));
    }


    @Test
    void shouldGetPart(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    BackupMeta backupMeta = new BackupMeta();
                    backupMeta.addDataset("Test");
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
                            backupMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });
        Mockito.when(remoteBackupRepository.getDatasetMeta(
                        Mockito.any(), Mockito.any())
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L,"Test","1111"));
                    Files.writeString(
                            repositoryDir.resolve("Test").resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );

        Files.createFile(repositoryDir.resolve("testFile"));
        Files.createFile(repositoryDir.resolve("Test").resolve("testFile"));

        Path part = localBackupRepository.getPart("Test","part0");

        Assertions.assertFalse(Files.exists(repositoryDir.resolve("testFile")));
        Assertions.assertFalse(Files.exists(repositoryDir.resolve("Test").resolve("testFile")));

        Assertions.assertTrue(Files.exists(repositoryDir.resolve("_meta.json")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));

        Mockito.verify(remoteBackupRepository)
                .getPart(
                        "Test",
                        "part0",
                        repositoryDir.resolve("Test"),
                        new PartMeta("part0", 10L,"Test","1111")
                );
    }
}
