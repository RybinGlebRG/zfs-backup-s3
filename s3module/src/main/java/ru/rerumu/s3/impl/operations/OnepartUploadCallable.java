package ru.rerumu.s3.impl.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.services.S3RequestService;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class OnepartUploadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3RequestService s3RequestService;


    public OnepartUploadCallable(Path path, String key, S3RequestService s3RequestService) {
        this.path = path;
        this.key = key;
        this.s3RequestService = s3RequestService;
    }

    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        s3RequestService.putObject(path,key);
        return null;
    }
}
