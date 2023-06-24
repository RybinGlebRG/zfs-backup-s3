package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

// TODO: Check thread safe
public class ListCallable implements Callable<List<String>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String key;
    private final S3RequestService s3RequestService;

    public ListCallable(
            @NonNull String key,
            @NonNull S3RequestService s3RequestService) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3RequestService);
        this.key = key;
        this.s3RequestService = s3RequestService;
    }

    @Override
    public List<String> call() {
        logger.info(String.format("Searching for files with prefix '%s'",key));

        List<ListObject> res  = s3RequestService.listObjects(key);

        List<String> keys = res.stream()
                .map(ListObject::key)
                .collect(Collectors.toCollection(ArrayList::new));

        logger.info(String.format("Found on S3:\n'%s'", keys));

        return keys;
    }
}
