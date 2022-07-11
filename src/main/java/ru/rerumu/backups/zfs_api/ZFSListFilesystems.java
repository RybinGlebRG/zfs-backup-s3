package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.impl.ProcessWrapperImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class ZFSListFilesystems {
    protected final Logger logger = LoggerFactory.getLogger(ZFSListFilesystems.class);

    private final ProcessWrapper processWrapper;

    public ZFSListFilesystems(String parentFileSystem, ProcessWrapperFactory processWrapperFactory) throws IOException {
//        super(Arrays.asList(
//                "zfs","list","-rH","-t","filesystem,volume","-o","name","-s","name",parentFileSystem
//        ));

        processWrapper = processWrapperFactory.getProcessWrapper(
                Arrays.asList(
                        "zfs","list","-rH","-t","filesystem,volume","-o","name","-s","name",parentFileSystem
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
