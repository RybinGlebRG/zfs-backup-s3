module ru.rerumu.zfs_backup_s3.utils {
    requires org.apache.commons.codec;
    requires org.slf4j;
    requires org.checkerframework.checker.qual;

    exports ru.rerumu.zfs_backup_s3.utils.processes.factories to ru.rerumu.zfs_backup_s3.zfs,ru.rerumu.zfs_backup_s3.s3module;
    exports ru.rerumu.zfs_backup_s3.utils.processes.factories.impl to ru.rerumu.zfs_backup_s3.zfs,ru.rerumu.zfs_backup_s3.s3module;
    exports ru.rerumu.zfs_backup_s3.utils.processes to ru.rerumu.zfs_backup_s3.zfs, ru.rerumu.zfs_backup_s3.s3module;
    exports ru.rerumu.zfs_backup_s3.utils to ru.rerumu.zfs_backup_s3.zfs,ru.rerumu.zfs_backup_s3.s3module, ru.rerumu.zfs_backup_s3.backup, ru.rerumu.zfs_backup_s3.local_storage;
    exports ru.rerumu.zfs_backup_s3.utils.processes.impl to ru.rerumu.zfs_backup_s3.zfs;
    exports ru.rerumu.zfs_backup_s3.utils.callables to ru.rerumu.zfs_backup_s3.zfs, ru.rerumu.zfs_backup_s3.s3module;
    exports ru.rerumu.zfs_backup_s3.utils.callables.impl to ru.rerumu.zfs_backup_s3.zfs, ru.rerumu.zfs_backup_s3.s3module;
    exports ru.rerumu.zfs_backup_s3.utils.impl to ru.rerumu.zfs_backup_s3.backup, ru.rerumu.zfs_backup_s3.local_storage, ru.rerumu.zfs_backup_s3.s3module, ru.rerumu.zfs_backup_s3.zfs;
}