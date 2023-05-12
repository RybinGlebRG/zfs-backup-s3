package ru.rerumu.s3.operations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.impl.operations.ListCallable;
import ru.rerumu.s3.services.S3RequestService;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestListCallable {

    @Mock
    S3RequestService s3RequestService;
    @Mock
    ListObjectsResponse listObjectsResponse;


    @Test
    void shouldList(){
        ListCallable callable = new ListCallable("test-key",s3RequestService);

        when(s3RequestService.listObjects(anyString())).thenReturn(listObjectsResponse);
        when(listObjectsResponse.contents()).thenReturn(List.of(
                S3Object.builder().key("test1").build(),
                S3Object.builder().key("test2").build(),
                S3Object.builder().key("test3").build()
        ));

        List<String> res = callable.call();

        List<String> expected = List.of("test1","test2","test3");

        Assertions.assertEquals(expected,res);

        verify(s3RequestService).listObjects("test-key");
    }
}
