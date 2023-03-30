package ru.rerumu.backups.zfs_api.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;

import java.io.IOException;
import java.util.List;

public class ZFSSendMultiIncrementalEncrypted  extends ZFSAbstractSend {
    protected final Logger logger = LoggerFactory.getLogger(ZFSSendMultiIncrementalEncrypted.class);

//    private final ProcessWrapper processWrapper;

    public ZFSSendMultiIncrementalEncrypted(
            Snapshot baseSnapshot,
            Snapshot incrementalSnapshot,
            ProcessWrapperFactory processWrapperFactory)
            throws IOException {
//        super(Arrays.asList("zfs", "send", "-vpPIw", baseSnapshot.getFullName(), incrementalSnapshot.getFullName()));

//        setStderrProcessor(logger::debug);

       super(processWrapperFactory.getProcessWrapper(
                List.of("zfs", "send", "-vpPw", "-I",baseSnapshot.getFullName(), incrementalSnapshot.getFullName())
        ));

        processWrapper.run();
        processWrapper.setStderrProcessor(logger::debug);
    }
}
