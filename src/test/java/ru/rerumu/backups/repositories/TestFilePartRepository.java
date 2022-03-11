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

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");
        byte[] dst;
        try(InputStream inputStream = filePartRepository.getNextInputStream()){
            dst = inputStream.readAllBytes();
        }

        Assertions.assertArrayEquals(src,dst);
    }

    @Test
    void shouldNotGetNext(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");

        Assertions.assertThrows(NoMorePartsException.class, filePartRepository::getNextInputStream);
    }

    @Test
    void shouldThrowTooMany(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part1.ready"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");

        Assertions.assertThrows(TooManyPartsException.class, filePartRepository::getNextInputStream);
    }

    @Test
    void shouldThrowTooManyFinished(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));
        Files.createFile(tempDir.resolve("finished"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");

        Assertions.assertThrows(TooManyPartsException.class, filePartRepository::getNextInputStream);
    }

    @Test
    void shouldThrowFinished(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("finished"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");

        Assertions.assertThrows(FinishedFlagException.class, filePartRepository::getNextInputStream);
    }

    @Test
    void shouldFillNewFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");
        byte[] src = new byte[1000];
        new Random().nextBytes(src);
        try(BufferedOutputStream outputStream = filePartRepository.newPart()){
            outputStream.write(src);
        }

        Path lastPart = filePartRepository.getLastPart();
        byte[] dst;
        try(InputStream inputStream = Files.newInputStream(lastPart)){
            dst = inputStream.readAllBytes();
        }

        Assertions.assertArrayEquals(src,dst);
    }

    @Test
    void shouldMarkReceived(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");
        byte[] dst;
        try(InputStream inputStream = filePartRepository.getNextInputStream()){
            dst = inputStream.readAllBytes();
        }

        filePartRepository.markReceivedLastPart();

        Assertions.assertFalse(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready")));
        Assertions.assertTrue(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.received")));

    }

    @Test
    void shouldDeleteLastPart(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready"));

        FilePartRepository filePartRepository = new FilePartRepositoryImpl(tempDir, "level_0_25_02_2020__20_50");
        byte[] dst;
        try(InputStream inputStream = filePartRepository.getNextInputStream()){
            dst = inputStream.readAllBytes();
        }

        filePartRepository.deleteLastPart();

        Assertions.assertFalse(Files.exists(tempDir.resolve("level_0_25_02_2020__20_50.part0.ready")));

    }
}
