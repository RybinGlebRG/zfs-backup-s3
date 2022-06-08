package ru.rerumu.backups.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.io.impl.S3LoaderImpl;
import software.amazon.awssdk.regions.Region;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class TestS3Loader {

    @Disabled
    @Test
    void shouldSend(@TempDir Path tempDir) throws URISyntaxException, IOException, NoSuchAlgorithmException, IncorrectHashException {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.TRACE);
        S3LoaderImpl s3Loader = new S3LoaderImpl();
        s3Loader.addStorage(new S3Storage(
                Region.of(System.getProperty("region")),
                System.getProperty("bucket"),
                System.getProperty("keyId"),
                System.getProperty("secretKey"),
                Paths.get("level-0"),
                new URI(System.getProperty("endpoint")),
                "STANDARD"
        ));


        byte[] srcByte = new byte[5431];
        new Random().nextBytes(srcByte);
        Path path = Files.createFile(tempDir.resolve("test"));

        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(path))){
            bufferedOutputStream.write(srcByte);
        }
        s3Loader.upload("Test",path);
    }
}
