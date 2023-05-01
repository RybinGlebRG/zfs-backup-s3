package ru.rerumu.backups.services.zfs.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GetDatasetStringStdConsumer implements TriConsumer<BufferedInputStream,Runnable,Runnable> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<String> res;

    public GetDatasetStringStdConsumer(List<String> res) {
        this.res = res;
    }

    @Override
    public void accept(BufferedInputStream bufferedInputStream, Runnable close, Runnable kill) {
        try {
            byte[] output = bufferedInputStream.readAllBytes();
            String str = new String(output, StandardCharsets.UTF_8);
            logger.debug(String.format("Got from process: \n%s",str));
            String[] lines = str.split("\\n");

            Arrays.stream(lines)
                    .map(String::strip)
                    .peek(item -> logger.debug(String.format("Got dataset name: %s",item)))
                    .forEach(res::add);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            kill.run();
            throw new RuntimeException(e);
        }
    }
}
