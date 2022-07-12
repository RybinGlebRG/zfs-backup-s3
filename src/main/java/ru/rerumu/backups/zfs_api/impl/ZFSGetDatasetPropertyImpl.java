package ru.rerumu.backups.zfs_api.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSGetDatasetProperty;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSGetDatasetPropertyImpl implements ZFSGetDatasetProperty {

    private final ProcessWrapper processWrapper;

    public ZFSGetDatasetPropertyImpl(
            String propertyName,
            String datasetName,
            ProcessWrapperFactory processWrapperFactory
    ) throws IOException {

        processWrapper = processWrapperFactory.getProcessWrapper(
                List.of(
                        "zfs","get","-Hp","-d","0","-t","filesystem,volume","-o","value",propertyName,datasetName
                )
        );
        processWrapper.run();
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        return processWrapper.getBufferedInputStream();
    }

    @Override
    public void close() throws InterruptedException, IOException, ExecutionException {
        processWrapper.close();
    }
}
