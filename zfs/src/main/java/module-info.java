module ru.rerumu.zfs {

    requires org.slf4j;
    requires ru.rerumu.utils;
    requires org.checkerframework.checker.qual;

    exports ru.rerumu.zfs.models to ru.rerumu.backup;
    exports ru.rerumu.zfs to ru.rerumu.backup;

}