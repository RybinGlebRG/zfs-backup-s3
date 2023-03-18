package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;

import java.io.IOException;
import java.util.List;

public class ZFSSendReplicate extends ZFSAbstractSend {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ZFSSendReplicate(Snapshot snapshot, ProcessWrapperFactory processWrapperFactory) throws IOException {
        super(processWrapperFactory.getProcessWrapper(
                List.of("zfs","send","-vpRPw",snapshot.getFullName())
        ));
        processWrapper.run();
        processWrapper.setStderrProcessor(logger::debug);
    }

}
