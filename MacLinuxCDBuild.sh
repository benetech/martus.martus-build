#!/bin/sh

#################################################
# Martus Mac/Linux CD Build Script
# Requirements:
#
# 	Cygwin bash
#	Command-line CVS (part of Cygwin distro)
# 	InstallShield v6.1 or later
#	Environment var MARTUSISPROJECTDIR set to
#		path of InstallShield project
#
# Notes: The script variables HOMEDRIVE HOMEPATH
#		need to be modified to suit
#		your build environment
#
#		- This script should be run AFTER the
#			original build script has been run
#################################################

# error function
error() { echo "$*" >&2; exit 1; }

# set the necessary environment vars
HOMEDRIVE=c:
HOMEPATH=\MartusBuild
MARTUSINSTALLERPROJECT=$HOMEDRIVE/$HOMEPATH/martus-binary/Installer
MARTUSISPROJECTDIR=$MARTUSINSTALLERPROJECT/Win32
MARTUSBUILDFILES=$MARTUSINSTALLERPROJECT/BuildFiles
CURRENT_VERSION=1_0_2

PATH=/cygdrive/c/j2sdk1.4.1_02/bin:/cygdrive/c/java/ant-1.5/bin:$PATH

# variable for build
MARTUS_ZIP_NAME=MartusClient-$CURRENT_VERSION-src.zip

export HOMEDRIVE HOMEPATH MARTUSINSTALLERPROJECT MARTUSISPROJECTDIR MARTUSBUILDFILES PATH CURRENT_VERSION MARTUS_ZIP_NAME

# move cwd to a neutral position
INITIAL_DIR=`pwd`
cd / || exit

date=`date '+%Y%m%d'`

# Check pre-conditions
echo
echo "Checking pre-conditions...";
if [ ! -d "$MARTUSBUILDFILES" ]; then
	echo "Error: You must run the Martus build script before you can create a CD Image with this script"
	exit
fi

if [ ! -f "/tmp/Releases/martus.jar" ]; then
	echo "Error: You must run the Martus build script before you can create a CD Image with this script"
	exit
fi

if [ ! -f "$HOMEDRIVE/$HOMEPATH/martus/$MARTUS_ZIP_NAME" ]; then
	echo "Error: You must run the Martus build script before you can create a CD Image with this script"
	exit
fi

echo
echo "starting the CD Image build...";
if [ -d "/tmp/LinuxCD" ]; then
	rm -fR /tmp/LinuxCD
fi
mkdir -p /tmp/LinuxCD

#copy verify
mkdir -p /tmp/LinuxCD/Verify
cp $MARTUSBUILDFILES/Verify/* /tmp/LinuxCD/Verify/

#copy libExt
mkdir -p /tmp/LinuxCD/LibExt
cp $MARTUSBUILDFILES/Jars/*.jar /tmp/LinuxCD/LibExt/

#copy Martus dir
mkdir -p /tmp/LinuxCD/Martus
cp /tmp/Releases/martus.jar /tmp/LinuxCD/Martus/
cp $MARTUSBUILDFILES/Documents/license.txt /tmp/LinuxCD/Martus/
cp $MARTUSBUILDFILES/Documents/gpl.txt /tmp/LinuxCD/Martus/
cp $HOMEDRIVE/$HOMEPATH/martus/$MARTUS_ZIP_NAME /tmp/LinuxCD/Martus/

mkdir -p /tmp/LinuxCD/Martus/Docs
cp $MARTUSBUILDFILES/Documents/*.txt /tmp/LinuxCD/Martus/Docs/
cp $MARTUSBUILDFILES/Documents/*en.pdf /tmp/LinuxCD/Martus/Docs/
cp -R $MARTUSBUILDFILES/Documents/Licenses /tmp/LinuxCD/Martus/Docs/

echo 
echo "Creating ISO..."
if [ -f "/tmp/Releases/Martus-Linux-$date.iso" ]; then
	rm -f /tmp/Releases/Martus-Linux-$date.iso
fi

mkisofs -J -r -T -hide-joliet-trans-tbl -l -V Martus-Linux-$date -o /tmp/Releases/Martus-Linux-$date.iso "/tmp/LinuxCD" ||
   error "mkisofs returned $?"
   
echo
echo "generating md5sums of ISO..."
cd /tmp/Releases
md5sum.exe Martus-Linux-$date.iso > Martus-Linux-$date.iso.MD5

echo
echo "Cleaning up..."
rm -fR /tmp/LinuxCD

echo
echo "The build completed succesfully. The Release files are located in /tmp/Releases/ ."
