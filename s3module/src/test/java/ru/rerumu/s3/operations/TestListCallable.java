package ru.rerumu.s3.operations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.impl.operations.ListCallable;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.requests.models.ListObject;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
    void shouldList()throws Exception{
        List<ListObject> listObjects = new ArrayList<>();
        listObjects.add(new ListObject("test1","\"111\"",1L));
        listObjects.add(new ListObject("test2","\"222\"",2L));
        listObjects.add(new ListObject("test3","\"333\"",3L));

        when(s3RequestService.listObjects(anyString())).thenReturn(listObjects);


        Callable<List<String>> callable = new ListCallable("test-key",s3RequestService);
        List<String> res = callable.call();

        List<String> expected = List.of("test1","test2","test3");

        Assertions.assertEquals(expected,res);

        verify(s3RequestService).listObjects("test-key");
    }
}
