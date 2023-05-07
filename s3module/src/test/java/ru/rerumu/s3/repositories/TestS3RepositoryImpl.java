package ru.rerumu.s3.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.factories.S3CallableFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.S3Service;
import ru.rerumu.s3.repositories.impl.S3RepositoryImpl;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Path;
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

    @Mock
    CallableExecutor callableExecutor;

    @Test
    void shouldAddWithoutRetry() throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");

        when(callableExecutor.callWithRetry(ArgumentMatchers.<Callable<Void>>any()))
                .thenReturn(null);

        when(callableExecutor.callWithRetry(ArgumentMatchers.<Callable<List<String>>>any()))
                .thenReturn(keys);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory, callableExecutor);
        s3Repository.add("test-bucket/test-pool/level-0/", Paths.get("/tmp/zfs-backup-s3.part1"));

        verify(s3CallableFactory).getUploadCallable(
                Paths.get("/tmp/zfs-backup-s3.part1"),
                "test-bucket/test-pool/level-0/zfs-backup-s3.part1"
        );
        verify(callableExecutor, times(2)).callWithRetry(any());
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

        when(callableExecutor.callWithRetry(uploadCallable))
                .thenReturn(null)
                .thenReturn(null);

        when(callableExecutor.callWithRetry(listCallable))
                .thenReturn(keys1)
                .thenReturn(keys2 );


        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory, callableExecutor);
        s3Repository.add("test-bucket/test-pool/level-0/", Paths.get("/tmp/zfs-backup-s3.part2"));

        verify(s3CallableFactory,times(2)).getUploadCallable(
                Paths.get("/tmp/zfs-backup-s3.part2"),
                "test-bucket/test-pool/level-0/zfs-backup-s3.part2"
        );
        verify(s3CallableFactory,times(2)).getListCallable("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
        verify(callableExecutor, times(4)).callWithRetry(any());
    }

    @Test
    void shouldListAllUnordered() throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part2");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part1");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part12");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part11");
        keys.add("test-bucket/test-pool/level-0/zfs-backup-s3.part0");

        when(callableExecutor.callWithRetry(any()))
                .thenReturn(keys);

        S3RepositoryImpl s3Repository = new S3RepositoryImpl(s3CallableFactory, callableExecutor);
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
