package ru.rerumu.zfs_backup_s3.backups.factories.impl;

import ru.rerumu.zfs_backup_s3.backups.consumers.ReceiveStdinConsumer;
import ru.rerumu.zfs_backup_s3.backups.consumers.SendStdoutConsumer;
import ru.rerumu.zfs_backup_s3.backups.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

// TODO: Check thread safe
public class StdConsumerFactoryImpl implements StdConsumerFactory {

    private final S3Service s3Service;

    public StdConsumerFactoryImpl(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public Consumer<BufferedInputStream> getSendStdoutConsumer(String prefix) {
        return new SendStdoutConsumer(s3Service,prefix);
    }

    @Override
    public Consumer<BufferedOutputStream> getReceiveStdinConsumer(String prefix) {
        return new ReceiveStdinConsumer(s3Service,prefix);
    }
}
