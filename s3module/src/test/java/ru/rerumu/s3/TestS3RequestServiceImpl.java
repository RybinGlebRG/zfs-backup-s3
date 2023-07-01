package ru.rerumu.s3;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;
import ru.rerumu.zfs_backup_s3.utils.callables.CallableExecutor;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestS3RequestServiceImpl {

    @Mock
    CallableExecutorImpl callableExecutor;

    @Mock
    CallableSupplierFactory callableSupplierFactory;

    @Test
    void shouldGetMetadata() {
        S3RequestServiceImpl s3RequestService = new S3RequestServiceImpl(callableExecutor, callableSupplierFactory);
        ListObjectsResponse listObjectsResponse = ListObjectsResponse.builder()
                .contents(
                        S3Object.builder().key("test.part1").eTag("1").size(1L).build(),
                        S3Object.builder().key("test.part11").eTag("11").size(11L).build(),
                        S3Object.builder().key("test.part13").eTag("13").size(13L).build()
                )
                .isTruncated(false)
                .build();
        when(callableExecutor.callWithRetry(any())).thenReturn(listObjectsResponse);

        ListObject listObject = s3RequestService.getMetadata("test.part1");

        Assertions.assertEquals("test.part1", listObject.key());
    }
}
