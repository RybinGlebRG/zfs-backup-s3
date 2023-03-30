package ru.rerumu.backups.zfs_api.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;

import java.io.IOException;
import java.util.List;

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
