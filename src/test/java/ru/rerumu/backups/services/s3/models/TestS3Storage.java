package ru.rerumu.backups.services.s3.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.services.s3.models.S3Storage;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Paths;

class TestS3Storage {

    @Test
    void shouldGetAttributes() throws Exception{
        S3Storage s3Storage = new S3Storage(
                Region.EU_NORTH_1,
                "bucket",
                "1111",
                "2222",
                Paths.get("prefix"),
                new URI("https://endpoint.example"),
                "standard"
        );

        Assertions.assertEquals(Region.EU_NORTH_1,s3Storage.getRegion());
        Assertions.assertEquals("standard",s3Storage.getStorageClass());
        Assertions.assertEquals("bucket",s3Storage.getBucketName());
        Assertions.assertEquals(AwsBasicCredentials.create("1111","2222"),s3Storage.getCredentials());
        Assertions.assertEquals(new URI("https://endpoint.example"),s3Storage.getEndpoint());
        Assertions.assertEquals(Paths.get("prefix"),s3Storage.getPrefix());
    }
}