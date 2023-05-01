package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.zfs.consumers.GetDatasetStringStdConsumer;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.consumers.SendStdoutConsumer;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.List;

public class StdConsumerFactoryImpl implements StdConsumerFactory {
    @Override
    public TriConsumer<BufferedInputStream, Runnable, Runnable> getSendStdoutConsumer(S3StreamRepositoryImpl s3StreamRepository, String prefix) {
        return new SendStdoutConsumer(s3StreamRepository,prefix);
    }

    @Override
    public TriConsumer<BufferedInputStream, Runnable, Runnable> getDatasetStringStdConsumer(List<String> res) {
        return new GetDatasetStringStdConsumer(res);
    }
}
