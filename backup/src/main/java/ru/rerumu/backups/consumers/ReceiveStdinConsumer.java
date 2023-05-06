package ru.rerumu.backups.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.repositories.impl.S3StreamRepositoryImpl;

import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public class ReceiveStdinConsumer implements Consumer<BufferedOutputStream> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3StreamRepositoryImpl s3StreamRepository;
    private final String prefix;

    public ReceiveStdinConsumer(S3StreamRepositoryImpl s3StreamRepository, String prefix) {
        this.s3StreamRepository = s3StreamRepository;
        this.prefix = prefix;
    }

    @Override
    public void accept(BufferedOutputStream bufferedOutputStream) {
        try {
            s3StreamRepository.getAll(bufferedOutputStream, prefix);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
