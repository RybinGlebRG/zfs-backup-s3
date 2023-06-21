
package ru.rerumu.zfs_backup_s3.utils.processes;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

@ThreadSafe
public class StdLineConsumer implements Consumer<BufferedInputStream> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Consumer<String> consumer;

    public StdLineConsumer(@NonNull Consumer<String> consumer ) {
        Objects.requireNonNull(consumer,"consumer cannot be null");
        this.consumer = consumer;
    }

    @Override
    public synchronized void accept(BufferedInputStream bufferedInputStream) {
        logger.info("Started reading std");
        try (InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String s = null;
            while ((s = bufferedReader.readLine()) != null) {
                consumer.accept(s);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        logger.info("Finished reading std");
    }
}
