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
    }

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

        backupMeta.addDataset("Test1");
        BackupMeta res = new BackupMeta(readJson(repositoryDir.resolve("_meta.json")));

        DatasetMeta srcDatasetMeta = new DatasetMeta();
        // TODO: write

        Assertions.assertEquals(backupMeta, res);
    }

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
        datasetMeta.addPart(new PartMeta("part0", 10L));

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

        localBackupRepository.add("Test", "part1", newPart);

        BackupMeta res = new BackupMeta(readJson(repositoryDir.resolve("_meta.json")));

        Assertions.assertEquals(backupMeta, res);
    }

    @Test
    void shouldAddPartToNotExistingDataset(@TempDir Path tmpDir, @TempDir Path repositoryDir) throws Exception {
        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(
                repositoryDir,
                null,
                false
        );

        Path newPart = tmpDir.resolve("part0");
        Files.createFile(newPart);

        localBackupRepository.add("Test", "part0", newPart);


        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("_meta.json")));
        DatasetMeta res = new DatasetMeta(readJson(repositoryDir.resolve("Test").resolve("_meta.json")));

        DatasetMeta datasetMeta = new DatasetMeta();
        datasetMeta.addPart(new PartMeta("part0", 0L));

        Assertions.assertEquals(datasetMeta, res);
    }

    @Test
    void shouldAddPartToExistingDataset(@TempDir Path tmpDir, @TempDir Path repositoryDir) throws Exception {
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
        datasetMeta.addPart(new PartMeta("part0", 10L));

        Files.writeString(
                repositoryDir.resolve("Test").resolve("_meta.json"),
                datasetMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        Files.createFile(tmpDir.resolve("part1"));

        localBackupRepository.add("Test", "part1", tmpDir.resolve("part1"));

        datasetMeta.addPart(new PartMeta("part1", 20L));

        Assertions.assertTrue(Files.exists(repositoryDir.resolve("Test").resolve("part1")));

        DatasetMeta resDatasetMeta = new DatasetMeta(readJson(repositoryDir.resolve("Test").resolve("_meta.json")));

        Assertions.assertEquals(datasetMeta, resDatasetMeta);
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
