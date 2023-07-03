package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@ThreadSafe
public final class ListCallable extends CallableOnlyOnce<List<String>> {
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
    protected List<String> callOnce() {
        logger.info(String.format("Searching for files with prefix '%s'",key));

        List<ListObject> res  = s3RequestService.listObjects(key);

        List<String> keys = res.stream()
                .map(ListObject::key)
                .collect(Collectors.toCollection(ArrayList::new));

        logger.info(String.format("Found on S3:\n'%s'", keys));

        return keys;
    }
}
