package ru.rerumu.backups.repositories;

import org.json.JSONObject;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


public class TestLocalBackupRepositoryLocalOnly {

    private JSONObject readJson(Path path) throws IOException {
        String jsonString;
        try (InputStream inputStream = Files.newInputStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            jsonString = new String(bufferedInputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        return new JSONObject(jsonString);
    }

    /**
     * Test checks addition of the new part of new dataset to empty local repository.
     * New metadata files should be created, directory for new dataset should be created,
     * new part should be added to the new dataset directory.
     * Backup metadata should contain only new dataset.
     * Dataset metadata should contain only new part.
     * New part should be present in dataset directory.
     *
     */
    @Test
    void shouldAddPartToNotExistingBackup(@TempDir Path tmpDir, @TempDir Path repositoryDir) throws Exception {
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

        Path newPart = tmpDir.resolve("part0");
        Files.createFile(newPart);

        localBackupRepository.add("Test", "part0", newPart);

        BackupMeta backupMeta = new BackupMeta();
        backupMeta.addDataset("Test");
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("_meta.json")));
        BackupMeta resBackupMeta = new BackupMeta(readJson(repositoryDir.resolve("_meta.json")));

        DatasetMeta datasetMeta = new DatasetMeta();
        datasetMeta.addPart(new PartMeta("part0", 0L));
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));
        DatasetMeta resDatasetMeta = new DatasetMeta(readJson(repositoryDir.resolve("Test").resolve("_meta.json")));
        Assertions.assertEquals(backupMeta, resBackupMeta);
        Assertions.assertEquals(datasetMeta, resDatasetMeta);
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("part0")));
    }

    /**
     * Test checks addition of the new part of new dataset to not empty local repository.
     * Directory for new dataset should be created.
     * New metadata file for new dataset should be created in new dataset directory.
     * New part should be added to the new dataset directory.
     * New dataset should be added to backup metadata.
     * Dataset metadata should contain only new part.
     *
     */
    @Test
    void shouldAddDatasetToExistingBackup(@TempDir Path tmpDir, @TempDir Path repositoryDir) throws Exception {
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

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

        DatasetMeta datasetMeta = new DatasetMeta();
        datasetMeta.addPart(new PartMeta("part0",10L));
        Files.createDirectory(repositoryDir.resolve("Test"));
        Files.writeString(
                repositoryDir.resolve("Test").resolve("_meta.json"),
                datasetMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );


        Path newPart = tmpDir.resolve("part1");
        Files.createFile(newPart);

        localBackupRepository.add("Test1", "part1", newPart);

        BackupMeta srcBackupMeta = new BackupMeta();
        srcBackupMeta.addDataset("Test");
        srcBackupMeta.addDataset("Test1");
        BackupMeta resBackupMeta = new BackupMeta(readJson(repositoryDir.resolve("_meta.json")));

        DatasetMeta srcDatasetMeta = new DatasetMeta();
        srcDatasetMeta.addPart(new PartMeta("part1",0L));
        DatasetMeta resDatasetMeta = new DatasetMeta(readJson(repositoryDir.resolve("Test1").resolve("_meta.json")));

        Assertions.assertEquals(srcBackupMeta,resBackupMeta);
        Assertions.assertEquals(srcDatasetMeta,resDatasetMeta);
        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test1").resolve("part1")));
    }

    /**
     * Test checks addition of the new part of existing dataset to not empty local repository.
     * New part should be added to the dataset directory.
     * New part should be added to dataset metadata.
     *
     */
    @Test
    void shouldAddPartToSameDataset(@TempDir Path tmpDir, @TempDir Path repositoryDir) throws Exception {
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

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
        Files.createDirectory(repositoryDir.resolve("Test"));
        DatasetMeta datasetMeta = new DatasetMeta();
        datasetMeta.addPart(new PartMeta("part0",10L));
        Files.writeString(
                repositoryDir.resolve("Test").resolve("_meta.json"),
                datasetMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        Files.createFile(tmpDir.resolve("part1"));

        localBackupRepository.add("Test","part1",tmpDir.resolve("part1"));

        BackupMeta srcBackupMeta = new BackupMeta();
        srcBackupMeta.addDataset("Test");
        BackupMeta resBackupMeta = new BackupMeta(readJson(repositoryDir.resolve("_meta.json")));

        DatasetMeta srcDatasetMeta = new DatasetMeta();
        srcDatasetMeta.addPart(new PartMeta("part0",10L));
        srcDatasetMeta.addPart(new PartMeta("part1",0L));
        DatasetMeta resDatasetMeta = new DatasetMeta(readJson(repositoryDir.resolve("Test").resolve("_meta.json")));

        Assertions.assertEquals(srcBackupMeta, resBackupMeta);
        Assertions.assertEquals(srcDatasetMeta, resDatasetMeta);
    }

    @Test
    void shouldGetDatasets(@TempDir Path repositoryDir) throws Exception {
        BackupMeta backupMeta = new BackupMeta();
        backupMeta.addDataset("Test");
        backupMeta.addDataset("Test1");
        backupMeta.addDataset("Test2");
        Files.writeString(
                repositoryDir.resolve("_meta.json"),
                backupMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

        List<String> datasets = localBackupRepository.getDatasets();

        Assertions.assertEquals(List.of("Test", "Test1", "Test2"), datasets);
    }

    @Test
    void shouldGetParts(@TempDir Path repositoryDir) throws Exception {
        Files.createDirectory(repositoryDir.resolve("Test"));
        DatasetMeta datasetMeta = new DatasetMeta();
        datasetMeta.addPart(new PartMeta("part0", 10L));
        datasetMeta.addPart(new PartMeta("1part0", 20L));
        datasetMeta.addPart(new PartMeta("1part1", 30L));
        Files.writeString(
                repositoryDir.resolve("Test").resolve("_meta.json"),
                datasetMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

        List<String> parts = localBackupRepository.getParts("Test");

        Assertions.assertEquals(List.of("part0", "1part0", "1part1"), parts);
    }

    @Test
    void shouldGetPart(@TempDir Path repositoryDir) throws Exception {
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

        Path part = localBackupRepository.getPart("Test", "part0");

        Assertions.assertEquals(repositoryDir.resolve("Test").resolve("part0"), part);

    }
}
