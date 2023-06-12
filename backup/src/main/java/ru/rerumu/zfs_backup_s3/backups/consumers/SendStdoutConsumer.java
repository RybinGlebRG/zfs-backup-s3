package ru.rerumu.zfs_backup_s3.backups.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import java.io.BufferedInputStream;
import java.util.Objects;
import java.util.function.Consumer;

public class SendStdoutConsumer implements Consumer<BufferedInputStream> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Service s3Service;
    private final String prefix;

    public SendStdoutConsumer(S3Service s3Service, String prefix) {
        Objects.requireNonNull(s3Service);
        Objects.requireNonNull(prefix);
        this.s3Service = s3Service;
        this.prefix = prefix;
    }

    @Override
    public void accept(BufferedInputStream bufferedInputStream) {
        try {
            s3Service.upload(bufferedInputStream,prefix);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
