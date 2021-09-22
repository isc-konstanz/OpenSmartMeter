#!/bin/sh
#Scriptname: build.sh
#Description: Build script to generate the OpenSmartMeter debian package

if [ $(id -u) != 0 ]; then
    echo "DPKG build process should be performed with root privileges." 1>&2
    exit 1
fi
root_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
build_dir="$root_dir/build/dpkg"

rm -rf $build_dir/*

cd $root_dir

if hash gradle 2>/dev/null; then
    gradle="gradle"
else
    gradle=$root_dir"/gradlew"
fi
eval $gradle packages

for package in $build_dir/*/ ; do
    cd $package
    chmod 755 debian/pre* 2>/dev/null
    chmod 755 debian/post* 2>/dev/null
    chmod 755 debian/rules

    dpkg-buildpackage -us -uc
done
