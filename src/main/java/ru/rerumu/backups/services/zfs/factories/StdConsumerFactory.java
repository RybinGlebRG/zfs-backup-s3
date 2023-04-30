package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;

public interface StdConsumerFactory {

    TriConsumer<BufferedInputStream,Runnable,Runnable> getSendStdoutConsumer(
            S3StreamRepositoryImpl s3StreamRepository,
            String prefix
    );
}
