module ru.rerumu.zfs_backup_s3.utils {
    requires org.apache.commons.codec;
    requires org.slf4j;
    requires org.checkerframework.checker.qual;

    exports ru.rerumu.zfs_backup_s3.utils.processes.factories;
    exports ru.rerumu.zfs_backup_s3.utils.processes.factories.impl;
    exports ru.rerumu.zfs_backup_s3.utils.processes;
    exports ru.rerumu.zfs_backup_s3.utils;
    exports ru.rerumu.zfs_backup_s3.utils.processes.impl;
    exports ru.rerumu.zfs_backup_s3.utils.callables;
    exports ru.rerumu.zfs_backup_s3.utils.callables.impl;
}