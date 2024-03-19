module ru.rerumu.zfs_backup_s3.s3module {

    requires org.slf4j;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires org.apache.commons.lang3;
    requires ru.rerumu.zfs_backup_s3.utils;
    requires org.checkerframework.checker.qual;

    exports ru.rerumu.zfs_backup_s3.s3.models to ru.rerumu.zfs_backup_s3.backup;
    exports ru.rerumu.zfs_backup_s3.s3 to ru.rerumu.zfs_backup_s3.backup, ru.rerumu.zfs_backup_s3.local_storage;

}