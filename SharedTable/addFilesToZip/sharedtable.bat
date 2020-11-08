@echo off
set DIR="%~dp0"
set JAVA_EXEC="%DIR:"=%\java"
pushd %DIR% & %JAVA_EXEC% -Dfile.encoding=UTF8 -p "%~dp0/../app" -m sharedtable/com.sharedtable.controller.MainViewController config-path conf %* & popd
