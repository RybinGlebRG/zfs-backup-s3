module ru.rerumu.zfs_backup_s3.cli {
    requires commons.cli;
    requires org.slf4j;
    requires ru.rerumu.zfs_backup_s3.backup;

    exports ru.rerumu.zfs_backup_s3.cli to ru.rerumu.zfs_backup_s3.main;
}