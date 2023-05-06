module ru.rerumu.zfs {
    requires org.slf4j;
    requires ru.rerumu.s3module;
    requires ru.rerumu.utils;
    exports ru.rerumu.zfs;
    exports ru.rerumu.zfs.factories;
    exports ru.rerumu.zfs.models;
    exports ru.rerumu.zfs.factories.impl;
    exports ru.rerumu.zfs.impl;
    exports ru.rerumu.zfs.consumers;
}