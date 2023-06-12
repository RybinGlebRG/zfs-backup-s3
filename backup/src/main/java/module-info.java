module ru.rerumu.zfs_backup_s3.backup {
    exports ru.rerumu.zfs_backup_s3.backups.services to ru.rerumu.zfs_backup_s3.cli;
    exports ru.rerumu.zfs_backup_s3.backups to ru.rerumu.zfs_backup_s3.cli;

    requires ru.rerumu.zfs_backup_s3.zfs;
    requires org.slf4j;
    requires ru.rerumu.zfs_backup_s3.s3module;
    requires software.amazon.awssdk.regions;
    requires ru.rerumu.zfs_backup_s3.utils;
    requires commons.cli;
}