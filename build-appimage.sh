#!/usr/bin/env bash

rm -rf ./AppDir
mkdir AppDir

# Copy Icon
cp ./icon.svg ./AppDir/icon.svg

cp -r ./target/libs ./AppDir/
version=$(find ./target -name "CorrectorParticulas-*.jar" | grep -Eoh "[0-9]+\.[0-9]+\.[0-9]+")
cp "./target/CorrectorParticulas-$version.jar" ./AppDir/

# Write AppRun file
cat <<EOF > ./AppDir/AppRun
#!/usr/bin/env bash

cd \$(dirname \$0)
/usr/bin/java -jar ./CorrectorParticulas-$version.jar
EOF

chmod +x ./AppDir/AppRun

mkdir -p ./AppDir/usr/lib
cp /usr/lib/libopencv_java481.so ./AppDir/usr/lib/

# Write .desktop file
cat <<EOF > ./AppDir/particle-tracker-corrector.desktop
[Desktop Entry]
Name=Particle Tracker Corrector
Exec=AppRun
Type=Application
Categories=Utility
Icon=icon
EOF

ARCH=x86_64 appimagetool AppDir
