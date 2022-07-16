package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;
import ru.rerumu.backups.repositories.impl.LocalBackupRepositoryImpl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;


public class TestLocalBackupRepository {

    @Test
    void shouldGetNext(@TempDir Path tempDir) throws IOException, FinishedFlagException, NoMorePartsException, TooManyPartsException {
        Path srcFile = tempDir.resolve("level_0_25_02_2020__20_50.part0.ready");
        Files.createFile(srcFile);

        byte[] src = new byte[1000];
        new Random().nextBytes(src);
        try(OutputStream outputStream = Files.newOutputStream(srcFile)){
            outputStream.write(src);
        }

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);
        byte[] dst;
        Path nextPath = localBackupRepository.getNextInputPath();
        try(InputStream inputStream = Files.newInputStream(nextPath)){
            dst = inputStream.readAllBytes();
        }

        Assertions.assertArrayEquals(src,dst);
    }

    @Test
    void shouldNotGetNext(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);

        Assertions.assertThrows(NoMorePartsException.class, localBackupRepository::getNextInputPath);
    }

    @Test
    void shouldThrowTooMany(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part1.ready"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);

        Assertions.assertThrows(TooManyPartsException.class, localBackupRepository::getNextInputPath);
    }

    @Test
    void shouldThrowTooManyFinished(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));
        Files.createFile(tempDir.resolve("finished"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);

        Assertions.assertThrows(TooManyPartsException.class, localBackupRepository::getNextInputPath);
    }

    @Test
    void shouldThrowFinished(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("finished"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);

        Assertions.assertThrows(FinishedFlagException.class, localBackupRepository::getNextInputPath);
    }

    @Test
    void shouldFillNewFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);
        byte[] src = new byte[1000];
        new Random().nextBytes(src);
        Path newPath = localBackupRepository.createNewFilePath("level_0_25_02_2020__20_50",0);
        try(OutputStream outputStream = Files.newOutputStream(newPath);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)){
            bufferedOutputStream.write(src);
        }

        byte[] dst;
        try(InputStream inputStream = Files.newInputStream(newPath)){
            dst = inputStream.readAllBytes();
        }

        Assertions.assertArrayEquals(src,dst);
    }

    @Test
    void shouldMarkReceived(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);
        Path nextPath = localBackupRepository.getNextInputPath();

        localBackupRepository.markReceived(nextPath);

        Assertions.assertFalse(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready")));
        Assertions.assertTrue(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.received")));

    }

    @Test
    void shouldMarkReady(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("level_0_25_02_2020__20_50.part0");
        Files.createFile(path);

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);
        localBackupRepository.markReady(path);

        Assertions.assertFalse(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0")));
        Assertions.assertTrue(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready")));

    }

    @Test
    void shouldDelete(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("level_0_25_02_2020__20_50.part0.ready");
        Files.createFile(path);

        LocalBackupRepository localBackupRepository = new LocalBackupRepositoryImpl(tempDir);

        localBackupRepository.delete(path);

        Assertions.assertFalse(Files.exists(path));

    }
}
