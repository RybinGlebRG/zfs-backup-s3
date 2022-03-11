#!/bin/sh

java -Dconf.path="example/" -Dmode="sendFull" -jar ./backups_java-1.0.0.jar  >> output.java 2>&1