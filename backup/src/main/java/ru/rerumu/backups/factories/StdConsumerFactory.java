package ru.rerumu.backups.factories;

import ru.rerumu.s3.S3Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public interface StdConsumerFactory {

    Consumer<BufferedInputStream> getSendStdoutConsumer(String prefix);
    Consumer<BufferedOutputStream> getReceiveStdinConsumer(String prefix);
}
