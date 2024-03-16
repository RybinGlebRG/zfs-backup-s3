module ru.rerumu.zfs_backup_s3.local_storage {
    exports ru.rerumu.zfs_backup_s3.local_storage.factories;
    exports ru.rerumu.zfs_backup_s3.local_storage.services;
    exports ru.rerumu.zfs_backup_s3.local_storage.services.impl;
    exports ru.rerumu.zfs_backup_s3.local_storage.factories.impl;

    requires ru.rerumu.zfs_backup_s3.utils;
    requires org.slf4j;
}