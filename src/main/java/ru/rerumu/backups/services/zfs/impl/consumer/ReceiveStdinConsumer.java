package ru.rerumu.backups.services.zfs.impl.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedOutputStream;

public class ReceiveStdinConsumer implements TriConsumer<BufferedOutputStream, Runnable,Runnable> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3StreamRepositoryImpl s3StreamRepository;
    private final String prefix;

    public ReceiveStdinConsumer(S3StreamRepositoryImpl s3StreamRepository, String prefix) {
        this.s3StreamRepository = s3StreamRepository;
        this.prefix = prefix;
    }

    @Override
    public void accept(BufferedOutputStream bufferedOutputStream, Runnable close, Runnable kill) {
        try {
            s3StreamRepository.getAll(bufferedOutputStream, prefix);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            kill.run();
            throw new RuntimeException(e);
        } finally {
            close.run();
        }
    }
}
