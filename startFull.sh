#!/bin/sh

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