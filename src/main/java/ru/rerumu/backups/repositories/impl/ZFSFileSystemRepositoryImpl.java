package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.services.impl.ZFSProcessFactoryImpl;
import ru.rerumu.backups.zfs_api.ZFSListFilesystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZFSFileSystemRepositoryImpl implements ZFSFileSystemRepository {

    private final Logger logger = LoggerFactory.getLogger(ZFSFileSystemRepositoryImpl.class);
    private final ZFSProcessFactoryImpl zfsProcessFactory;

    public ZFSFileSystemRepositoryImpl(ZFSProcessFactoryImpl zfsProcessFactory){
        this.zfsProcessFactory = zfsProcessFactory;
    }

    @Override
    public List<ZFSFileSystem> getFilesystemsTreeList(ZFSFileSystem zfsFileSystem) throws IOException, InterruptedException {
        ZFSListFilesystems zfsListFilesystems = zfsProcessFactory.getZFSListFilesystems(zfsFileSystem);
        byte[] buf = zfsListFilesystems.getBufferedInputStream().readAllBytes();
        zfsListFilesystems.close();

        String str = Arrays.toString(buf);
        String[] lines = str.split("\\n");

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();

        // Already sorted
        for (String line: lines){
                zfsFileSystemList.add(new ZFSFileSystem(line));
        }

        return zfsFileSystemList;

    }
}
