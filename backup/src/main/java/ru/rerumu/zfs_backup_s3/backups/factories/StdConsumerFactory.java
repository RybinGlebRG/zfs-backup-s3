package ru.rerumu.zfs_backup_s3.backups.factories;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public interface StdConsumerFactory {

    Consumer<BufferedInputStream> getSendStdoutConsumer(String prefix);
    Consumer<BufferedOutputStream> getReceiveStdinConsumer(String prefix);
}
