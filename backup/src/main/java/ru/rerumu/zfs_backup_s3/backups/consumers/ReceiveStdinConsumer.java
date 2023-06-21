package ru.rerumu.zfs_backup_s3.backups.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import java.io.BufferedOutputStream;
import java.util.function.Consumer;

// TODO: Check thread safe
public class ReceiveStdinConsumer implements Consumer<BufferedOutputStream> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Service s3Service;
    private final String prefix;

    public ReceiveStdinConsumer(S3Service s3Service, String prefix) {
        this.s3Service = s3Service;
        this.prefix = prefix;
    }

    @Override
    public void accept(BufferedOutputStream bufferedOutputStream) {
        try {
            s3Service.download(prefix,bufferedOutputStream);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
