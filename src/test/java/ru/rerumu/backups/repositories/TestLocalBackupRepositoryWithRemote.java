package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.FinishedFlagException;
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


public class TestLocalBackupRepositoryWithRemote {

    @Test
    void shouldClone(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.eq(repositoryDir)))
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
                Mockito.eq("Test"),Mockito.eq(repositoryDir.resolve("Test")))
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L));
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
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
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("_meta.json")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));
    }

    @Test
    void shouldGetDatasets(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.eq(repositoryDir)))
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
                        Mockito.eq("Test"),Mockito.eq(repositoryDir.resolve("Test")))
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L));
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
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

        List<String> datasets = localBackupRepository.getDatasets();

        Assertions.assertEquals(List.of("Test"),datasets);
    }

    @Test
    void shouldClearRepositoryOnlyParts(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.eq(repositoryDir)))
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
                        Mockito.eq("Test"),Mockito.eq(repositoryDir.resolve("Test")))
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L));
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });
        Mockito.when(remoteBackupRepository.getPart(
                        Mockito.eq("Test"),
                        Mockito.eq("part0"),
                        Mockito.eq(repositoryDir.resolve("Test"))
                ))
                .thenAnswer(invocationOnMock -> {
                   Files.createFile(repositoryDir.resolve("Test").resolve("part0"));
                   return repositoryDir.resolve("Test").resolve("part0");
                });

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );

        Files.createFile(repositoryDir.resolve("testFile"));
        Files.createDirectory(repositoryDir.resolve("testDir"));
        Files.createFile(repositoryDir.resolve("testDir").resolve("testFile"));

        Path path = localBackupRepository.getNextPart("Test",null);

        Assertions.assertFalse(Files.exists(repositoryDir.resolve("testFile")));
        Assertions.assertFalse(Files.exists(repositoryDir.resolve("testDir").resolve("testFile")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("_meta.json")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("part0")));
    }

    void shouldGetParts(){
        // TODO: write
    }

    @Test
    void shouldGetNextPart(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.eq(repositoryDir)))
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
                        Mockito.eq("Test"),Mockito.eq(repositoryDir.resolve("Test")))
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L));
                    datasetMeta.addPart(new PartMeta("part1", 10L));
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });
        Mockito.when(remoteBackupRepository.getPart(
                        Mockito.eq("Test"),
                        Mockito.eq("part0"),
                        Mockito.eq(repositoryDir.resolve("Test"))
                ))
                .thenReturn(repositoryDir.resolve("Test").resolve("part0"));
        Mockito.when(remoteBackupRepository.getPart(
                        Mockito.eq("Test"),
                        Mockito.eq("part1"),
                        Mockito.eq(repositoryDir.resolve("Test"))
                ))
                .thenReturn(repositoryDir.resolve("Test").resolve("part1"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );

        Path path = localBackupRepository.getNextPart("Test",null);

        Assertions.assertEquals(repositoryDir.resolve("Test").resolve("part0"),path);

        path = localBackupRepository.getNextPart("Test","part0");

        Assertions.assertEquals(repositoryDir.resolve("Test").resolve("part1"),path);

        Assertions.assertThrows(NoMorePartsException.class,()->{
            localBackupRepository.getNextPart("Test","part1");
        });
    }

    @Test
    void shouldNotFindAny(@TempDir Path repositoryDir) throws Exception {
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.eq(repositoryDir)))
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
                        Mockito.eq("Test"),Mockito.eq(repositoryDir.resolve("Test")))
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
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

        Assertions.assertThrows(NoMorePartsException.class,()->{
            localBackupRepository.getNextPart("Test",null);
        });
    }

    @Test
    void shouldFinish(@TempDir Path repositoryDir) throws Exception{
        RemoteBackupRepository remoteBackupRepository = Mockito.mock(RemoteBackupRepository.class);

        Mockito.when(remoteBackupRepository.getBackupMeta(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    Path targetDir = (Path)args[0];
                    BackupMeta backupMeta = new BackupMeta();
                    backupMeta.addDataset("Test");
                    Files.writeString(
                            targetDir.resolve("_meta.json"),
                            backupMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return targetDir.resolve("_meta.json");
                });
        Mockito.when(remoteBackupRepository.getDatasetMeta(
                        Mockito.eq("Test"),Mockito.eq(repositoryDir.resolve("Test")))
                )
                .thenAnswer(invocationOnMock -> {
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L));
                    Files.writeString(
                            repositoryDir.resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return repositoryDir.resolve("_meta.json");
                });

        Mockito.when(remoteBackupRepository.getDatasetMeta(Mockito.any(),Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    String datasetName = (String)args[0];
                    Path targetDir = (Path)args[1];
                    DatasetMeta datasetMeta = new DatasetMeta();
                    datasetMeta.addPart(new PartMeta("part0", 10L));
                    Files.writeString(
                            targetDir.resolve("_meta.json"),
                            datasetMeta.toJSONObject().toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                    return targetDir.resolve("_meta.json");
                });


        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                remoteBackupRepository,
                true
        );

        Files.createFile(repositoryDir.resolve("finished"));


        Assertions.assertThrows(FinishedFlagException.class,()->{
            localBackupRepository.getNextPart("Test",null);
        });

        InOrder inOrder = Mockito.inOrder(remoteBackupRepository);

        inOrder.verify(remoteBackupRepository).getBackupMeta(repositoryDir);
        inOrder.verify(remoteBackupRepository).getDatasetMeta("Test",repositoryDir.resolve("Test"));

    }
}
