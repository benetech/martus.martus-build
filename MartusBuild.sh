#!/bin/sh

#################################################
# Martus CD Build Script
#
# Notes: The script variables:
#				 CVSROOT HOMEDRIVE HOMEPATH
#				 need to be modified to suit
#				 your build environment
#
#				PATH must include jsdk & ant bin directories
#################################################

#################################################
#
#	TODO:
#		- update build to use downloaded jars
#			and jsdk for build
#
#################################################
set -u
#set -n

#NOTE: Add additional language-code and language-code to language-string mappings below
MARTUS_LANGUAGES="en es ru ar fr th"
export MARTUS_LANGUAGES

# language-code to language-name mapping
LANGUAGE_STRING="English"
getLangNameFromCode()
{
	LANGUAGE_CODE=$1
	
	case $LANGUAGE_CODE in
	"es")
	   LANGUAGE_STRING="Spanish";;
	"ru")
	   LANGUAGE_STRING="Russian";;
	"ar")
	   LANGUAGE_STRING="Arabic";;
	"fr")
	   LANGUAGE_STRING="French";;
	"th")
	   LANGUAGE_STRING="Thai";;
	*)
	   LANGUAGE_STRING="English";;
	esac
}

# error functions
error() { echo "ERROR: $*" >&2; exit 1; }
message() { echo "$*" >&2; }

# usage fn
usage()
{
	echo "Usage: $0 -cb [-s | -t]"
	echo -e "\t-s: Simple build. No tagging. Overrides -t"
	echo -e "\t-t: Tag CVS. Run TestAll, checks-in successful builds"
	echo -e "\t-c: Client Installers built"
	echo -e "\t-b: Burn a Client CD. Implies option -c"
}

setCvsEnvVars() # sets the primary environment vars for CVS access
{
	CVSROOT=:pserver:ivo@cvs.bookshare.org:/var/local/cvs
	HOMEDRIVE=c:
	HOMEPATH=\CVS_HOME
	CVS_HOME=$HOMEDRIVE/$HOMEPATH
	MARTUSINSTALLERPROJECT=$CVS_HOME/binary-martus/Installer
	MARTUSNSISPROJECTDIR=$MARTUSINSTALLERPROJECT/Win32_NSIS
	MARTUSBUILDFILES=$MARTUSINSTALLERPROJECT/BuildFiles

	MARTUSSOURCES=$CVS_HOME/MartusBuild

	PATH=/cygdrive/c/j2sdk1.4.2_03/bin:/cygdrive/c/java/ant-1.5/bin:$PATH

	CVS_DATE=`date '+%Y-%m-%d'`
	CVS_YEAR=`date '+%Y'`
	CVS_MONTH_DAY=`date '+%m%d'`

	INITIAL_DIR=`pwd`

	export CVSROOT HOMEDRIVE HOMEPATH CVS_HOME CVS_DATE CVS_YEAR CVS_MONTH_DAY
	export MARTUSSOURCES MARTUSINSTALLERPROJECT MARTUSNSISPROJECTDIR MARTUSBUILDFILES PATH INITIAL_DIR
}

cleanCvsHome() # Clean the build environment
{
	echo
	echo "Cleaning the build environment (ignore mount/umount messages)...";
	if [ -d "$CVS_HOME" ]; then
		rm -Rf $CVS_HOME
	fi
	mkdir $CVS_HOME
	mkdir $MARTUSSOURCES
	
	if [ ! -d "/MartusBuild" ]; then
		mount $MARTUSSOURCES /MartusBuild 2>&1
	fi
}

downloadSourcesFromCvs() # downloads sources from CVS
{
	cleanCvsHome
	martus_cvs_src_modules="client amplifier common jar-verifier hrdag meta server swing utils mspa logi"

	echo
	echo "Downloading source from CVS...";
	cd $CVS_HOME || error "unable to cd: err $?"
	
	for cvs_module in $martus_cvs_src_modules
		do
		cvs -q checkout martus-$cvs_module/source || error "cvs returned $? - for martus-$cvs_module"
		cvs -q -l checkout martus-$cvs_module || error "cvs returned $? - for -l martus-$cvs_module"
		echo "copying martus-$cvs_module...";
		echo
		cp -r $CVS_HOME/martus-$cvs_module/source/* $MARTUSSOURCES || error "copy martus-$cvs_module returned $?"
		echo
	done
	
	downloadMartusFilesFromCVS
	downloadMartusAmpPresentationFromCvs
	downloadMartusThirdpartyFromCvsAndSetup
	downloadMartusVerifyFromCvsAndSetup
	cleanBuildArea
	downloadMartusInstallerFromCvsAndSetup
} # downloadSourcesFromCvs

downloadMartusFilesFromCVS() # downloads martus files from CVS
{
	cd $CVS_HOME || error "unable to cd: err $?"
	
	cvs -q checkout -l -P martus || error "cvs checkout martus returned $?"
	echo "copying martus...";
	echo
	cp -r $CVS_HOME/martus/* $MARTUSSOURCES || error "copy martus returned $?"
	echo
} # downloadMartusFilesFromCVS

downloadMartusAmpPresentationFromCvs() # downloads martus-amp presentation files from CVS
{
	cd $CVS_HOME || error "unable to cd: err $?"
	
	cvs -q checkout martus-amplifier/presentation || error "cvs martus-amplifier/presentation returned $?"
	mkdir -p $MARTUSSOURCES/www/MartusAmplifier/presentation
	cp -r $CVS_HOME/martus-amplifier/presentation/* $MARTUSSOURCES/www/MartusAmplifier/presentation/ || error "copy martus-amplifier/presentation returned $?"
	echo
	
	cvs -q checkout martus-amplifier/presentationNonSSL || error "cvs martus-amplifier/presentationNonSSL returned $?"
	mkdir -p $MARTUSSOURCES/www/MartusAmplifier/presentationNonSSL
	cp -r $CVS_HOME/martus-amplifier/presentationNonSSL/* $MARTUSSOURCES/www/MartusAmplifier/presentationNonSSL/ || error "copy martus-amplifier/presentationNonSSL returned $?"
	echo
} # downloadMartusAmpPresentationFromCvs

downloadMartusThirdpartyFromCvsAndSetup() # downloads third-party items from CVS
{
	cd $CVS_HOME || error "unable to cd: err $?"
	
	common_thirdparty_jar_names="ant InfiniteMonkey XMLRPC"
	SRC_THIRDPARTY_JARS_COMMON_DIR=$CVS_HOME/martus-thirdparty/common

	server_thirdparty_jar_names="Jetty Lucene Velocity"
	SRC_THIRDPARTY_JARS_SERVER_DIR=$CVS_HOME/martus-thirdparty/server

	SRC_THIRDPARTY_JARS=$MARTUSSOURCES/ThirdPartyJars
	export SRC_THIRDPARTY_JARS
	
	cvs -q checkout martus-thirdparty || error "cvs checkout martus-thirdparty returned $?"
	mkdir $SRC_THIRDPARTY_JARS
	echo "copying martus-thirdparty...";
	echo
	
	for jar_name in $common_thirdparty_jar_names
		do
		cp $SRC_THIRDPARTY_JARS_COMMON_DIR/$jar_name/bin/*.jar $SRC_THIRDPARTY_JARS/ || error "copy $jar_name returned $?"
	done
	
	for jar_name in $server_thirdparty_jar_names
		do
		cp $SRC_THIRDPARTY_JARS_SERVER_DIR/$jar_name/bin/*.jar $SRC_THIRDPARTY_JARS/ || error "copy $jar_name returned $?"
	done
} #downloadMartusThirdpartyFromCvsAndSetup

downloadMartusVerifyFromCvsAndSetup() # download server martus verify from CVS and build
{
	cd $CVS_HOME || error "unable to cd: err $?"
	
	SRC_VERIFY=$CVS_HOME/martus-jar-verifier/source/org/martus/jarverifier
	export SRC_VERIFY
	
	cvs -q checkout martus-jar-verifier || error "cvs martus-jar-verifier returned $?"
	
	rm -f martus-jar-verifier/*.bat
	rm -f martus-jar-verifier/*.txt
	echo
	echo "Building Server JarVerifier...";
	cd $SRC_VERIFY/ || exit
	if [ -f "JarVerifier.class" ]; then
		rm -f JarVerifier.class
	fi
	javac JarVerifier.java
	status=$?
	if [ -f "JarVerifier.class" ]; then
		rm -f JarVerifier.java
	fi
	cd $CVS_HOME || exit
} # downloadMartusVerifyFromCvsAndSetup

cleanBuildArea() # remove CVS directories from build area
{
echo
echo "Cleaning build area...";
cd $MARTUSSOURCES
find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
cd $CVS_HOME || exit
} # cleanBuildArea

downloadMartusInstallerFromCvsAndSetup() # downloads installer from CVS and sets up CD Image
{
	if [ $build_client_cd = 0 ]; then
		echo
		echo "No CD Build necessary...";
		return
	fi
	echo
	echo "CD Build necessary, downloading installer from CVS...";
	cvs checkout binary-martus/Installer/ 2>&1 || error "cvs returned $?"
	
	copyThirdPartyJarToCDBuild
	copyThirdPartySourceToCDBuild
	copyThirdPartyLicenseToCDBuild
	cp $CVS_HOME/martus-thirdparty/common/InfiniteMonkey/bin/InfiniteMonkey.dll $MARTUSBUILDFILES/ProgramFiles/
} # downloadMartusInstallerFromCvsAndSetup

copyThirdPartyJarToCDBuild() # copies third-party jars into CD Image
{
	BUILDFILES_JARS=$MARTUSBUILDFILES/Jars
	SRC_THIRDPARTY_JARS_COMMON_DIR=$CVS_HOME/martus-thirdparty/common
	SRC_THIRDPARTY_JARS_LIBEXT_DIR=$CVS_HOME/martus-thirdparty/libext
	echo "Copying thirdparty jars to build location..."
	echo
	mkdir -p $BUILDFILES_JARS
	cd $CVS_HOME
	cp $SRC_THIRDPARTY_JARS_COMMON_DIR/InfiniteMonkey/bin/InfiniteMonkey.jar $BUILDFILES_JARS/
	cp $SRC_THIRDPARTY_JARS_COMMON_DIR/XMLRPC/bin/xmlrpc-*.jar $BUILDFILES_JARS/
	cp $SRC_THIRDPARTY_JARS_LIBEXT_DIR/BouncyCastle/bin/*.jar $BUILDFILES_JARS/
	cp $SRC_THIRDPARTY_JARS_LIBEXT_DIR/JUnit/bin/*.jar $BUILDFILES_JARS/
} # copyThirdPartyJarToCDBuild

copyThirdPartySourceToCDBuild() # copies third-party sources into CD Image
{
	BUILDFILES_SRC_FILES=$MARTUSBUILDFILES/SourceFiles
	rm -fr $BUILDFILES_SRC_FILES
	mkdir -p $BUILDFILES_SRC_FILES
	
	mkdir -p $BUILDFILES_SRC_FILES/BouncyCastle
	cp $CVS_HOME/martus-thirdparty/libext/BouncyCastle/source/* $BUILDFILES_SRC_FILES/BouncyCastle/
	
	mkdir -p $BUILDFILES_SRC_FILES/InfiniteMonkey
	cp $CVS_HOME/martus-thirdparty/common/InfiniteMonkey/source/* $BUILDFILES_SRC_FILES/InfiniteMonkey/

	mkdir -p $BUILDFILES_SRC_FILES/Installer/NSIS
	cp $CVS_HOME/martus-thirdparty/client/installer/Win32/NSIS/source/* $BUILDFILES_SRC_FILES/Installer/NSIS/
	
	mkdir -p $BUILDFILES_SRC_FILES/junit
	cp $CVS_HOME/martus-thirdparty/libext/JUnit/source/* $BUILDFILES_SRC_FILES/junit/
	
	mkdir -p $BUILDFILES_SRC_FILES/Logi
	cp $CVS_HOME/martus-thirdparty/common/Logi/source/* $BUILDFILES_SRC_FILES/Logi/
	
	mkdir -p $BUILDFILES_SRC_FILES/Sun
	cp $CVS_HOME/martus-thirdparty/client/Sun/source/* $BUILDFILES_SRC_FILES/Sun/
	
	mkdir -p $BUILDFILES_SRC_FILES/xmlrpc
	cp $CVS_HOME/martus-thirdparty/common/XMLRPC/source/* $BUILDFILES_SRC_FILES/xmlrpc/
	
	cd $BUILDFILES_SRC_FILES
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
} # copyThirdPartySourceToCDBuild

copyThirdPartyLicenseToCDBuild() # copies third-party license info to CD Image
{
	BUILDFILES_LICENSES=$MARTUSBUILDFILES/Documents/Licenses
	rm -fr $BUILDFILES_LICENSES
	
	mkdir -p $BUILDFILES_LICENSES/BouncyCastle
	cp $CVS_HOME/martus-thirdparty/libext/BouncyCastle/license/* $BUILDFILES_LICENSES/BouncyCastle/
	
	mkdir -p $BUILDFILES_LICENSES/InfiniteMonkey
	cp $CVS_HOME/martus-thirdparty/common/InfiniteMonkey/license/* $BUILDFILES_LICENSES/InfiniteMonkey/
	
	mkdir -p $BUILDFILES_LICENSES/JUnit
	cp $CVS_HOME/martus-thirdparty/libext/JUnit/license/* $BUILDFILES_LICENSES/JUnit/
	
	mkdir -p $BUILDFILES_LICENSES/Logi
	cp $CVS_HOME/martus-thirdparty/common/Logi/license/* $BUILDFILES_LICENSES/Logi/
	
	mkdir -p $BUILDFILES_LICENSES/Sun
	cp $CVS_HOME/martus-thirdparty/client/Sun/license/* $BUILDFILES_LICENSES/Sun/
	
	mkdir -p $BUILDFILES_LICENSES/Xml-Rpc
	cp $CVS_HOME/martus-thirdparty/common/XMLRPC/license/* $BUILDFILES_LICENSES/Xml-Rpc/
	
	cd $BUILDFILES_LICENSES
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
} # copyThirdPartyLicenseToCDBuild

setupBuildEnvironment() # sets up environment for build
{
	CURRENT_VERSION=pre2.5-internal
	BUILD_DATE=`date '+%Y%m%d'`
	
	# the build number below relies on the Ant task that creates and autoincrements this file
	BUILD_NUMBER_FILE="$MARTUSSOURCES/build.number"
	if [ -f "$BUILD_NUMBER_FILE" ]; then
		BUILD_NUMBER=`cat $BUILD_NUMBER_FILE | grep build.number | cut -d'=' -f2`
	else
		BUILD_NUMBER=1
	fi
	
	MARTUS_ZIP_NAME=MartusClient-$CURRENT_VERSION-src.zip
	
	echo "Build is v $CURRENT_VERSION, b $BUILD_NUMBER, date $BUILD_DATE"
	echo
	
	BUILD_OUTPUT_DIR=$MARTUSSOURCES/dist
	RELEASE_DIR=/tmp/Releases
	BUILD_VERNUM_TAG=$BUILD_DATE.$BUILD_NUMBER
	
	export CURRENT_VERSION BUILD_NUMBER BUILD_DATE MARTUS_ZIP_NAME BUILD_NUMBER_FILE RELEASE_DIR BUILD_VERNUM_TAG
} # setupBuildEnvironment

startAntBuild() # initiates the Ant build
{
	echo
	echo "Starting the ant build (might take a minute)..."
	cd /MartusBuild
	if [ $cvs_tag = 1 ]; then
		ant md5
		#ant md5-no-tests
	else
		ant build
	fi
	status=$?
	
	# check the build.number file back into CVS
	cd $CVS_HOME
	echo "Updating to CVS: $BUILD_NUMBER_FILE"
	cp -f $BUILD_NUMBER_FILE $CVS_HOME/martus/
	cvs commit -m "v $CVS_DATE" ${CVS_HOME}/martus/build.number
	
	if [ $status != 0 ]; then
		echo
		echo "Build Failed!"
		cd $INITIAL_DIR
	fi
	
	echo
	echo "Ant completed with status: $status"
	
	if [ ! -f "$BUILD_OUTPUT_DIR/martus.jar" ]; then
		echo "BUILD FAILED!! Exit status $status"
		echo "Please note any messages above"
		echo "Cleaning up..."
		cd /
		#rm -Rf $CVS_HOME
		cd $INITIAL_DIR
		echo "Exiting..."
		exit 1
	fi
	
	if [ $cvs_tag = 1 ]; then
		if [ ! -f "$BUILD_OUTPUT_DIR/martus.jar.md5" ]; then
			echo "BUILD FAILED!! Missing md5. Exit status $status"
			echo "Please note any messages above"
			echo "Cleaning up..."
			cd /
			#rm -Rf $CVS_HOME
			cd $INITIAL_DIR
			echo "Exiting..."
			exit 1
		fi
	fi
	cd $INITIAL_DIR
} # startAntBuild

copyAntBuildToCDBuild() # copies successful build to CD Image
{
	echo
	echo "Moving martus.jar to temp CD build location..."
	if [ -d "$RELEASE_DIR" ]; then
		rm -fR $RELEASE_DIR
	fi
	mkdir -p $RELEASE_DIR
	
	JARNAME_CLIENT_ORIG=martus
	JARNAME_CLIENT_FINAL=$JARNAME_CLIENT_ORIG-$BUILD_VERNUM_TAG
	export JARNAME_CLIENT_ORIG JARNAME_CLIENT_FINAL
	
	JARNAME_SERVER_ORIG=martus-server
	JARNAME_SERVER_FINAL=$JARNAME_SERVER_ORIG-$BUILD_VERNUM_TAG
	export JARNAME_SERVER_ORIG JARNAME_SERVER_FINAL
	
	JARNAME_MSPA_ORIG=martus-mspa-client
	JARNAME_MSPA_FINAL=$JARNAME_MSPA_ORIG-$BUILD_VERNUM_TAG
	export JARNAME_MSPA_ORIG JARNAME_MSPA_FINAL
	
	JARNAME_META_ORIG=martus-meta
	JARNAME_META_FINAL=$JARNAME_META_ORIG-$BUILD_VERNUM_TAG
	export JARNAME_META_ORIG JARNAME_META_FINAL
	
	cp -v $BUILD_OUTPUT_DIR/$JARNAME_SERVER_ORIG.jar $RELEASE_DIR/$JARNAME_SERVER_FINAL.jar || exit
	if [ -f "$BUILD_OUTPUT_DIR/$JARNAME_SERVER_ORIG.jar.MD5" ]; then
		echo -e "\n" >> "$BUILD_OUTPUT_DIR/$JARNAME_SERVER_ORIG.jar.MD5"
		cp -v $BUILD_OUTPUT_DIR/$JARNAME_SERVER_ORIG.jar.MD5 $RELEASE_DIR/$JARNAME_SERVER_FINAL.jar.md5 || exit
	fi
	
	cp -v $BUILD_OUTPUT_DIR/$JARNAME_MSPA_ORIG.jar $RELEASE_DIR/$JARNAME_MSPA_FINAL.jar || exit
	if [ -f "$BUILD_OUTPUT_DIR/$JARNAME_MSPA_ORIG.jar.MD5" ]; then
		echo -e "\n" >> "$BUILD_OUTPUT_DIR/$JARNAME_MSPA_ORIG.jar.MD5"
		cp -v $BUILD_OUTPUT_DIR/$JARNAME_MSPA_ORIG.jar.MD5 $RELEASE_DIR/$JARNAME_MSPA_FINAL.jar.md5 || exit
	fi
	
	cp -v $BUILD_OUTPUT_DIR/$JARNAME_META_ORIG.jar $RELEASE_DIR/$JARNAME_META_FINAL.jar || exit
	if [ -f "$BUILD_OUTPUT_DIR/$JARNAME_META_ORIG.jar.MD5" ]; then
		echo -e "\n" >> "$BUILD_OUTPUT_DIR/$JARNAME_META_ORIG.jar.MD5"
		cp $BUILD_OUTPUT_DIR/$JARNAME_META_ORIG.jar.MD5 $RELEASE_DIR/$JARNAME_META_FINAL.jar.md5 || exit
	fi
	
	cp -v $BUILD_OUTPUT_DIR/$JARNAME_CLIENT_ORIG.jar $RELEASE_DIR/$JARNAME_CLIENT_ORIG.jar || exit
	cp -v $BUILD_OUTPUT_DIR/$JARNAME_CLIENT_ORIG.jar $RELEASE_DIR/$JARNAME_CLIENT_FINAL.jar || exit
	if [ -f "$BUILD_OUTPUT_DIR/$JARNAME_CLIENT_ORIG.jar.MD5" ]; then
		echo -e "\n" >> "$BUILD_OUTPUT_DIR/$JARNAME_CLIENT_ORIG.jar.MD5"
		cp -v $BUILD_OUTPUT_DIR/$JARNAME_CLIENT_ORIG.jar.MD5 $RELEASE_DIR/$JARNAME_CLIENT_FINAL.jar.md5 || exit
	fi
} # copyAntBuildToCDBuild

updateCvsTree() # updates CVS with successful builds
{
	if [ $cvs_tag = 0 ]; then
		return
	fi
	
	echo
	echo "Labeling CVS with tag: v${CVS_DATE}_build-$BUILD_NUMBER"
	cd $CVS_HOME
	cvs tag v${CVS_DATE}_build-$BUILD_NUMBER martus martus-client martus-amplifier martus-common martus-jar-verifier martus-hrdag martus-meta martus-server martus-swing martus-utils martus-mspa martus-logi || error "Unable to add tag to CVS. Check any error messages displayed."
	cvs tag v${CVS_DATE}_build-$BUILD_NUMBER martus-thirdparty
	
	#check if ClientJar directory structure already exists, if not add it
	cvs checkout -l binary-martus/Releases/ClientJar || error "cvs checkout -l binary-martus/Releases/ClientJar - returned $?"
	cvs checkout -l binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR" ]; then
		# create fake CVS entries
		if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientJar/CVS" ]; then
			mkdir -p $CVS_HOME/binary-martus/Releases/ClientJar/CVS || exit
			touch $CVS_HOME/binary-martus/Releases/ClientJar/CVS/Entries
			echo "binary-martus/Releases/ClientJar" > $CVS_HOME/binary-martus/Releases/ClientJar/CVS/Repository
			echo $CVSROOT > $CVS_HOME/binary-martus/Releases/ClientJar/CVS/Root
		fi
		
		mkdir $CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR
		cd $CVS_HOME/binary-martus/Releases/ClientJar/
		cvs add $CVS_YEAR  || exit
	fi
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY" ]; then
		mkdir $CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY
		cd $CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR
		cvs add $CVS_MONTH_DAY || exit
	fi
	
	# add client to CVS
	CVS_CLIENTJAR_DIR=$CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY
	cp -v $RELEASE_DIR/martus-$BUILD_VERNUM_TAG.jar $CVS_CLIENTJAR_DIR/ || error "unable to copy"
	cp -v $RELEASE_DIR/martus-$BUILD_VERNUM_TAG.jar.md5 $CVS_CLIENTJAR_DIR/ || error "unable to copy"
	echo "Adding to CVS: $JARNAME_CLIENT_FINAL.jar"
	cd $CVS_CLIENTJAR_DIR
	cvs add $JARNAME_CLIENT_FINAL.jar  || error "unable to cvs add $JARNAME_CLIENT_FINAL.jar"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_CLIENT_FINAL.jar || error "unable to commit client jar"
	cvs add $CVS_CLIENTJAR_DIR/$JARNAME_CLIENT_FINAL.jar.md5  || error "unable to cvs add $JARNAME_CLIENT_FINAL.jar.md5"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_CLIENT_FINAL.jar.md5 || error "unable to commit client jar md5"

	#check if ServerJar directory structure already exists, if not add it
	cd $CVS_HOME
	cvs checkout -l binary-martus/Releases/ServerJar || error "cvs returned $?"
	cvs checkout -l binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR" ]; then
		# create fake CVS entries
		if [ ! -d "$CVS_HOME/binary-martus/Releases/ServerJar/CVS" ]; then
			mkdir -p $CVS_HOME/binary-martus/Releases/ServerJar/CVS || exit
			touch $CVS_HOME/binary-martus/Releases/ServerJar/CVS/Entries
			echo "binary-martus/Releases/ServerJar" > $CVS_HOME/binary-martus/Releases/ServerJar/CVS/Repository
			echo $CVSROOT > $CVS_HOME/binary-martus/Releases/ServerJar/CVS/Root
		fi
		
		mkdir $CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR
		cd $CVS_HOME/binary-martus/Releases/ServerJar/
		cvs.exe add $CVS_YEAR  || error "Unable to add to cvs: $CVS_YEAR"
	fi
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY" ]; then
		mkdir $CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY
		cd $CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR
		cvs.exe add $CVS_MONTH_DAY || error "Unable to add to cvs: $CVS_MONTH_DAY"
	fi

	# add server to CVS
	CVS_SERVERJAR_DIR=$CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY
	cp -v $RELEASE_DIR/$JARNAME_SERVER_FINAL.jar $CVS_SERVERJAR_DIR/ || error "Unable to copy"
	cp -v $RELEASE_DIR/$JARNAME_SERVER_FINAL.jar.md5 $CVS_SERVERJAR_DIR || error "Unable to copy"
	
	cp -v $RELEASE_DIR/$JARNAME_MSPA_FINAL.jar $CVS_SERVERJAR_DIR || error "Unable to copy"
	cp -v $RELEASE_DIR/$JARNAME_MSPA_FINAL.jar.md5 $CVS_SERVERJAR_DIR || error "Unable to copy"
	
	echo "Adding to CVS: $JARNAME_SERVER_FINAL.jar"
	cd $CVS_SERVERJAR_DIR
	cvs add $JARNAME_SERVER_FINAL.jar || error "Unable to cvs add $JARNAME_SERVER_FINAL.jar"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_SERVER_FINAL.jar || error "Unable to cvs commit $JARNAME_SERVER_FINAL.jar"
	cvs add $JARNAME_SERVER_FINAL.jar.md5 || error "Unable to cvs add $JARNAME_SERVER_FINAL.jar.md5"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_SERVER_FINAL.jar.md5 || error "Unable to cvs commit $JARNAME_SERVER_FINAL.jar.md5"
	
	cvs add $JARNAME_MSPA_FINAL.jar || error "Unable to cvs add $JARNAME_MSPA_FINAL.jar"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_MSPA_FINAL.jar || error "Unable to cvs commit $JARNAME_MSPA_FINAL.jar"
	cvs add $JARNAME_MSPA_FINAL.jar.md5 || error "Unable to cvs add $JARNAME_MSPA_FINAL.jar.md5"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_MSPA_FINAL.jar.md5 || error "Unable to cvs commit $JARNAME_MSPA_FINAL.jar.md5"
	
	# add meta to CVS
	cp -v $RELEASE_DIR/$JARNAME_META_FINAL.jar $CVS_SERVERJAR_DIR/ || error "Unable to copy"
	cp -v $RELEASE_DIR/$JARNAME_META_FINAL.jar.md5 $CVS_SERVERJAR_DIR/ || error "Unable to copy"
	echo "Adding to CVS: $JARNAME_META_FINAL.jar"
	cd $CVS_SERVERJAR_DIR
	cvs add $JARNAME_META_FINAL.jar || error "Unable to cvs add $JARNAME_META_FINAL.jar"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_META_FINAL.jar || error "Unable to cvs commit $JARNAME_META_FINAL.jar"
	cvs add $JARNAME_META_FINAL.jar.md5 || error "Unable to cvs add $JARNAME_META_FINAL.jar.md5"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $JARNAME_META_FINAL.jar.md5 || error "Unable to cvs commit $JARNAME_META_FINAL.jar"
} # updateCvsTree

removeUnnecesarryBuildFiles() # removes files uncessesary from CD Image
{
	cd $INITIAL_DIR
	echo
	echo "removing extra dirs & files (ignore any File not Found messages)...";
	rm -fR $BUILD_OUTPUT_DIR
	rm -fR $MARTUSSOURCES/TechDocs
	rm -fR $MARTUSSOURCES/org/martus/meta
	rm -fR $MARTUSSOURCES/org/martus/server
	rm -fR $MARTUSSOURCES/org/martus/mspa
	rm -fR $MARTUSSOURCES/org/martus/amplifier
	rm -fR $MARTUSSOURCES/org/martus/jarverifier
	rm -f $MARTUSSOURCES/.classpath
	rm -f $MARTUSSOURCES/.project
	rm -f $MARTUSSOURCES/build.number
	rm -f $MARTUSSOURCES/build.properties
} # removeUnnecesarryBuildFiles

createInstallerLicenseFile() # creates license file
{
	# create the license file for the installer
	if [ -f "$MARTUSBUILDFILES/combined-license.txt" ]; then
		rm -f $MARTUSBUILDFILES/combined-license.txt
	fi
	cat $MARTUSBUILDFILES/Documents/license.txt > $MARTUSBUILDFILES/combined-license.txt
	echo -e "\n\n\t**********************************\n\n" >> $MARTUSBUILDFILES/combined-license.txt
	cat $MARTUSBUILDFILES/Documents/gpl.txt >> $MARTUSBUILDFILES/combined-license.txt
} # createInstallerLicenseFile

fixNewlinesInTxtFiles()
{
	unix2dos.exe --unix2dos $MARTUSBUILDFILES/combined-license.txt
	find $MARTUSBUILDFILES/Documents -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	find $MARTUSBUILDFILES/verify -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	find $MARTUSBUILDFILES/Winsock95 -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	find $BUILDFILES_SRC_FILES -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
}

copyLicenseTextFiles()
{
	cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSSOURCES/
	cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSSOURCES/
	
	cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSBUILDFILES/ProgramFiles/
	cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSBUILDFILES/ProgramFiles/
	
	cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSBUILDFILES/Verify/
	cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSBUILDFILES/Verify/
	
	cp $MARTUSBUILDFILES/Documents/license.txt "$BUILDFILES_SRC_FILES/Installer/NSIS Scripts/"
	cp $MARTUSBUILDFILES/Documents/gpl.txt "$BUILDFILES_SRC_FILES/Installer/NSIS Scripts/"
} # copyLicenseTextFiles

zipSources()
{
	MARTUS_ZIP_PATH=$MARTUSSOURCES/$MARTUS_ZIP_NAME
	echo
	echo "zipping up sources..."
	cd $MARTUSSOURCES
	find . -name "*.java" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "build.xml" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "*.gif" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "*.jpg" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "*.png" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "license.txt" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "gpl.txt" -print | zip $MARTUS_ZIP_PATH -q@
	find . -name "main-class.txt" -print | zip $MARTUS_ZIP_PATH -q@
	
	echo
	echo "zipping up language files...";
	for martus_lang in $MARTUS_LANGUAGES
		do
		echo -e "\tzipping language: ${martus_lang}"
		find . -name "MartusHelpTOC-${martus_lang}.txt" -print | zip $MARTUS_ZIP_PATH -q@
		find . -name "MartusHelp-${martus_lang}.txt" -print | zip $MARTUS_ZIP_PATH -q@
		find . -name "Martus-${martus_lang}.mtf" -print | zip $MARTUS_ZIP_PATH -q@
	done

	#unofficial
	find . -name "UnofficialTranslationMessage.txt" -print | zip $MARTUS_ZIP_PATH -q@
	
	echo
	echo "zipping third party items..."
	cd $BUILDFILES_SRC_FILES
	
	find ./BouncyCastle -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
	find ./Sun -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
	find ./InfiniteMonkey -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
	find ./junit -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
	find ./xmlrpc -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
	find ./logi -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
	find ./Installer -type "f" -name "*" -print | zip $MARTUS_ZIP_PATH -q@
} # zipSources

buildClientJarVerifier()
{
	echo
	echo "Building JarVerifier...";
	cd $MARTUSBUILDFILES/Verify/source/org/martus/jarverifier || error "cannot cd to $MARTUSBUILDFILES/Verify/source/org/martus/jarverifier"
	if [ -f "JarVerifier.class" ]; then
		rm -f JarVerifier.class
	fi
	javac JarVerifier.java
	status=$?
	
	if [ ! -f "JarVerifier.class" ]; then
		echo "BUILD FAILED!!, exit status $status!!"
		exit $status
	fi
} # buildClientJarVerifier

createAndFixCdDocuments()
{
	createInstallerLicenseFile
	fixNewlinesInTxtFiles
	copyLicenseTextFiles
	zipSources
} # createAndFixCdDocuments

createClientInstallers()
{
	cd $INITIAL_DIR
	echo
	echo "Starting the installer build..."
	
	RELEASE_FILE=$RELEASE_DIR/martus.jar
	export RELEASE_FILE
	
	if [ ! -f "$RELEASE_FILE" ]; then
		error "No Martus.jar was found to use in the build...."
	fi
	cp -v $RELEASE_FILE "$MARTUSBUILDFILES/ProgramFiles/"
	
	removeUnnecesarryBuildFiles
	
	# moving installer scripts
	INSTALLER_SRC_FILES="$BUILDFILES_SRC_FILES/Installer/NSIS Scripts"
	if [ ! -d "$INSTALLER_SRC_FILES" ]; then
		echo
		echo "Creating $INSTALLER_SRC_FILES..."
		mkdir -p "$INSTALLER_SRC_FILES"
	fi
	
	cp -v $MARTUSNSISPROJECTDIR/*.nsi "$INSTALLER_SRC_FILES/" || error "Unable to copy *.nsi files"
	mkdir "$INSTALLER_SRC_FILES/locallang"

	for martus_lang_code in $MARTUS_LANGUAGES
		do
		getLangNameFromCode $martus_lang_code
		cp -v $MARTUSNSISPROJECTDIR/locallang/${LANGUAGE_STRING}.* "$INSTALLER_SRC_FILES/locallang/" || error "Unable to copy locallang ${martus_lang_code} files"
	done
	
	find $CVS_HOME -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	find $MARTUSSOURCES -type "f" -name "*.class" -exec rm -fR '{}' \; > /dev/null
	
	createAndFixCdDocuments
	buildClientJarVerifier
	createInstallerCdImage
	createCdNsisInstaller
	createSingleNsisInstaller
	createUpgradeInstaller
	createCdIso
} # createClientInstallers

createInstallerCdImage()
{
	CD_IMAGE_DIR=$RELEASE_DIR/CD_IMAGE
	export CD_IMAGE_DIR
	
	mkdir -p $CD_IMAGE_DIR
	cp -v $MARTUSBUILDFILES/Documents/license.txt $CD_IMAGE_DIR
	
	mkdir -p $CD_IMAGE_DIR/Win95
	cp -v $MARTUSBUILDFILES/Winsock95/* $CD_IMAGE_DIR/Win95/
	
	cp -v $MARTUSBUILDFILES/ProgramFiles/autorun.inf $CD_IMAGE_DIR
	
	mkdir -p $CD_IMAGE_DIR/Martus
	cp -v $MARTUSBUILDFILES/ProgramFiles/* $CD_IMAGE_DIR/Martus/
	rm -f $CD_IMAGE_DIR/Martus/autorun.inf
	cp -v $MARTUSBUILDFILES/Documents/license.txt $CD_IMAGE_DIR/Martus/
	cp -v $MARTUSBUILDFILES/Documents/gpl.txt $CD_IMAGE_DIR/Martus/
	
	mkdir -p $CD_IMAGE_DIR/verify/
	cp -v $MARTUSBUILDFILES/Verify/readme_verify.txt $CD_IMAGE_DIR/verify/
	cp -v $MARTUSBUILDFILES/Verify/gpl.txt $CD_IMAGE_DIR/verify/
	cp -v $MARTUSBUILDFILES/Verify/license.txt $CD_IMAGE_DIR/verify/
	cp -v $MARTUSBUILDFILES/Verify/*.bat $CD_IMAGE_DIR/verify/
	
	mkdir -p $CD_IMAGE_DIR/Martus/Docs
	cp -v $MARTUSBUILDFILES/Documents/README.txt $CD_IMAGE_DIR
	cp -v $MARTUSBUILDFILES/Documents/martus_user_guide.pdf $CD_IMAGE_DIR/Martus/Docs
	cp -v $MARTUSBUILDFILES/Documents/quickstartguide.pdf $CD_IMAGE_DIR/Martus/Docs
	
	for martus_lang in $MARTUS_LANGUAGES
		do
		echo -e "\tcopying docs for language: ${martus_lang}"
		cp -v $MARTUSBUILDFILES/Documents/README_${martus_lang}.txt $CD_IMAGE_DIR || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/README_${martus_lang}.txt"
		cp -v $MARTUSBUILDFILES/Documents/*_${martus_lang}.pdf $CD_IMAGE_DIR/Martus/Docs || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/*_${martus_lang}.pdf"
		cp -v $MARTUSBUILDFILES/Verify/readme_verify_${martus_lang}.txt $CD_IMAGE_DIR/verify/ || message "ERROR: Unable to copy $CD_IMAGE_DIR/readme_verify_${martus_lang}.txt"
	done
	
	cp -v $MARTUSBUILDFILES/Documents/LinuxJavaInstall.txt $CD_IMAGE_DIR/Martus/Docs/
	
	cp -vr $BUILDFILES_LICENSES $CD_IMAGE_DIR/Martus/Docs/
	
	mkdir -p $CD_IMAGE_DIR/LibExt
	cp -v $MARTUSBUILDFILES/Jars/* $CD_IMAGE_DIR/LibExt/
	
	mkdir -p $CD_IMAGE_DIR/Sources/
	cp -v $MARTUSSOURCES/$MARTUS_ZIP_NAME $CD_IMAGE_DIR/Sources/
	
	mkdir -p $CD_IMAGE_DIR/Java/Linux/i586
	cd "$MARTUSBUILDFILES/Java redist/Linux/i586/"
	cp -v *.bin $CD_IMAGE_DIR/Java/Linux/i586/
	
	cd $CD_IMAGE_DIR
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	cd $INITIAL_DIR
} # createInstallerCdImage

createCdNsisInstaller()
{
	cd $MARTUSNSISPROJECTDIR
	if [ -f "$MARTUSNSISPROJECTDIR/MartusSetup.exe" ]; then
		echo
		echo "Removing $MARTUSNSISPROJECTDIR/MartusSetup.exe"
		rm -f $MARTUSNSISPROJECTDIR/MartusSetup.exe
	fi
	echo
	echo "starting the CD NSIS installer build...";
	
	makensis.exe /V2 NSIS_Martus.nsi
	if [ -f "$MARTUSNSISPROJECTDIR/MartusSetup.exe" ]; then
		echo
		echo "Build CD NSIS installer succeded..."
	else
		echo
		echo "Build CD NSIS installer FAILED..."
		exit 1
	fi
	cp -v $MARTUSNSISPROJECTDIR/MartusSetup.exe $CD_IMAGE_DIR/
	cd $INITIAL_DIR
} # createCdNsisInstaller

createSingleNsisInstaller()
{
	cp -v $MARTUSSOURCES/$MARTUS_ZIP_NAME $MARTUSBUILDFILES/ || error "zip copy failed"
	
	cd $MARTUSNSISPROJECTDIR
	if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupSingle.exe" ]; then
		rm -f $MARTUSNSISPROJECTDIR/MartusSetupSingle.exe
	fi
	echo "starting the Single NSIS installer build...";
	
	makensis.exe /V2 NSIS_Martus_Single.nsi
	if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupSingle.exe" ]; then
		echo
		echo "Build Single NSIS installer succeded..."
		cp "$MARTUSNSISPROJECTDIR/MartusSetupSingle.exe" $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe
	else
		echo
		echo "Build Single NSIS installer FAILED..."
		exit 1
	fi
	
	echo
	echo "generating md5sums of Single installer..."
	cd $RELEASE_DIR
	md5sum $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe > $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5
	echo -e "\n" >> $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5
} # createSingleNsisInstaller

createUpgradeInstaller()
{
	cd $MARTUSNSISPROJECTDIR
	if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe" ]; then
		rm -f $MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe
	fi
	echo "starting the Upgrade NSIS installer build...";
	
	makensis.exe /V2 NSIS_Martus_Upgrade.nsi
	if [ -f "$MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe" ]; then
		echo
		echo "Build Upgrade NSIS installer succeded..."
		cp -v "$MARTUSNSISPROJECTDIR/MartusSetupUpgrade.exe" $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe
	else
		echo
		echo "Build Upgrade NSIS installer FAILED..."
		exit 1
	fi
	
	echo
	echo "generating md5sums of Upgrade..."
	cd $RELEASE_DIR
	md5sum $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe > $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5
	echo -e "\n" >> $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5
	
	cd $INITIAL_DIR
} # createUpgradeInstaller

createCdIso()
{
	echo 
	echo "Creating ISO..."
	if [ -f "$MARTUSBUILDFILES/Martus-$BUILD_VERNUM_TAG.iso" ]; then
		rm -f $MARTUSBUILDFILES/Martus-$BUILD_VERNUM_TAG.iso
	fi
	
	mkisofs -J -r -T -hide-joliet-trans-tbl -l -V Martus-$BUILD_VERNUM_TAG -o $MARTUSBUILDFILES/Martus-$BUILD_VERNUM_TAG.iso $CD_IMAGE_DIR || error "mkisofs returned $?"
	   
	if [ ! -d "$RELEASE_DIR" ]; then
		mkdir -p $RELEASE_DIR
	fi
	
	if [ -f "$RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso" ]; then
		rm -f $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso
	fi
	mv $MARTUSBUILDFILES/Martus-$BUILD_VERNUM_TAG.iso $RELEASE_DIR/
	
	echo
	echo "generating md5sums of ISO..."
	cd $RELEASE_DIR
	md5sum $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso > $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.md5
	echo -e "\n" >> $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.md5
} # createCdIso

updateCvsTreeWithBinaries()
{
	if [ $cvs_tag = 0 ]; then
		return
	fi

	cd $CVS_HOME || exit	

	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientISO/CVS" ]; then
		mkdir -p $CVS_HOME/binary-martus/Releases/ClientISO/CVS || exit
		cd $CVS_HOME/binary-martus/Releases/ClientISO/CVS
		touch Entries
		echo "binary-martus/Releases/ClientISO" > Repository
		echo $CVSROOT > Root
	fi

	# add ISO md5 to CVS
	cp $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.md5 $CVS_HOME/binary-martus/Releases/ClientISO || exit
	cd $CVS_HOME/binary-martus/Releases/ClientISO/
	echo "Adding to CVS: Martus-$BUILD_VERNUM_TAG.iso.md5"
	cvs.exe add Martus-$BUILD_VERNUM_TAG.iso.md5  || exit
	cvs.exe commit -m "v $CVS_DATE build $BUILD_NUMBER" Martus-$BUILD_VERNUM_TAG.iso.md5 || exit
	
	# move ISO onto Network
	if [ -d "/cygdrive/h/Martus/ClientISO" ]; then
		echo "Moving ISO onto network drive"
		cp $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso /cygdrive/h/Martus/ClientISO/ || exit
		cp $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.md5 /cygdrive/h/Martus/ClientISO/ || exit
	else
		echo "Beneserve2 not available. You must move the Client ISO onto //Beneserve2/Engineering/Martus/ClientISO/ , its md5 has already been checked into CVS"
	fi
	
	# add Single Installer into CVS
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientExe/CVS" ]; then
		mkdir -p $CVS_HOME/binary-martus/Releases/ClientExe/CVS || exit
		cd $CVS_HOME/binary-martus/Releases/ClientExe/CVS
		touch Entries
		echo "binary-martus/Releases/ClientExe" > Repository
		echo $CVSROOT > Root
	fi
	
	# add Single exe md5 to CVS
	cp $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5 $CVS_HOME/binary-martus/Releases/ClientExe || exit
	cd $CVS_HOME/binary-martus/Releases/ClientExe/
	echo "Adding to CVS: MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5"
	cvs.exe add MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5  || exit
	cvs.exe commit -m "v $CVS_DATE build $BUILD_NUMBER" MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5 || exit
	
	# move Single Exe onto Network
	if [ -d "/cygdrive/h/Martus/ClientExe" ]; then
		echo "Moving ClientExe onto network drive"
		cp $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe /cygdrive/h/Martus/ClientExe/ || exit
		cp $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5 /cygdrive/h/Martus/ClientExe/ || exit
	else
		echo "Beneserve2 not available. You must move the Client Single Exe onto //Beneserve2/Engineering/Martus/ClientExe/ , its md5 has already been checked into CVS"
	fi
	
	# add Upgrade exe md5 to CVS
	cp $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5 $CVS_HOME/binary-martus/Releases/ClientExe || exit
	cd $CVS_HOME/binary-martus/Releases/ClientExe/
	echo "Adding to CVS: MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5"
	cvs.exe add MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5  || exit
	cvs.exe commit -m "v $CVS_DATE build $BUILD_NUMBER" MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5 || exit
	
	# move Upgrade Exe onto Network
	if [ -d "/cygdrive/h/Martus/ClientExe" ]; then
		echo "Moving MartusSetupUpgrade onto network drive"
		cp $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe /cygdrive/h/Martus/ClientExe/ || exit
		cp $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.md5 /cygdrive/h/Martus/ClientExe/ || exit
	else
		echo "Beneserve2 not available. You must move the Client Upgrade Exe onto //Beneserve2/Engineering/Martus/ClientExe/ , its md5 has already been checked into CVS"
	fi
}

####### main program #######
if [ $# = 0 ]; then
	usage
	exit 1
fi

cvs_tag=0
build_client_cd=0
burn_client_cd=0
simple_build=0

while getopts ":tcbs" opt; do
	case $opt in
		t ) cvs_tag=1
			export cvs_tag;;
		c ) build_client_cd=1
			export build_client_cd;;
		b ) burn_client_cd=1
			build_client_cd=1
			export build_client_cd burn_client_cd;;
		s ) simple_build=1
			cvs_tag=0
			export simple_build cvs_tag;;
		\? ) usage
			exit 1 ;;
	esac
done

if [ $simple_build = 1 ]; then
	cvs_tag=0
	export cvs_tag
fi


echo 
if [ $build_client_cd = 1 ]; then
	echo "- Client installers to be built"
else
	echo "- Client installers NOT built"
fi

if [ $burn_client_cd = 1 ]; then
	echo "- Will burn to a CD"
else
	echo "- Will NOT burn to a CD"
fi

if [ $cvs_tag = 1 ]; then
	echo "- Tagging CVS after successful build"
else
	echo "- NOT tagging CVS after successful build"
fi

setCvsEnvVars

# move cwd to a neutral position
cd / || exit

downloadSourcesFromCvs
setupBuildEnvironment
startAntBuild
copyAntBuildToCDBuild
updateCvsTree

if [ $build_client_cd = 0 ]; then
	echo
	echo "The build completed succesfully. The built jars are in $RELEASE_DIR."
	exit 0
fi

# clean up the sources dir
if [ -d "$SRC_THIRDPARTY_JARS" ]; then
	rm -Rf $SRC_THIRDPARTY_JARS
fi

if [ -d "$SRC_VERIFY" ]; then
	rm -Rf $SRC_VERIFY
fi

if [ -d "$MARTUSSOURCES/www" ]; then
	rm -Rf $MARTUSSOURCES/www
fi

createClientInstallers
updateCvsTreeWithBinaries

echo
echo "The build completed succesfully. The Release files are located in $RELEASE_DIR/ ."

# burn the cd image if required
if [ $burn_client_cd = 1 ]; then
	echo "Ready to burn image onto CD. Make sure a blank CD is in the CD burner, then press Enter to start:"
	read throw_away
	cdrecord dev=0,1,0 -v -eject -dao -data $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso
fi

cd $INITIAL_DIR

exit 0
