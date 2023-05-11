package ru.rerumu.s3.repositories.impl.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.utils.MD5;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
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

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {

            byte[] buf = bufferedInputStream.readAllBytes();
            String md5 = MD5.getMD5Hex(buf);

            PutObjectResponse putObjectResponse = s3RequestService.putObject(key,buf);

            String eTag = putObjectResponse.eTag();
            logger.info(String.format("ETag='%s'", eTag));
            if (!(eTag.equals('"' + md5 + '"'))) {
                throw new IncorrectHashException();
            }
        }
        return null;
    }
}
