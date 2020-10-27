# sharedtable

This is a Microsoft Paint like app, where multiple user can draw on the same canvas at the same time.

**!!IMPORTANT:** Before all, you need to set the JAVA_HOME environment variable to the installation path of OpenJDK 14.0.1

* for Windows: https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html
* for Linux: https://vitux.com/how-to-setup-java_home-path-in-ubuntu/

## Build
1. Navigate to the SharedTable directory
1. Open a terminal
1. ./gradlew build

## Run
1. Navigate to the SharedTable directory
1. Open a terminal
1. ./gradlew run

## make portable jlink image
1. Navigate to the SharedTable directory
1. Open a terminal
1. ./gradlew jlink

The image will be in build/image directory

## make debian package
1. Navigate to the SharedTable directory
1. Open a terminal
1. ./gradlew jlink
1. ./gradlew make_deb

The sharedtable.deb file will be in deb_release directory

## make windows installer
1. install NSIS (https://nsis.sourceforge.io/Download) precisely to 'C:\Program Files (x86)\NSIS
1. Navigate to the SharedTable directory
1. Open a terminal
1. gradlew jlink
1. graldew make_installer

The installer will be in nsisOutput directory
