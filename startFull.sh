#!/bin/sh

export ZFS_BACKUP_S3_REGION=***
export ZFS_BACKUP_S3_ACCESS_KEY_ID=***
export ZFS_BACKUP_S3_SECRET_ACCESS_KEY=***
export ZFS_BACKUP_S3_FULL_PREFIX=***
export ZFS_BACKUP_S3_FULL_STORAGE_CLASS=***
export ZFS_BACKUP_S3_MAX_S3_PART_SIZE=***
export ZFS_BACKUP_S3_MAX_FILE_SIZE=***
export ZFS_BACKUP_S3_TEMP_DIR=***

nohup java -Xms1g -Xmx3g \
-XX:+HeapDumpOnOutOfMemoryError \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9010 \
-Dcom.sun.management.jmxremote.rmi.port=9010 \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dconf.path="example/" \
-Dmode="backupFull" \
-Dlogback.configurationFile=/path/to/config.xml \
-jar ./backups_java-1.0.0.jar  >> output.java 2>&1 &