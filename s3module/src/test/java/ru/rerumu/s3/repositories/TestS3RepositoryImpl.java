package ru.rerumu.s3.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.factories.S3CallableFactory;
import ru.rerumu.s3.repositories.impl.S3RepositoryImpl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestS3RepositoryImpl {
    @Mock
    S3CallableFactory s3CallableFactory;

    @Test
    void shouldAddWithoutRetry() throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        Callable<List<String>> listCallable = (Callable<List<String>>)mock(Callable.class);

        when(s3CallableFactory.getUploadCallable(any(),anyString())).thenReturn((Callable<Void>)mock(Callable.class));
        when(s3CallableFactory.getListCallable(anyString())).thenReturn(listCallable);
        when(listCallable.call()).thenReturn(keys);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory);
        s3Repository.add("test-bucket/test-pool/level-0/", Paths.get("/tmp/zfs-backup-s3.part1"));

        verify(s3CallableFactory).getUploadCallable(
                Paths.get("/tmp/zfs-backup-s3.part1"),
                "test-bucket/test-pool/level-0/zfs-backup-s3.part1"
        );
        verify(s3CallableFactory,times(1)).getUploadCallable(any(),anyString());
        verify(s3CallableFactory,times(1)).getListCallable(anyString());
    }

    @Test
    void shouldAddWithRetry() throws Exception {
        List<String> keys1 = new ArrayList<>();
        keys1.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");

        List<String> keys2 = new ArrayList<>();
        keys2.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        keys2.add("test-bucket/test-pool/level-0/zfs-backup-s3.part2");

        Callable<List<String>> listCallable = (Callable<List<String>>) mock(Callable.class);
        Callable<Void> uploadCallable = (Callable<Void>) mock(Callable.class);

        when(s3CallableFactory.getUploadCallable(any(),anyString())).thenReturn(uploadCallable);
        when(s3CallableFactory.getListCallable(anyString())).thenReturn(listCallable);
        when(listCallable.call())
                .thenReturn(keys1)
                .thenReturn(keys2);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory);
        s3Repository.add("test-bucket/test-pool/level-0/", Paths.get("/tmp/zfs-backup-s3.part2"));

        verify(s3CallableFactory,times(2)).getUploadCallable(
                Paths.get("/tmp/zfs-backup-s3.part2"),
                "test-bucket/test-pool/level-0/zfs-backup-s3.part2"
        );
        verify(s3CallableFactory,times(2)).getListCallable("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
    }

    @Test
    void shouldThrowExceptionOnAdd(){

        when(s3CallableFactory.getUploadCallable(any(),anyString())).thenThrow(IllegalArgumentException.class);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory);

        Assertions.assertThrows(
                RuntimeException.class,
                ()->s3Repository.add("test-bucket/test-pool/level-0/", Paths.get("/tmp/zfs-backup-s3.part1"))
        );

    }

    @Test
    void shouldListAllUnordered() throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part12");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part11");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part0");
        Callable<List<String>> listCallable = (Callable<List<String>>) mock(Callable.class);

        when(s3CallableFactory.getListCallable(anyString())).thenReturn(listCallable);
        when(listCallable.call()).thenReturn(keys);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory);
        List<String> res = s3Repository.listAll("test-bucket/test-pool/level-0/zfs-backup-s3");

        List<String> shouldKeys = new ArrayList<>();
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part0");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part11");
        shouldKeys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part12");
        Assertions.assertEquals(shouldKeys, res);

        verify(s3CallableFactory).getListCallable("test-bucket/test-pool/level-0/zfs-backup-s3");
    }
}
