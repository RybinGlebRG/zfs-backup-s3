module ru.rerumu.zfs_backup_s3.zfs {

    requires org.slf4j;
    requires ru.rerumu.zfs_backup_s3.utils;
    requires org.checkerframework.checker.qual;

    exports ru.rerumu.zfs_backup_s3.zfs.models to ru.rerumu.zfs_backup_s3.backup;
    exports ru.rerumu.zfs_backup_s3.zfs to ru.rerumu.zfs_backup_s3.backup;

}