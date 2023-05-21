package ru.rerumu.s3.operations;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.impl.complex_operations.MultipartDownloadCallable;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.requests.models.ListObject;
import ru.rerumu.utils.MD5;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestMultipartDownloadCallable {

    @Mock
    S3RequestService s3RequestService;

    @Test
    void shouldDownloadSmall(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        byte[] data = new byte[1000];
        new Random().nextBytes(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3RequestService.getMetadata("test-key")).thenReturn(
                new ListObject("test-key",MD5.getMD5Hex(data), (long) data.length)
        );
        when(s3RequestService.getObjectRange("test-key",0L,1000L,target))
                .thenReturn(MD5.getMD5Bytes(data));


        Callable<Void> callable = new MultipartDownloadCallable(
                target,
                "test-key",
                100_000_000,
                s3RequestService
        );
        callable.call();

        verifyNoMoreInteractions(s3RequestService);
    }

    @Test
    void shouldDownloadBig(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        byte[] data = new byte[1020];
        new Random().nextBytes(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3RequestService.getMetadata("test-key")).thenReturn(
                new ListObject("test-key",MD5.getMD5Hex(data), (long) data.length)
        );
        when(s3RequestService.getObjectRange("test-key",0L,500L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,0,500)));
        when(s3RequestService.getObjectRange("test-key",500L,1000L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,500,1000)));
        when(s3RequestService.getObjectRange("test-key",1000L,1020L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,1000,1020)));


        Callable<Void> callable = new MultipartDownloadCallable(
                target,
                "test-key",
                500,
                s3RequestService
        );
        callable.call();

        verifyNoMoreInteractions(s3RequestService);
    }

    @Test
    void shouldDownloadBigEdgeCase(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        byte[] data = new byte[1001];
        new Random().nextBytes(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        byte[] concatenatedMd5 = Stream.of(
                        MD5.getMD5Bytes(ArrayUtils.subarray(data,0,500)),
                        MD5.getMD5Bytes(ArrayUtils.subarray(data,500,1000)),
                        MD5.getMD5Bytes(ArrayUtils.subarray(data,1000,1001))
                )
                .reduce(new byte[0], ArrayUtils::addAll,ArrayUtils::addAll);
        String md5 = MD5.getMD5Hex(concatenatedMd5) + "-" + 3;

        when(s3RequestService.getMetadata("test-key")).thenReturn(
                new ListObject("test-key",md5, (long) data.length)
        );
        when(s3RequestService.getObjectRange("test-key",0L,500L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,0,500)));
        when(s3RequestService.getObjectRange("test-key",500L,1000L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,500,1000)));
        when(s3RequestService.getObjectRange("test-key",1000L,1001L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,1000,1001)));


        Callable<Void> callable = new MultipartDownloadCallable(
                target,
                "test-key",
                500,
                s3RequestService
        );
        callable.call();

        verifyNoMoreInteractions(s3RequestService);
    }

    @Test
    void shouldThrowExceptionDownloadSmall(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        byte[] data = new byte[1000];
        new Random().nextBytes(data);

        byte[] wrong = new byte[123];
        new Random().nextBytes(wrong);

        Files.write(
                target,
                wrong,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3RequestService.getMetadata("test-key")).thenReturn(
                new ListObject("test-key",MD5.getMD5Hex(data), (long) data.length)
        );
        when(s3RequestService.getObjectRange("test-key",0L,1000L,target))
                .thenReturn(MD5.getMD5Bytes(wrong));


        Callable<Void> callable = new MultipartDownloadCallable(
                target,
                "test-key",
                100_000_000,
                s3RequestService
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);
    }

    @Test
    void shouldThrowExceptionDownloadBig(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        byte[] data = new byte[1020];
        new Random().nextBytes(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        byte[] concatenatedMd5 = Stream.of(
                        MD5.getMD5Bytes(ArrayUtils.subarray(data,0,500)),
                        MD5.getMD5Bytes(ArrayUtils.subarray(data,500,1000)),
                        MD5.getMD5Bytes(ArrayUtils.subarray(data,1000,1001))
                )
                .reduce(new byte[0], ArrayUtils::addAll,ArrayUtils::addAll);
        String md5 = MD5.getMD5Hex(concatenatedMd5) + "-" + 3;

        when(s3RequestService.getMetadata("test-key")).thenReturn(
                new ListObject("test-key",md5, (long) data.length)
        );
        when(s3RequestService.getObjectRange("test-key",0L,500L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,0,500)));
        when(s3RequestService.getObjectRange("test-key",500L,1000L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,500,1000)));
        when(s3RequestService.getObjectRange("test-key",1000L,1020L,target))
                .thenReturn(MD5.getMD5Bytes(ArrayUtils.subarray(data,0,320)));


        Callable<Void> callable = new MultipartDownloadCallable(
                target,
                "test-key",
                500,
                s3RequestService
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);
    }

}
