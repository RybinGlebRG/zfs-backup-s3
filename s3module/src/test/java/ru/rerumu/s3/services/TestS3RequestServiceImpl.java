package ru.rerumu.s3.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.CompletedPart;

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
}
