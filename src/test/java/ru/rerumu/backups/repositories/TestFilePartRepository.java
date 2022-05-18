package ru.rerumu.backups.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class TestFilePartRepository {

    @Test
    void shouldGetNext(@TempDir Path tempDir) throws IOException, FinishedFlagException, NoMorePartsException, TooManyPartsException {
        Path srcFile = tempDir.resolve("level_0_25_02_2020__20_50.part0.ready");
        Files.createFile(srcFile);

        byte[] src = new byte[1000];
        new Random().nextBytes(src);
        try(OutputStream outputStream = Files.newOutputStream(srcFile)){
            outputStream.write(src);
        }

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);
        byte[] dst;
        Path nextPath = filePartRepository.getNextInputPath();
        try(InputStream inputStream = Files.newInputStream(nextPath)){
            dst = inputStream.readAllBytes();
        }

        Assertions.assertArrayEquals(src,dst);
    }

    @Test
    void shouldNotGetNext(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);

        Assertions.assertThrows(NoMorePartsException.class, filePartRepository::getNextInputPath);
    }

    @Test
    void shouldThrowTooMany(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part1.ready"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);

        Assertions.assertThrows(TooManyPartsException.class, filePartRepository::getNextInputPath);
    }

    @Test
    void shouldThrowTooManyFinished(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));
        Files.createFile(tempDir.resolve("finished"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);

        Assertions.assertThrows(TooManyPartsException.class, filePartRepository::getNextInputPath);
    }

    @Test
    void shouldThrowFinished(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("finished"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);

        Assertions.assertThrows(FinishedFlagException.class, filePartRepository::getNextInputPath);
    }

    @Test
    void shouldFillNewFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);
        byte[] src = new byte[1000];
        new Random().nextBytes(src);
        Path newPath = filePartRepository.createNewFilePath("level_0_25_02_2020__20_50",0);
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

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);
        Path nextPath = filePartRepository.getNextInputPath();

        filePartRepository.markReceived(nextPath);

        Assertions.assertFalse(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready")));
        Assertions.assertTrue(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.received")));

    }

    @Test
    void shouldMarkReady(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("level_0_25_02_2020__20_50.part0");
        Files.createFile(path);

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);
        filePartRepository.markReady(path);

        Assertions.assertFalse(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0")));
        Assertions.assertTrue(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready")));

    }

    @Test
    void shouldDelete(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("level_0_25_02_2020__20_50.part0.ready");
        Files.createFile(path);

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir);

        filePartRepository.delete(path);

        Assertions.assertFalse(Files.exists(path));

    }
}
