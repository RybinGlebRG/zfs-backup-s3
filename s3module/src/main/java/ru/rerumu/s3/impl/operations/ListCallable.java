package ru.rerumu.s3.impl.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.services.S3RequestService;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ListCallable implements Callable<List<String>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String key;
    private final S3RequestService s3RequestService;

    public ListCallable(String key, S3RequestService s3RequestService) {
        this.key = key;
        this.s3RequestService = s3RequestService;
    }

    @Override
    public List<String> call() {
        logger.info(String.format("Searching for files with prefix '%s'",key));

        // TODO: pagination?
        ListObjectsResponse res  = s3RequestService.listObjects(key);
        List<S3Object> s3Objects = res.contents();

        List<String> keys = s3Objects.stream()
                .map(S3Object::key)
                .collect(Collectors.toCollection(ArrayList::new));

        logger.info(String.format("Found on S3:\n'%s'", keys));

        return keys;
    }
}
