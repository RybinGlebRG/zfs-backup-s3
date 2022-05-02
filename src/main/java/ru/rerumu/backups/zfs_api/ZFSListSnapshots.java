package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.zfs_api.impl.ProcessWrapperImpl;

import java.io.IOException;
import java.util.Arrays;

public class ZFSListSnapshots extends ProcessWrapperImpl {
    protected final Logger logger = LoggerFactory.getLogger(ZFSListSnapshots.class);

    public ZFSListSnapshots(ZFSFileSystem zfsFileSystem) throws IOException {
        super(Arrays.asList(
                "zfs","list","-rH","-t","snapshot","-o","name","-s","name","-s","creation",zfsFileSystem.getName()
        ));
    }
}
