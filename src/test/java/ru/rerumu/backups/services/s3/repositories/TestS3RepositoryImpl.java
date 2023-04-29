package ru.rerumu.backups.services.s3.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.s3.S3Service;
import ru.rerumu.backups.services.s3.repositories.impl.S3RepositoryImpl;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestS3RepositoryImpl {

    @Mock
    S3Service s3Service;


    @Test
    void shouldListAllUnordered() throws Exception {
        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Path.of(""),
                new URI("http://localhost:9090/"),
                "STANDARD"
        );
        List<String> keys = new ArrayList<>();
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part12");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part11");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part0");


        when(s3Service.list(anyString()))
                .thenReturn(keys);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3Storage,s3Service);
        List<String> res = s3Repository.listAll("test-bucket/test-pool/level-0/zfs-backup-s3");

//        verify(s3Service).list("test-bucket/test-pool/level-0/zfs-backup-s3");

        List<String> shouldKeys = new ArrayList<>();
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part0");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part11");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part12");
        Assertions.assertEquals(shouldKeys,res);
    }
}
