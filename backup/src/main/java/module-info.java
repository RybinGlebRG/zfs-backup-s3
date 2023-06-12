module ru.rerumu.backup {
    exports ru.rerumu.backups.services;
    exports ru.rerumu.backups;

    requires ru.rerumu.zfs;
    requires org.slf4j;
    requires ru.rerumu.s3module;
    requires software.amazon.awssdk.regions;
    requires ru.rerumu.utils;
    requires commons.cli;
}