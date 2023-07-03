package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class OnepartUploadCallable extends CallableOnlyOnce<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3RequestService s3RequestService;


    public OnepartUploadCallable(
            @NonNull Path path,
            @NonNull  String key,
            @NonNull S3RequestService s3RequestService) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3RequestService);
        this.path = path;
        this.key = key;
        this.s3RequestService = s3RequestService;
    }

    @Override
    protected Void callOnce() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        s3RequestService.putObject(path,key);
        return null;
    }
}
