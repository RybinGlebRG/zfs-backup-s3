package ru.rerumu.zfs_backup_s3.zfs.consumers;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@ThreadSafe
public class GetDatasetStringStdConsumer implements Consumer<BufferedInputStream> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<String> res;

    public GetDatasetStringStdConsumer(@NonNull List<String> res) {
        Objects.requireNonNull(res, "List cannot be null");
        this.res = res;
    }

    private void validateLine(String line) {
        try {
            if (!line.matches("^([a-zA-Z0-9_-]+/?)+$")) {
                throw new IOException("Unacceptable line format");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void accept(@NonNull BufferedInputStream bufferedInputStream) {
        Objects.requireNonNull(bufferedInputStream, "Buffered input stream cannot be null");
        try {
            byte[] output = bufferedInputStream.readAllBytes();
            String str = new String(output, StandardCharsets.UTF_8);
            logger.debug(String.format("Got from process: \n%s",str));
            String[] lines = str.split("\\n");

            Arrays.stream(lines)
                    .map(String::strip)
                    .peek(this::validateLine)
                    .peek(item -> logger.debug(String.format("Got dataset name: %s",item)))
                    .forEach(res::add);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
