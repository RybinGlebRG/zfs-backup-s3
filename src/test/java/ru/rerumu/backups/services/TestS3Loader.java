package ru.rerumu.backups.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

public class TestS3Loader {

    @Disabled
    @Test
    void shouldSend(@TempDir Path tempDir) throws URISyntaxException, IOException {
        S3LoaderImpl s3Loader = new S3LoaderImpl();
        s3Loader.addStorage(new S3Storage(
                Region.of("***"),
                "***",
                "***",
                "***",
                Paths.get("***"),
                new URI("***"),
                "***"
        ));
        s3Loader.addStorage(new S3Storage(
                Region.of("***"),
                "***",
                "***",
                "***",
                Paths.get("***"),
                new URI("***"),
                "***"
        ));

        String src =  "TestTestTst";
        byte[] srcByte = src.getBytes(StandardCharsets.UTF_8);
        Path path = Files.createFile(tempDir.resolve("test"));

        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(path))){
            bufferedOutputStream.write(srcByte);
        }
        s3Loader.upload("Test",path);
    }
}
