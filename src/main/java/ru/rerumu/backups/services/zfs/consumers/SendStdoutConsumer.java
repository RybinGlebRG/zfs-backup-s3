package ru.rerumu.backups.services.zfs.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;

public class SendStdoutConsumer implements TriConsumer<BufferedInputStream,Runnable,Runnable> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3StreamRepositoryImpl s3StreamRepository;
    private final String prefix;

    public SendStdoutConsumer(S3StreamRepositoryImpl s3StreamRepository, String prefix) {
        this.s3StreamRepository = s3StreamRepository;
        this.prefix = prefix;
    }

    @Override
    public void accept(BufferedInputStream bufferedInputStream, Runnable close, Runnable kill) {
        try {
            s3StreamRepository.add(prefix, bufferedInputStream);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            kill.run();
            throw new RuntimeException(e);
        }
    }
}
