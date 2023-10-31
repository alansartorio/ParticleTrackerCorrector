#!/usr/bin/env bash

package=particle-tracker-corrector
mkdir "$package"

mkdir -p "$package"/usr/{share/{applications,icons/hicolor/scalable/apps,java},lib/jni}
mkdir "$package"/DEBIAN

cat <<"EOF" > "$package"/DEBIAN/control
Package: particle-tracker-corrector
Description: Program for manually tracking particles in videos
Version: 4.1.0
Maintainer: Alan Sartorio
Architecture: all
Depends: libopencv4.5-java, libcommons-csv-java
EOF

mvn package
cp target/particle-tracker-corrector-*.jar "$package"/usr/share/java/particle-tracker-corrector.jar

cat <<"EOF" > "$package"/usr/share/applications/particle-tracker-corrector.desktop
[Desktop Entry]
Name=Particle Tracker Corrector
Exec=java -jar /usr/share/java/particle-tracker-corrector.jar
Type=Application
Categories=Utility
Icon=particle-tracker-corrector
EOF

cp icon.svg "$package"/usr/share/icons/hicolor/scalable/apps/particle-tracker-corrector.svg

ln -s /usr/lib/jni/libopencv_java454d.so "$package"/usr/lib/jni/libopencv_java454.so
