package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.impl.ProcessWrapperImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSListSnapshots  {
    protected final Logger logger = LoggerFactory.getLogger(ZFSListSnapshots.class);

    private final ProcessWrapper processWrapper;

    public ZFSListSnapshots(String fileSystemName, ProcessWrapperFactory processWrapperFactory) throws IOException {

        processWrapper = processWrapperFactory.getProcessWrapper(
                List.of(
                        "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1", fileSystemName
                )
        );

        processWrapper.setStderrProcessor(logger::error);

        processWrapper.run();
    }

    public BufferedInputStream getBufferedInputStream() {
        return processWrapper.getBufferedInputStream();
    }

    public void close() throws InterruptedException, IOException, ExecutionException {
        processWrapper.close();
    }
}
