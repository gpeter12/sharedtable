#!/bin/bash
CFGPTH="config-path "$(pwd)"/conf"
./bin/java -Dprism.lcdtext=false -Dfile.encoding=UTF-8 -m sharedtable/com.sharedtable.controller.MainViewController $(echo $CFGPTH)
