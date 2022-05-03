package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.zfs_api.impl.ProcessWrapperImpl;

import java.io.IOException;
import java.util.Arrays;

public class ZFSListFilesystems extends ProcessWrapperImpl {
    protected final Logger logger = LoggerFactory.getLogger(ZFSListFilesystems.class);

    public ZFSListFilesystems(String parentFileSystem) throws IOException {
        super(Arrays.asList(
                "zfs","list","-rH","-t","filesystem","-o","name","-s","name",parentFileSystem
        ));
    }
}
