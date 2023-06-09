package ru.rerumu.s3.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestS3RequestServiceImpl {

    @Mock
    CallableExecutor callableExecutor;

    @Mock
    CallableSupplierFactory factory;

    @Test
    void shouldUploadPart(){
        S3RequestServiceImpl service = new S3RequestServiceImpl(
                callableExecutor,
                factory
        );

        byte[] data = new byte[12345];
        new Random().nextBytes(data);

        service.uploadPart("test-key","test-upload",1,data);


        verify(factory).getUploadPartSupplier("test-key","test-upload",1,data);
        verify(callableExecutor).callWithRetry(any());

        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(callableExecutor);
    }

    @Test
    void shouldCreateMultipartUpload(){
        S3RequestServiceImpl service = new S3RequestServiceImpl(
                callableExecutor,
                factory
        );

        service.createMultipartUpload("test-key");

        verify(factory).getCreateMultipartUploadSupplier("test-key");
        verify(callableExecutor).callWithRetry(any());

        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(callableExecutor);
    }

    @Test
    void shouldAbortMultipartUpload(){
        S3RequestServiceImpl service = new S3RequestServiceImpl(
                callableExecutor,
                factory
        );

        service.abortMultipartUpload("test-key","test-upload");

        verify(factory).getAbortMultipartUploadSupplier("test-key","test-upload");
        verify(callableExecutor).callWithRetry(any());

        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(callableExecutor);
    }

    @Test
    void shouldCompleteMultipartUpload(){
        S3RequestServiceImpl service = new S3RequestServiceImpl(
                callableExecutor,
                factory
        );
        List<CompletedPart> completedPartList = new ArrayList<>();
        List<byte[]> md5List = new ArrayList<>();

        service.completeMultipartUpload(completedPartList,"test-key","test-upload",md5List);

        verify(factory).getCompleteMultipartUploadSupplier(completedPartList,"test-key","test-upload",md5List);
        verify(callableExecutor).callWithRetry(any());

        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(callableExecutor);
    }

    @Test
    void shouldListObjects(){
        ListObjectsResponse response = mock(ListObjectsResponse.class);
        List<S3Object> s3Objects = new ArrayList<>();
        s3Objects.add(S3Object.builder().eTag("1").size(1L).key("1").build());
        s3Objects.add(S3Object.builder().eTag("2").size(2L).key("2").build());
        s3Objects.add(S3Object.builder().eTag("3").size(3L).key("3").build());

        when(callableExecutor.callWithRetry(any())).thenReturn(response);
        when(response.contents()).thenReturn(s3Objects);
        when(response.isTruncated()).thenReturn(false);

        S3RequestServiceImpl service = new S3RequestServiceImpl(
                callableExecutor,
                factory
        );

        service.listObjects("test-key");

        verify(factory).getListObjectSupplier("test-key");
        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(callableExecutor);
        verifyNoMoreInteractions(response);
    }

    @Test
    void shouldListObjectsTruncated(){
        ListObjectsResponse response = mock(ListObjectsResponse.class);
        ListObjectsResponse response1 = mock(ListObjectsResponse.class);
        List<S3Object> s3Objects = new ArrayList<>();
        s3Objects.add(S3Object.builder().eTag("1").size(1L).key("1").build());
        s3Objects.add(S3Object.builder().eTag("2").size(2L).key("2").build());

        List<S3Object> s3Objects1 = new ArrayList<>();
        s3Objects1.add(S3Object.builder().eTag("3").size(3L).key("3").build());
        s3Objects1.add(S3Object.builder().eTag("4").size(4L).key("4").build());

        when(callableExecutor.callWithRetry(any()))
                .thenReturn(response)
                .thenReturn(response1);
        when(response.contents()).thenReturn(s3Objects);
        when(response.isTruncated()).thenReturn(true);
        when(response.nextMarker()).thenReturn("3");
        when(response1.contents()).thenReturn(s3Objects1);
        when(response1.isTruncated()).thenReturn(false);

        S3RequestServiceImpl service = new S3RequestServiceImpl(
                callableExecutor,
                factory
        );

        service.listObjects("test-key");

        verify(factory).getListObjectSupplier("test-key");
        verify(factory).getListObjectSupplier("test-key","3");
        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(callableExecutor);
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(response1);
    }
}
