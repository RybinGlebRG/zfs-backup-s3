package ru.rerumu.zfs_backup_s3.zfs;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

@ThreadSafe
public sealed interface ZFSServiceFactory permits ZFSServiceFactoryImpl {

    ZFSService getZFSService();
}
