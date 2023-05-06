package ru.rerumu.backups.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.repositories.impl.S3StreamRepositoryImpl;

import java.io.BufferedInputStream;
import java.util.function.Consumer;

public class SendStdoutConsumer implements Consumer<BufferedInputStream> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3StreamRepositoryImpl s3StreamRepository;
    private final String prefix;

    public SendStdoutConsumer(S3StreamRepositoryImpl s3StreamRepository, String prefix) {
        this.s3StreamRepository = s3StreamRepository;
        this.prefix = prefix;
    }

    @Override
    public void accept(BufferedInputStream bufferedInputStream) {
        try {
            s3StreamRepository.add(prefix, bufferedInputStream);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
