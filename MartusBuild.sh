#!/bin/sh

# TODO: test run with command below
#set -u

#################################################
# Martus CD Build Script
#
# Notes: The script variables:
#				 CVSROOT HOMEDRIVE HOMEPATH
#				 need to be modified to suit
#				 your build environment
#################################################

#################################################
#
#	TODO:
#
#################################################

# error function
error() { echo "$*" >&2; exit 1; }

# set the necessary environment vars
CVSROOT=:pserver:ivo@cvs.bookshare.org:/var/local/cvs
HOMEDRIVE=c:
HOMEPATH=\CVS_HOME
MARTUSINSTALLERPROJECT=$HOMEDRIVE/$HOMEPATH/binary-martus/Installer
MARTUSNSISPROJECTDIR=$MARTUSINSTALLERPROJECT/Win32_NSIS
MARTUSBUILDFILES=$MARTUSINSTALLERPROJECT/BuildFiles

MARTUSSOURCES=$HOMEDRIVE/$HOMEPATH/MartusBuild

PATH=/cygdrive/c/j2sdk1.4.2_03/bin:/cygdrive/c/java/ant-1.5/bin:$PATH

export HOMEDRIVE HOMEPATH MARTUSSOURCES MARTUSINSTALLERPROJECT MARTUSNSISPROJECTDIR MARTUSBUILDFILES PATH CVSROOT

# move cwd to a neutral position
INITIAL_DIR=`pwd`
cd / || exit

# check if this is a CVS tagged release
echo
echo "Should the CVS tree be labeled after a successful build?[y/N]"
read cvs_tag
if [ $cvs_tag = 'y' ]; then
	cvs_tag=Y
fi

cvs_date=`date '+%Y-%m-%d'`

echo
echo "Should client Releases be built?[y/N]"
read build_cd
if [ $build_cd = 'y' ]; then
	build_cd=Y
fi

if [ $build_cd = 'Y' ]; then
	# check if we should burn a CD
	echo
	echo "Should the resultant ISO be burned onto a CD?[y/N]"
	read burn_cd
	if [ $burn_cd = 'y' ]; then
		burn_cd=Y
	fi
fi

if [ $build_cd = 'Y' ]; then
	echo
	echo "Should the client release include sources?[y/N]"
	read include_sources
	if [ $include_sources = 'y' ]; then
		include_sources=Y
	fi
fi

# Clean the build environment
echo
echo "Cleaning the build environment (ignore mount/umount messages)...";
if [ -d "$HOMEDRIVE/$HOMEPATH" ]; then
	rm -Rf $HOMEDRIVE/$HOMEPATH
fi
mkdir $HOMEDRIVE/$HOMEPATH
mkdir $HOMEDRIVE/$HOMEPATH/MartusBuild

if [ ! -d "/MartusBuild" ]; then
	mount $MARTUSSOURCES /MartusBuild 2>&1
fi

# Connect to cvs & download latest source
echo
echo "Downloading source from CVS...";
cd $HOMEDRIVE/$HOMEPATH || exit

cvs.exe -q checkout -l -P martus || error "cvs returned $?"
echo "copying martus...";
echo
cp -r martus/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-client/source || error "cvs returned $?"
echo "copying martus-client...";
echo 
cp -r martus-client/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-common/source || error "cvs returned $?"
echo "copying martus-common...";
echo
cp -r martus-common/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-hrdag/source || error "cvs returned $?"
echo "copying martus-hrdag...or hrvd...";
echo
cp -r martus-hrdag/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-meta/source || error "cvs returned $?"
echo "copying martus-meta...";
echo
cp -r martus-meta/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild/ || error "copy returned $?"
echo

cvs.exe -q checkout martus-server/source || error "cvs returned $?"
echo "copying martus-server...";
echo
cp -r martus-server/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-amplifier/source || error "cvs returned $?"
echo "copying martus-amplifier...";
echo
cp -r martus-amplifier/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-amplifier/presentation || error "cvs returned $?"
mkdir -p MartusBuild/www/MartusAmplifier/presentation
cp -r martus-amplifier/presentation/* MartusBuild/www/MartusAmplifier/presentation/ || error "copy returned $?"
echo

cvs.exe -q checkout martus-amplifier/presentationNonSSL || error "cvs returned $?"
mkdir -p MartusBuild/www/MartusAmplifier/presentationNonSSL
cp -r martus-amplifier/presentationNonSSL/* MartusBuild/www/MartusAmplifier/presentationNonSSL/ || error "copy returned $?"
echo

cvs.exe -q checkout martus-mspa/source || error "cvs returned $?"
echo "copying martus-mspa...";
echo
cp -r martus-mspa/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-swing/source || error "cvs returned $?"
echo "copying martus-swing...";
echo
cp -r martus-swing/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-utils/source || error "cvs returned $?"
echo "copying martus-utils...";
echo
cp -r martus-utils/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-logi/source || error "cvs returned $?"
echo "copying martus-logi...";
echo
cp -r martus-logi/source/* $HOMEDRIVE/$HOMEPATH/MartusBuild || error "copy returned $?"
echo

cvs.exe -q checkout martus-thirdparty || error "cvs returned $?"
mkdir MartusBuild/ThirdPartyJars
echo "copying martus-thirdparty...";
echo
cp martus-thirdparty/common/ant/bin/*.jar MartusBuild/ThirdPartyJars/ || error "copy returned $?"
cp martus-thirdparty/common/InfiniteMonkey/bin/*.jar MartusBuild/ThirdPartyJars/ || error "copy returned $?"
cp martus-thirdparty/common/XMLRPC/bin/*.jar MartusBuild/ThirdPartyJars/ || error "copy returned $?"

cp martus-thirdparty/server/Jetty/bin/*.jar MartusBuild/ThirdPartyJars/ || error "copy returned $?"
cp martus-thirdparty/server/Lucene/bin/*.jar MartusBuild/ThirdPartyJars/ || error "copy returned $?"
cp martus-thirdparty/server/Velocity/bin/*.jar MartusBuild/ThirdPartyJars/ || error "copy returned $?"

cvs.exe -q checkout martus-jar-verifier || error "cvs returned $?"
mkdir MartusBuild/verify
cp -r martus-jar-verifier/* MartusBuild/verify/
rm -f MartusBuild/verify/*.bat
rm -f MartusBuild/verify/*.txt
echo
echo "Building Server JarVerifier...";
cd MartusBuild/verify || exit
if [ -f "JarVerifier.class" ]; then
	rm -f JarVerifier.class
fi
javac JarVerifier.java
status=$?
if [ -f "JarVerifier.class" ]; then
	rm -f JarVerifier.java
fi
cd $HOMEDRIVE/$HOMEPATH || exit

# remove CVS directories from build area
cd $MARTUSSOURCES
find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null

cd $HOMEDRIVE/$HOMEPATH || exit
if [ $build_cd = 'Y' ]; then
	echo
	echo "Downloading installer from CVS...";
	RESULT=`cvs.exe checkout binary-martus/Installer/ 2>&1 || error "cvs returned $?"`
	RESULT=`cvs.exe checkout -l binary-martus/Releases || error "cvs returned $?"`

	### additional client tasks - start ###

	# copy third-party jars to the CD build location (xml-rpc, bc-castle, infinitemonkey, junit)
	echo "Copying thirdparty jars to build location..."
	echo
	mkdir -p $MARTUSBUILDFILES/Jars
	cd $HOMEDRIVE/$HOMEPATH
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/InfiniteMonkey/bin/InfiniteMonkey.jar $MARTUSBUILDFILES/Jars/
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/XMLRPC/bin/xmlrpc-*.jar $MARTUSBUILDFILES/Jars/
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/libext/BouncyCastle/bin/*.jar $MARTUSBUILDFILES/Jars/
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/libext/JUnit/bin/*.jar $MARTUSBUILDFILES/Jars/
	
	# copy sources to the build files source directory
	rm -fr $MARTUSBUILDFILES/SourceFiles
	mkdir -p $MARTUSBUILDFILES/SourceFiles
	
	mkdir -p $MARTUSBUILDFILES/SourceFiles/BouncyCastle
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/libext/BouncyCastle/source/* $MARTUSBUILDFILES/SourceFiles/BouncyCastle/
	
	mkdir -p $MARTUSBUILDFILES/SourceFiles/InfiniteMonkey
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/InfiniteMonkey/source/* $MARTUSBUILDFILES/SourceFiles/InfiniteMonkey/

	mkdir -p $MARTUSBUILDFILES/SourceFiles/Installer/NSIS
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/client/installer/Win32/NSIS/source/* $MARTUSBUILDFILES/SourceFiles/Installer/NSIS/
	
	mkdir -p $MARTUSBUILDFILES/SourceFiles/junit
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/libext/JUnit/source/* $MARTUSBUILDFILES/SourceFiles/junit/
	
	mkdir -p $MARTUSBUILDFILES/SourceFiles/Logi
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/Logi/source/* $MARTUSBUILDFILES/SourceFiles/Logi/
	
	mkdir -p $MARTUSBUILDFILES/SourceFiles/Sun
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/client/Sun/source/* $MARTUSBUILDFILES/SourceFiles/Sun/
	
	mkdir -p $MARTUSBUILDFILES/SourceFiles/xmlrpc
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/XMLRPC/source/* $MARTUSBUILDFILES/SourceFiles/xmlrpc/
	
	cd $MARTUSBUILDFILES/SourceFiles
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	
	# copy the licenses into the doc folder
	rm -fr $MARTUSBUILDFILES/Documents/Licenses
	
	mkdir -p $MARTUSBUILDFILES/Documents/Licenses/BouncyCastle
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/libext/BouncyCastle/license/* $MARTUSBUILDFILES/Documents/Licenses/BouncyCastle/
	
	mkdir -p $MARTUSBUILDFILES/Documents/Licenses/InfiniteMonkey
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/InfiniteMonkey/license/* $MARTUSBUILDFILES/Documents/Licenses/InfiniteMonkey/
	
	mkdir -p $MARTUSBUILDFILES/Documents/Licenses/JUnit
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/libext/JUnit/license/* $MARTUSBUILDFILES/Documents/Licenses/JUnit/
	
	mkdir -p $MARTUSBUILDFILES/Documents/Licenses/Logi
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/Logi/license/* $MARTUSBUILDFILES/Documents/Licenses/Logi/
	
	mkdir -p $MARTUSBUILDFILES/Documents/Licenses/Sun
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/client/Sun/license/* $MARTUSBUILDFILES/Documents/Licenses/Sun/
	
	mkdir -p $MARTUSBUILDFILES/Documents/Licenses/Xml-Rpc
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/XMLRPC/license/* $MARTUSBUILDFILES/Documents/Licenses/Xml-Rpc/
	
	cd $MARTUSBUILDFILES/Documents/Licenses
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	
	# copy program files
	cp $HOMEDRIVE/$HOMEPATH/martus-thirdparty/common/InfiniteMonkey/bin/InfiniteMonkey.dll $MARTUSBUILDFILES/ProgramFiles/
	

	### additional client tasks - end ###
fi

###########################################################################
# get the build details
CURRENT_VERSION=2.0
BUILD_DATE=`date '+%Y%m%d'`

# the build number below relies on the Ant task that creates and
#autoincrements this file
BUILD_NUMBER_FILE="$MARTUSSOURCES/build.number"
if [ -f "$BUILD_NUMBER_FILE" ]; then
	BUILD_NUMBER=`cat $BUILD_NUMBER_FILE | grep build.number | cut -d'=' -f2`
else
	BUILD_NUMBER=1
fi

MARTUS_ZIP_NAME=MartusClient-$CURRENT_VERSION-src.zip

# note the variables
echo "Build is v $CURRENT_VERSION, b $BUILD_NUMBER, date $BUILD_DATE"
echo

export CURRENT_VERSION BUILD_NUMBER BUILD_DATE MARTUS_ZIP_NAME BUILD_NUMBER_FILE

###########################################################################

echo
echo "Starting the ant build (might take a minute)..."
cd /MartusBuild
if [ $cvs_tag = 'Y' ]; then
	ant.bat md5
	#ant.bat md5-no-tests
else
	ant.bat build
fi
status=$?

# check the build.number file back into CVS
cd $HOMEDRIVE/$HOMEPATH
echo "Updating to CVS: $BUILD_NUMBER_FILE"
cp -f $BUILD_NUMBER_FILE $HOMEDRIVE/$HOMEPATH/martus/
cvs.exe commit -m "v $cvs_date" $HOMEDRIVE/$HOMEPATH/martus/build.number || exit

if [ $status != 0 ]; then
	echo
	echo "Build Failed!"
	cd $INITIAL_DIR
fi

echo
echo "Ant completed with status: $status"

if [ ! -f "$MARTUSSOURCES/dist/martus.jar" ]; then
	echo "BUILD FAILED!! Exit status $status"
	echo "Please note any messages above"
	echo "Cleaning up..."
	cd /
	#rm -Rf $HOMEDRIVE/$HOMEPATH
	cd $INITIAL_DIR
	echo "Exiting..."
	exit 1
fi

if [ $cvs_tag = 'Y' ]; then
if [ ! -f "$MARTUSSOURCES/dist/martus.jar.md5" ]; then
	echo "BUILD FAILED!! Missing md5. Exit status $status"
	echo "Please note any messages above"
	echo "Cleaning up..."
	cd /
	#rm -Rf $HOMEDRIVE/$HOMEPATH
	cd $INITIAL_DIR
	echo "Exiting..."
	exit 1
fi
fi

cd $INITIAL_DIR

echo
echo "Moving martus.jar to temp CD build location..."
if [ -d "/tmp/Releases" ]; then
	rm -fR /tmp/Releases
fi
mkdir -p /tmp/Releases

cp $MARTUSSOURCES/dist/martus-server.jar /tmp/Releases/martus-server-$BUILD_DATE.$BUILD_NUMBER.jar || exit
if [ -f "$MARTUSSOURCES/dist/martus-server.jar.MD5" ]; then
	echo -e "\n" >> "$MARTUSSOURCES/dist/martus-server.jar.MD5"
	cp $MARTUSSOURCES/dist/martus-server.jar.MD5 /tmp/Releases/martus-server-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
fi

cp $MARTUSSOURCES/dist/martus-mspa-client.jar /tmp/Releases/martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar || exit
if [ -f "$MARTUSSOURCES/dist/martus-mspa-client.jar.MD5" ]; then
	echo -e "\n" >> "$MARTUSSOURCES/dist/martus-mspa-client.jar.MD5"
	cp $MARTUSSOURCES/dist/martus-mspa-client.jar.MD5 /tmp/Releases/martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
fi

cp $MARTUSSOURCES/dist/martus-meta.jar /tmp/Releases/martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar || exit
if [ -f "$MARTUSSOURCES/dist/martus-meta.jar.MD5" ]; then
	echo -e "\n" >> "$MARTUSSOURCES/dist/martus-meta.jar.MD5"
	cp $MARTUSSOURCES/dist/martus-meta.jar.MD5 /tmp/Releases/martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
fi

cp $MARTUSSOURCES/dist/martus.jar /tmp/Releases/martus.jar || exit
cp $MARTUSSOURCES/dist/martus.jar /tmp/Releases/martus-$BUILD_DATE.$BUILD_NUMBER.jar || exit
if [ -f "$MARTUSSOURCES/dist/martus.jar.MD5" ]; then
	echo -e "\n" >> "$MARTUSSOURCES/dist/martus.jar.MD5"
	cp $MARTUSSOURCES/dist/martus.jar.MD5 /tmp/Releases/martus-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
fi

# labelling the sources CVS tree
if [ $cvs_tag = 'Y' ]; then
	echo
	echo "Labeling CVS with tag: v${cvs_date}_build-$BUILD_NUMBER"
	cd $HOMEDRIVE/$HOMEPATH
	RESULT=`cvs.exe tag v${cvs_date}_build-$BUILD_NUMBER martus martus-client martus-amplifier martus-common martus-hrdag martus-meta martus-server martus-swing martus-utils martus-mspa martus-logi martus-thirdparty`
	status=$?
	if [ $status != 0 ]; then
		echo "Unable to add tag to CVS. You must do it manually"
	fi;

	# create Client CVS dirs
	if [ ! -d "$HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar/CVS" ]; then
		mkdir -p $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar/CVS || exit
		cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar/CVS
		touch Entries
		echo "binary-martus/Releases/ClientJar" > Repository
		echo $CVSROOT > Root
	fi

	# create Server CVS dirs
	if [ ! -d "$HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar/CVS" ]; then
		mkdir -p $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar/CVS || exit
		cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar/CVS
		touch Entries
		echo "binary-martus/Releases/ServerJar" > Repository
		echo $CVSROOT > Root
	fi

	# add client to CVS
	mkdir -p $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar
	cp /tmp/Releases/martus-$BUILD_DATE.$BUILD_NUMBER.jar $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar || exit
	cp /tmp/Releases/martus-$BUILD_DATE.$BUILD_NUMBER.jar.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar || exit
	cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientJar/
	echo "Adding to CVS: martus-$BUILD_DATE.$BUILD_NUMBER.jar"
	cvs.exe add martus-$BUILD_DATE.$BUILD_NUMBER.jar  || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe add martus-$BUILD_DATE.$BUILD_NUMBER.jar.md5  || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit

	# add server to CVS
	mkdir -p $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar
	cp /tmp/Releases/martus-server-$BUILD_DATE.$BUILD_NUMBER.jar $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar || exit
	cp /tmp/Releases/martus-server-$BUILD_DATE.$BUILD_NUMBER.jar.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar || exit
	
	cp /tmp/Releases/martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar || exit
	cp /tmp/Releases/martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar || exit
	
	cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar/
	echo "Adding to CVS: martus-server-$BUILD_DATE.$BUILD_NUMBER.jar"
	cvs.exe add martus-server-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-server-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe add martus-server-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-server-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
	
	cvs.exe add martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe add martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-mspa-client-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
	
	# add meta to CVS
	cp /tmp/Releases/martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar || exit
	cp /tmp/Releases/martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar || exit
	cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ServerJar/
	echo "Adding to CVS: martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar"
	cvs.exe add martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar || exit
	cvs.exe add martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" martus-meta-$BUILD_DATE.$BUILD_NUMBER.jar.md5 || exit
	
	# verify the added tag
	#HOMEDRIVE=c:
	#HOMEPATH=\CVS_HOME2
	
	#export HOMEDRIVE HOMEPATH
	
	#mkdir $HOMEDRIVE/$HOMEPATH
	#cd $HOMEDRIVE/$HOMEPATH
	
	#cvs.exe -q checkout -r v${cvs_date}_build-$BUILD_NUMBER -l -P martus || error "unable to retrieve tag v${cvs_date}_build-$BUILD_NUMBER. cvs returned $?"
	
	#HOMEDRIVE=c:
	#HOMEPATH=\CVS_HOME

	#export HOMEDRIVE HOMEPATH	
fi

cd $INITIAL_DIR

#start installer build
if [ $build_cd = 'Y' ]; then
	echo
else
	echo
	echo "The build completed succesfully. The built jars are in /tmp/Releases."
	exit 0
fi

if [ $include_sources = 'Y' ]; then
	echo ""
else
	rm -fR $MARTUSSOURCES || exit
fi

# clean up the sources dir
if [ -d "$HOMEDRIVE/$HOMEPATH/MartusBuild/ThirdPartyJars" ]; then
	rm -Rf $HOMEDRIVE/$HOMEPATH/MartusBuild/ThirdPartyJars
fi

if [ -d "$HOMEDRIVE/$HOMEPATH/MartusBuild/verify" ]; then
	rm -Rf $HOMEDRIVE/$HOMEPATH/MartusBuild/verify
fi

if [ -d "$HOMEDRIVE/$HOMEPATH/MartusBuild/www" ]; then
	rm -Rf $HOMEDRIVE/$HOMEPATH/MartusBuild/www
fi

echo
echo "Starting the installer build..."

RELEASE_FILE=/tmp/Releases/martus.jar
stop_build=0
if [ ! -f "$RELEASE_FILE" ]; then
	stop_build=1
	echo
	echo "No Martus.jar was found to use in the build...."
	echo "This script will prep the NSIS project."
	echo "You will then need to:"
	echo "1) Manually add the jar to be used in the release to $MARTUSBUILDFILES/ProgramFiles/"
	echo "2) Create the CD image with:"
	echo "\t cd $MARTUSNSISPROJECTDIR"
	echo "\t makensis.exe NSIS_Martus.nsi"
	echo "\t cp MartusSetup.exe $CD_IMAGE_DIR/"
	echo "3) Create the Martus release ISO with:"
	echo "mkisofs -J -r -T -hide-joliet-trans-tbl -l -V Martus -o $MARTUSBUILDFILES/MartusRel.iso $CD_IMAGE_DIR"
else
	echo
	echo "Martus.jar to use in the build was found in the temporary location...."
	mv $RELEASE_FILE $MARTUSBUILDFILES/ProgramFiles/Martus.jar || exit
fi

cd $INITIAL_DIR
echo
echo "removing extra dirs & files (ignore any File not Found messages)...";
rm -fR $MARTUSSOURCES/dist
rm -fR $MARTUSSOURCES/TechDocs
rm -fR $MARTUSSOURCES/org/martus/meta
rm -fR $MARTUSSOURCES/org/martus/server
rm -fR $MARTUSSOURCES/org/martus/mspa
rm -fR $MARTUSSOURCES/org/martus/amplifier
rm -f $MARTUSSOURCES/.classpath
rm -f $MARTUSSOURCES/.project
rm -f $MARTUSSOURCES/build.number
rm -f $MARTUSSOURCES/build.properties

# moving installer scripts
if [ ! -d "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts" ]; then
	mkdir "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts"
fi

cp $MARTUSNSISPROJECTDIR/*.nsi "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/"
mkdir "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/locallang"
cp $MARTUSNSISPROJECTDIR/locallang/English.* "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/locallang/"
cp $MARTUSNSISPROJECTDIR/locallang/Spanish.* "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/locallang/"
cp $MARTUSNSISPROJECTDIR/locallang/Russian.* "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/locallang/"

find $HOMEDRIVE/$HOMEPATH -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
find $MARTUSSOURCES -type "f" -name "*.class" -exec rm -fR '{}' \; > /dev/null

# create the license file for the installer
if [ -f "$MARTUSBUILDFILES/combined-license.txt" ]; then
	rm -f $MARTUSBUILDFILES/combined-license.txt
fi
cat $MARTUSBUILDFILES/Documents/license.txt > $MARTUSBUILDFILES/combined-license.txt
echo -e "\n\n\t**********************************\n\n" >> $MARTUSBUILDFILES/combined-license.txt
cat $MARTUSBUILDFILES/Documents/gpl.txt >> $MARTUSBUILDFILES/combined-license.txt

# fix newlines for all text files
unix2dos.exe --unix2dos $MARTUSBUILDFILES/combined-license.txt
find $MARTUSBUILDFILES/Documents -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
find $MARTUSBUILDFILES/verify -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
find $MARTUSBUILDFILES/Winsock95 -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
find $MARTUSBUILDFILES/SourceFiles -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null

cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSSOURCES/
cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSSOURCES/

cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSBUILDFILES/ProgramFiles/
cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSBUILDFILES/ProgramFiles/

cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSBUILDFILES/Verify/
cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSBUILDFILES/Verify/

cp $MARTUSBUILDFILES/Documents/license.txt "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/"
cp $MARTUSBUILDFILES/Documents/gpl.txt "$MARTUSBUILDFILES/SourceFiles/Installer/NSIS Scripts/"

cd $INITIAL_DIR

#zip-up sources
if [ $include_sources = 'Y' ]; then
	echo
	echo "zipping up sources..."
	cd $MARTUSSOURCES
	find . -name "*.java" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "build.xml" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "*.gif" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "*.jpg" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "*.png" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "license.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "gpl.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "main-class.txt" -print | zip $MARTUS_ZIP_NAME -q@

	#english
	find . -name "MartusHelpTOC-en.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "MartusHelp-en.txt" -print | zip $MARTUS_ZIP_NAME -q@
	
	#spanish
	find . -name "MartusHelpTOC-es.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "MartusHelp-es.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "Martus-es.mtf" -print | zip $MARTUS_ZIP_NAME -q@
	
	#russian
	find . -name "MartusHelpTOC-ru.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "MartusHelp-ru.txt" -print | zip $MARTUS_ZIP_NAME -q@
	find . -name "Martus-ru.mtf" -print | zip $MARTUS_ZIP_NAME -q@
	
	echo
	echo "zipping third party items..."
	cd $MARTUSBUILDFILES/SourceFiles

	find ./BouncyCastle -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@
	find ./Sun -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@
	find ./InfiniteMonkey -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@
	find ./junit -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@
	find ./xmlrpc -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@
	find ./logi -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@
	find ./Installer -type "f" -name "*" -print | zip $MARTUSSOURCES/$MARTUS_ZIP_NAME -q@

fi

# Build JarVerifier
echo
echo "Building JarVerifier...";
cd $MARTUSBUILDFILES/Verify || exit
if [ -f "JarVerifier.class" ]; then
	rm -f JarVerifier.class
fi
javac JarVerifier.java
status=$?

if [ ! -f "JarVerifier.class" ]; then
	echo "BUILD FAILED!!, exit status $status!!"
	exit $status
fi

if [ $stop_build -eq 1 ];then
	echo "Exiting client build"
	exit 1
fi

# ###################################################
# Create CD Image to use
CD_IMAGE_DIR=/tmp/Releases/CD_IMAGE

mkdir -p $CD_IMAGE_DIR
cp $MARTUSBUILDFILES/Documents/README*.txt $CD_IMAGE_DIR
cp $MARTUSBUILDFILES/Documents/license.txt $CD_IMAGE_DIR

mkdir -p $CD_IMAGE_DIR/Win95
cp $MARTUSBUILDFILES/Winsock95/* $CD_IMAGE_DIR/Win95/

mkdir -p $CD_IMAGE_DIR/verify
cp $MARTUSBUILDFILES/Verify/* $CD_IMAGE_DIR/verify/
rm $CD_IMAGE_DIR/verify/*_th.txt

cp $MARTUSBUILDFILES/ProgramFiles/autorun.inf $CD_IMAGE_DIR

mkdir -p $CD_IMAGE_DIR/Martus
cp $MARTUSBUILDFILES/ProgramFiles/* $CD_IMAGE_DIR/Martus/
rm -f $CD_IMAGE_DIR/Martus/autorun.inf
cp $MARTUSBUILDFILES/Documents/license.txt $CD_IMAGE_DIR/Martus/
cp $MARTUSBUILDFILES/Documents/gpl.txt $CD_IMAGE_DIR/Martus/

mkdir -p $CD_IMAGE_DIR/Martus/Docs
cp $MARTUSBUILDFILES/Documents/martus_user_guide.pdf $CD_IMAGE_DIR/Martus/Docs
cp $MARTUSBUILDFILES/Documents/quickstartguide.pdf $CD_IMAGE_DIR/Martus/Docs
cp $MARTUSBUILDFILES/Documents/*_fr.pdf $CD_IMAGE_DIR/Martus/Docs
cp $MARTUSBUILDFILES/Documents/*_es.pdf $CD_IMAGE_DIR/Martus/Docs
cp $MARTUSBUILDFILES/Documents/*_ru.pdf $CD_IMAGE_DIR/Martus/Docs
cp $MARTUSBUILDFILES/Documents/LinuxJavaInstall.txt $CD_IMAGE_DIR/Martus/Docs/

cp -r $MARTUSBUILDFILES/Documents/Licenses $CD_IMAGE_DIR/Martus/Docs/

mkdir -p $CD_IMAGE_DIR/LibExt
cp $MARTUSBUILDFILES/Jars/* $CD_IMAGE_DIR/LibExt/

if [ $include_sources = 'Y' ]; then
	mkdir -p $CD_IMAGE_DIR/Sources/
	cp $MARTUSSOURCES/$MARTUS_ZIP_NAME $CD_IMAGE_DIR/Sources/
fi

mkdir -p $CD_IMAGE_DIR/Java/Linux/i586
cp "$MARTUSBUILDFILES/Java redist/Linux/i586/*.bin" $CD_IMAGE_DIR/Java/Linux/i586/

cd $CD_IMAGE_DIR
find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
cd $INITIAL_DIR

cd $MARTUSNSISPROJECTDIR
if [ -f "$MARTUSNSISPROJECTDIR/MartusSetup.exe" ]; then
	rm -f $MARTUSNSISPROJECTDIR/MartusSetup.exe
fi
echo "starting the CD NSIS installer build...";

makensis.exe /V2 NSIS_Martus.nsi
if [ -f "$MARTUSNSISPROJECTDIR/MartusSetup.exe" ]; then
	echo
	echo "Build succeded..."
else
	echo
	echo "Build failed..."
	exit 1
fi
cp $MARTUSNSISPROJECTDIR/MartusSetup.exe $CD_IMAGE_DIR/
cd $INITIAL_DIR
####################################################################################

# ##################################################################################
# Create Single Installer Image to use

if [ $include_sources = 'Y' ]; then
	cp $MARTUSSOURCES/$MARTUS_ZIP_NAME $MARTUSBUILDFILES/
fi

cd $MARTUSNSISPROJECTDIR
if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupSingle.exe" ]; then
	rm -f $MARTUSNSISPROJECTDIR/MartusSetupSingle.exe
fi
echo "starting the Single NSIS installer build...";

makensis.exe /V2 NSIS_Martus_Single.nsi
if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupSingle.exe" ]; then
	echo
	echo "Build succeded..."
	cp "$MARTUSNSISPROJECTDIR/MartusSetupSingle.exe" /tmp/Releases/MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe
else
	echo
	echo "Build failed..."
	exit 1
fi

cd $INITIAL_DIR

# ##################################################################################
# Create Upgrade Installer Image to use

cd $MARTUSNSISPROJECTDIR
if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe" ]; then
	rm -f $MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe
fi
echo "starting the Upgrade NSIS installer build...";

makensis.exe /V2 NSIS_Martus_Upgrade.nsi
if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe" ]; then
	echo
	echo "Build succeded..."
	cp "$MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe" /tmp/Releases/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe
else
	echo
	echo "Build failed..."
	exit 1
fi

cd $INITIAL_DIR

####################################################################################

echo
echo "Creating ISO..."
if [ -f "$MARTUSBUILDFILES/Martus-$BUILD_DATE.$BUILD_NUMBER.iso" ]; then
	rm -f $MARTUSBUILDFILES/Martus-$BUILD_DATE.$BUILD_NUMBER.iso
fi

mkisofs -J -r -T -hide-joliet-trans-tbl -l -V Martus-$BUILD_DATE.$BUILD_NUMBER -o $MARTUSBUILDFILES/Martus-$BUILD_DATE.$BUILD_NUMBER.iso $CD_IMAGE_DIR || error "mkisofs returned $?"

if [ ! -d "/tmp/Releases" ]; then
	mkdir -p /tmp/Releases
fi

if [ -f "/tmp/Releases/Martus-$BUILD_DATE.$BUILD_NUMBER.iso" ]; then
	rm -f /tmp/Releases/Martus-$BUILD_DATE.$BUILD_NUMBER.iso
fi
mv $MARTUSBUILDFILES/Martus-$BUILD_DATE.$BUILD_NUMBER.iso /tmp/Releases/

echo
echo "generating md5sums of ISO..."
cd /tmp/Releases
md5sum.exe Martus-$BUILD_DATE.$BUILD_NUMBER.iso > Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5
echo -e "\n" >> Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5

md5sum.exe MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe > MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5
echo -e "\n" >> MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5

md5sum.exe MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe > MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5
echo -e "\n" >> MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5

if [ $cvs_tag = 'Y' ]; then
	cd $HOMEDRIVE/$HOMEPATH || exit

	if [ ! -d "$HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientISO/CVS" ]; then
		mkdir -p $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientISO/CVS || exit
		cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientISO/CVS
		touch Entries
		echo "binary-martus/Releases/ClientISO" > Repository
		echo $CVSROOT > Root
	fi

	# add ISO md5 to CVS
	cp /tmp/Releases/Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientISO || exit
	cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientISO/
	echo "Adding to CVS: Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5"
	cvs.exe add Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5  || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5 || exit

	# move ISO onto Network
	if [ -d "/cygdrive/h/Martus/ClientISO" ]; then
		echo "Moving ISO onto network drive"
		cp /tmp/Releases/Martus-$BUILD_DATE.$BUILD_NUMBER.iso /cygdrive/h/Martus/ClientISO/ || exit
		cp /tmp/Releases/Martus-$BUILD_DATE.$BUILD_NUMBER.iso.md5 /cygdrive/h/Martus/ClientISO/ || exit
	else
		echo "Beneserve2 not available. You must move the Client ISO onto //Beneserve2/Engineering/Martus/ClientISO/ , its md5 has already been checked into CVS"
fi
	
	# add Single Installer into CVS
	if [ ! -d "$HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe/CVS" ]; then
		mkdir -p $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe/CVS || exit
		cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe/CVS
		touch Entries
		echo "binary-martus/Releases/ClientExe" > Repository
		echo $CVSROOT > Root
	fi
	
	# add Single exe md5 to CVS
	cp /tmp/Releases/MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe || exit
	cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe/
	echo "Adding to CVS: MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5"
	cvs.exe add MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5  || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5 || exit
	
	# move Single Exe onto Network
	if [ -d "/cygdrive/h/Martus/ClientExe" ]; then
		echo "Moving ClientExe onto network drive"
		cp /tmp/Releases/MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe /cygdrive/h/Martus/ClientExe/ || exit
		cp /tmp/Releases/MartusClient-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5 /cygdrive/h/Martus/ClientExe/ || exit
	else
		echo "Beneserve2 not available. You must move the Client Single Exe onto //Beneserve2/Engineering/Martus/ClientExe/ , its md5 has already been checked into CVS"
	fi
	
	# add Upgrade exe md5 to CVS
	cp /tmp/Releases/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5 $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe || exit
	cd $HOMEDRIVE/$HOMEPATH/binary-martus/Releases/ClientExe/
	echo "Adding to CVS: MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5"
	cvs.exe add MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5  || exit
	cvs.exe commit -m "v $cvs_date build $BUILD_NUMBER" MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5 || exit
	
	# move Upgrade Exe onto Network
	if [ -d "/cygdrive/h/Martus/ClientExe" ]; then
		echo "Moving MartusSetupUpgrade onto network drive"
		cp /tmp/Releases/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe /cygdrive/h/Martus/ClientExe/ || exit
		cp /tmp/Releases/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_DATE.$BUILD_NUMBER.exe.md5 /cygdrive/h/Martus/ClientExe/ || exit
	else
		echo "Beneserve2 not available. You must move the Client Upgrade Exe onto //Beneserve2/Engineering/Martus/ClientExe/ , its md5 has already been checked into CVS"
	fi
fi

echo
echo "The build completed succesfully. The Release files are located in /tmp/Releases/ ."

# burn the cd image if required
if [ $burn_cd = 'Y' ]; then
	echo "Ready to burn image onto CD. Make sure a blank CD is in the CD burner, then press any key to start:"
	read throw_away
	cdrecord dev=0,1,0 -v -eject -dao -data /tmp/Releases/Martus-$BUILD_DATE.$BUILD_NUMBER.iso
fi

exit 0
