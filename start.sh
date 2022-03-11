#!/bin/sh

nohup java -Dconf.path="example/" -Dmode="sendFull" \
-Dlogback.configurationFile=/path/to/config.xml \
-jar ./backups_java-1.0.0.jar  >> output.java 2>&1 &