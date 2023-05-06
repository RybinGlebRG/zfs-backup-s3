module ru.rerumu.zfs {

    requires org.slf4j;
    requires ru.rerumu.utils;

    exports ru.rerumu.zfs.models to ru.rerumu.backup;
    exports ru.rerumu.zfs to ru.rerumu.backup;

}