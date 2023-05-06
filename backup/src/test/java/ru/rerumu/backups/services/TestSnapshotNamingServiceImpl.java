package ru.rerumu.backups.services;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.impl.SnapshotNamingServiceImpl;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class TestSnapshotNamingServiceImpl {

    @Test
    void shouldGenerateName1() throws Exception{
        SnapshotNamingServiceImpl snapshotNamingService = new SnapshotNamingServiceImpl();
        String generatedName = snapshotNamingService.generateName();

        Assertions.assertTrue(generatedName.matches(
                "^zfs-backup-s3__\\d{4}-\\d{2}-\\d{2}T\\d{6}$"
        ));
    }

    @Test
    void shouldGenerateName2() throws Exception{
        SnapshotNamingServiceImpl snapshotNamingService = new SnapshotNamingServiceImpl();
        String generatedName = snapshotNamingService.generateName(LocalDateTime.of(2023,5,3,2,10,0));

        Assertions.assertTrue(generatedName.matches(
                "^zfs-backup-s3__2023-05-03T021000$"
        ));
    }

    @Test
    void shouldExtractTime()throws Exception{
        SnapshotNamingServiceImpl snapshotNamingService = new SnapshotNamingServiceImpl();
        LocalDateTime res = snapshotNamingService.extractTime("zfs-backup-s3__2023-05-03T021000");

        Assertions.assertEquals(LocalDateTime.of(2023,5,3,2,10,0), res);
    }
}
