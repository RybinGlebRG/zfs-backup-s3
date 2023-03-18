package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSSendFullEncrypted extends ZFSAbstractSend {
    protected final Logger logger = LoggerFactory.getLogger(ZFSSendFullEncrypted.class);

//    private final ProcessWrapper processWrapper;

    public ZFSSendFullEncrypted(Snapshot fullSnapshot, ProcessWrapperFactory processWrapperFactory) throws IOException {
        super(processWrapperFactory.getProcessWrapper(
                List.of("zfs","send","-vpPw",fullSnapshot.getFullName())
        ));

        processWrapper.run();
        processWrapper.setStderrProcessor(logger::debug);
    }
}
