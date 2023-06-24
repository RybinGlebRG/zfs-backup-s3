package ru.rerumu.zfs_backup_s3.backups.factories;

import ru.rerumu.zfs_backup_s3.backups.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

@NotThreadSafe
public sealed interface StdConsumerFactory permits StdConsumerFactoryImpl {

    Consumer<BufferedInputStream> getSendStdoutConsumer(String prefix);
    Consumer<BufferedOutputStream> getReceiveStdinConsumer(String prefix);
}
