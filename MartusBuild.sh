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
#################################################
set -u
#set -n

#NOTE: Add additional language-code and language-code to language-string mappings below
MARTUS_LANGUAGES="en es ru ar fr th fa ne"
export MARTUS_LANGUAGES

#################################################
# language-code to language-name mapping
#################################################
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
	"fa")
	   LANGUAGE_STRING="Farsi";;
	*)
	   LANGUAGE_STRING="English";;
	esac
}

#################################################
# error functions
#################################################
error() { echo "ERROR: $*" >&2; exit 1; }
message() { echo "$*" >&2; }

#################################################
# usage fn
#################################################
usage()
{
	echo "Usage: $0 -cb [-s | -t]"
	echo -e "\t-s: Simple build. No tagging. Overrides -t"
	echo -e "\t-t: Tag CVS. Run TestAll, checks-in successful builds"
	echo -e "\t-c: Client Installers built"
	echo -e "\t-b: Burn a Client CD. Implies option -c"
}

#################################################
# sets the primary environment vars for CVS access
#################################################
setCvsEnvVars()
{
	CVSROOT=:ext:ivocvs@10.10.220.21:/var/local/cvs
	HOMEDRIVE=c:
	HOMEPATH=\CVS_HOME
	CVS_HOME=$HOMEDRIVE/$HOMEPATH
	MARTUSINSTALLERPROJECT=$CVS_HOME/binary-martus/Installer
	MARTUSNSISPROJECTDIR=$MARTUSINSTALLERPROJECT/Win32_NSIS
	MARTUSBUILDFILES=$MARTUSINSTALLERPROJECT/BuildFiles
	
	RELEASE_DIR=/cygdrive/c/SharedDocs/MatusReleases
	PREVIOUS_RELEASE_DIR=/cygdrive/c/SharedDocs/Prev.MatusReleases

	PATH=/cygdrive/c/j2sdk1.4.2_07/bin:/cygdrive/c/java/apache-ant-1.6.2/bin:$PATH

	CVS_DATE=`date '+%Y-%m-%d'`
	CVS_DATE_FILENAME=`date '+%Y%m%d'`
	CVS_YEAR=`date '+%Y'`
	CVS_MONTH_DAY=`date '+%m%d'`

	INITIAL_DIR=`pwd`

	first_char_in_path=${0:0:1};
	if [ $first_char_in_path = "/" ]; then
		CURRENT_SCRIPT="$0";
	elif [ $first_char_in_path = "." ]; then
		CURRENT_SCRIPT=${0/#\./$INITIAL_DIR};
	else
		CURRENT_SCRIPT="$INITIAL_DIR/$0";
	fi
	
	if [ ! -f "$CURRENT_SCRIPT" ]; then
		echo
		error "CURRENT_SCRIPT doesnt exist: $CURRENT_SCRIPT"
	fi

	CLASSPATH=$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/common/Ant/bin/ant.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/common/Ant/bin/ant-junit.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/common/PersianCalendar/bin/persiancalendar.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/common/PersianCalendar/bin/icu4j_3_2_calendar.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/libext/JUnit/bin/junit.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/libext/BouncyCastle/bin/bcprov-jdk14-128.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/client/RhinoJavaScript/bin/js.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/build/java-mail/bin/mail.jar)
	CLASSPATH=$CLASSPATH\;$(cygpath -w /cygdrive/c/CVS_HOME/martus-thirdparty/build/java-activation-framework/bin/activation.jar)

	export CVSROOT HOMEDRIVE HOMEPATH CVS_HOME CVS_DATE CVS_YEAR CVS_MONTH_DAY CVS_DATE_FILENAME
	export MARTUSINSTALLERPROJECT MARTUSNSISPROJECTDIR MARTUSBUILDFILES PATH INITIAL_DIR RELEASE_DIR PREVIOUS_RELEASE_DIR CLASSPATH CURRENT_SCRIPT
}

#################################################
# Clean the build environment
#################################################
cleanCvsHome()
{
	echo
	echo "Cleaning the build environment $CVS_HOME (ignore mount/umount messages)...";
	if [ -d "$CVS_HOME" ]; then
		rm -Rf $CVS_HOME
	fi
	mkdir $CVS_HOME
}

#################################################
# downloads sources from CVS
#################################################
downloadSourcesFromCvs()
{
	cleanCvsHome
	cd "$CVS_HOME" || error "unable to cd: err $?"
	echo
	echo "Downloading source from CVS...";	
	cvs -q checkout -l -P martus || error "cvs checkout martus returned $?"

	martus_cvs_src_modules="client amplifier common jar-verifier hrdag meta server swing utils mspa logi bc-jce clientside js-xml-generator thirdparty"
	
	for cvs_module in $martus_cvs_src_modules
		do
		cvs -q checkout martus-$cvs_module || error "cvs returned $? - for martus-$cvs_module"
		echo
	done

	# get a listing of language files
	AVAILABLE_MTFS=`find martus-client/source/org/martus/client/swingui/ -type "f" -name "Martus-*.mtf"`
	AVAILABLE_HELP=`find martus-client/source/org/martus/client/swingui/ -type "f" -name "MartusHelp-*.txt"`
	AVAILABLE_TOC=`find martus-client/source/org/martus/client/swingui/ -type "f" -name "MartusHelpTOC-*.txt"`
	
	file_listings=$AVAILABLE_MTFS;
	removeFilesWithIncorrectLanguageCode;
	
	file_listings=$AVAILABLE_HELP;
	removeFilesWithIncorrectLanguageCode;
	
	file_listings=$AVAILABLE_TOC;
	removeFilesWithIncorrectLanguageCode;

	downloadMartusInstallerFromCvsAndSetup
} # downloadSourcesFromCvs

#################################################
# received a list of files, deletes those that have language code that
#  are not in language code list
#################################################
removeFilesWithIncorrectLanguageCode()
{
	for name in $file_listings;
	do
		part1=${name%.*}
		lang_code=${part1#*Martus*-}

		for lang in $MARTUS_LANGUAGES;
		do
			delete=1;
			if [ $lang_code = $lang ]; then
				delete=0;
				break;
			fi
		done
		if [ $delete = 1 ]; then
			echo "deleting $name";
			rm -v $name;
		fi
	done
}

#################################################
# downloads installer from CVS and sets up CD Image
#################################################
downloadMartusInstallerFromCvsAndSetup()
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

#################################################
# copies third-party jars into CD Image
#################################################
copyThirdPartyJarToCDBuild() 
{
	BUILDFILES_JARS=$MARTUSBUILDFILES/Jars
	SRC_THIRDPARTY_JARS_COMMON_DIR=$CVS_HOME/martus-thirdparty/common
	SRC_THIRDPARTY_JARS_LIBEXT_DIR=$CVS_HOME/martus-thirdparty/libext
	SRC_THIRDPARTY_JARS_CLIENT_DIR=$CVS_HOME/martus-thirdparty/client
	echo
	echo "Copying thirdparty jars to build location..."
	mkdir -p $BUILDFILES_JARS
	cd "$CVS_HOME"
	cp -v $SRC_THIRDPARTY_JARS_COMMON_DIR/InfiniteMonkey/bin/InfiniteMonkey.jar $BUILDFILES_JARS/
	cp -v $SRC_THIRDPARTY_JARS_COMMON_DIR/XMLRPC/bin/xmlrpc-*.jar $BUILDFILES_JARS/
	cp -v $SRC_THIRDPARTY_JARS_COMMON_DIR/PersianCalendar/bin/*.jar $BUILDFILES_JARS/
	cp -v $SRC_THIRDPARTY_JARS_COMMON_DIR/Velocity/bin/*.jar $BUILDFILES_JARS/
	cp -v $SRC_THIRDPARTY_JARS_LIBEXT_DIR/BouncyCastle/bin/*.jar $BUILDFILES_JARS/
	cp -v $SRC_THIRDPARTY_JARS_LIBEXT_DIR/JUnit/bin/*.jar $BUILDFILES_JARS/
	cp -v $SRC_THIRDPARTY_JARS_CLIENT_DIR/RhinoJavaScript/bin/*.jar $BUILDFILES_JARS/
	
} # copyThirdPartyJarToCDBuild

#################################################
# copies third-party sources into CD Image
#################################################
copyThirdPartySourceToCDBuild() 
{
	BUILDFILES_SRC_FILES=$MARTUSBUILDFILES/SourceFiles
	rm -fr $BUILDFILES_SRC_FILES
	mkdir -p $BUILDFILES_SRC_FILES
	
	mkdir -p $BUILDFILES_SRC_FILES/BouncyCastle
	cp $CVS_HOME/martus-thirdparty/libext/BouncyCastle/source/* $BUILDFILES_SRC_FILES/BouncyCastle/
	
	mkdir -p $BUILDFILES_SRC_FILES/InfiniteMonkey
	cp -v $CVS_HOME/martus-thirdparty/common/InfiniteMonkey/source/* $BUILDFILES_SRC_FILES/InfiniteMonkey/

	mkdir -p $BUILDFILES_SRC_FILES/Installer/NSIS
	cp -v $CVS_HOME/martus-thirdparty/client/installer/Win32/NSIS/source/* $BUILDFILES_SRC_FILES/Installer/NSIS/
	
	mkdir -p $BUILDFILES_SRC_FILES/junit
	cp -v $CVS_HOME/martus-thirdparty/libext/JUnit/source/* $BUILDFILES_SRC_FILES/junit/
	
	mkdir -p $BUILDFILES_SRC_FILES/Logi
	cp -v $CVS_HOME/martus-thirdparty/common/Logi/source/* $BUILDFILES_SRC_FILES/Logi/
	
	mkdir -p $BUILDFILES_SRC_FILES/Rhino
	cp -v $CVS_HOME/martus-thirdparty/client/RhinoJavaScript/source/* $BUILDFILES_SRC_FILES/RhinoJavaScript/
	
	mkdir -p $BUILDFILES_SRC_FILES/Sun
	cp -v $CVS_HOME/martus-thirdparty/client/Sun/source/* $BUILDFILES_SRC_FILES/Sun/
	
	mkdir -p $BUILDFILES_SRC_FILES/xmlrpc
	cp -v $CVS_HOME/martus-thirdparty/common/XMLRPC/source/* $BUILDFILES_SRC_FILES/xmlrpc/
	
	mkdir -p $BUILDFILES_SRC_FILES/PersianCalendar
	cp -v $CVS_HOME/martus-thirdparty/common/PersianCalendar/source/* $BUILDFILES_SRC_FILES/PersianCalendar/
	
	mkdir -p $BUILDFILES_SRC_FILES/Velocity
	cp -v $CVS_HOME/martus-thirdparty/common/Velocity/source/* $BUILDFILES_SRC_FILES/Velocity/
	
	cd "$BUILDFILES_SRC_FILES"
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	
} # copyThirdPartySourceToCDBuild

#################################################
# copies third-party license info to CD Image
#################################################
copyThirdPartyLicenseToCDBuild() 
{
	BUILDFILES_LICENSES=$MARTUSBUILDFILES/Documents/Licenses
	rm -fr $BUILDFILES_LICENSES
	
	mkdir -p $BUILDFILES_LICENSES/BouncyCastle
	cp -v $CVS_HOME/martus-thirdparty/libext/BouncyCastle/license/* $BUILDFILES_LICENSES/BouncyCastle/
	
	mkdir -p $BUILDFILES_LICENSES/InfiniteMonkey
	cp -v $CVS_HOME/martus-thirdparty/common/InfiniteMonkey/license/* $BUILDFILES_LICENSES/InfiniteMonkey/
	
	mkdir -p $BUILDFILES_LICENSES/JUnit
	cp -v $CVS_HOME/martus-thirdparty/libext/JUnit/license/* $BUILDFILES_LICENSES/JUnit/
	
	mkdir -p $BUILDFILES_LICENSES/Logi
	cp -v $CVS_HOME/martus-thirdparty/common/Logi/license/* $BUILDFILES_LICENSES/Logi/
	
	mkdir -p $BUILDFILES_LICENSES/Sun
	cp -v $CVS_HOME/martus-thirdparty/client/Sun/license/* $BUILDFILES_LICENSES/Sun/
	
	mkdir -p $BUILDFILES_LICENSES/Rhino
	cp -v $CVS_HOME/martus-thirdparty/client/RhinoJavaScript/license/* $BUILDFILES_LICENSES/RhinoJavaScript/
	
	mkdir -p $BUILDFILES_LICENSES/Xml-Rpc
	cp -v $CVS_HOME/martus-thirdparty/common/XMLRPC/license/* $BUILDFILES_LICENSES/Xml-Rpc/
	
	mkdir -p $BUILDFILES_LICENSES/PersianCalendar
	cp -v $CVS_HOME/martus-thirdparty/common/PersianCalendar/license/* $BUILDFILES_LICENSES/PersianCalendar/
	
	mkdir -p $BUILDFILES_LICENSES/Velocity
	cp -v $CVS_HOME/martus-thirdparty/common/Velocity/license/* $BUILDFILES_LICENSES/Velocity/
	
	cd "$BUILDFILES_LICENSES"
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	
} # copyThirdPartyLicenseToCDBuild

#################################################
# sets up environment for build
#################################################
setupBuildEnvironment() 
{
	BUILD_PROPERTY_FILE="$CVS_HOME/martus/build.properties"
	CURRENT_VERSION=`cat $BUILD_PROPERTY_FILE | grep martus.version | cut -d'=' -f2`
	BUILD_DATE=`date '+%Y%m%d'`
	
	# the build number below relies on the Ant task that creates and autoincrements this file
	BUILD_NUMBER_FILE="$CVS_HOME/martus/build.number"
	if [ -f "$BUILD_NUMBER_FILE" ]; then
		BUILD_NUMBER=`cat $BUILD_NUMBER_FILE | grep build.number | cut -d'=' -f2`
	else
		BUILD_NUMBER=1
	fi
	
	echo
	echo "Build is v $CURRENT_VERSION, b $BUILD_NUMBER, date $BUILD_DATE"
		
	BUILD_OUTPUT_DIR=$CVS_HOME/martus/dist
	BUILD_VERNUM_TAG=$BUILD_DATE.$BUILD_NUMBER
	
	export CURRENT_VERSION BUILD_NUMBER BUILD_DATE BUILD_NUMBER_FILE BUILD_VERNUM_TAG
} # setupBuildEnvironment

#################################################
# initiates the Ant build
#################################################
startAntBuild() 
{
	echo
	echo "Starting the ant build (might take a minute)..."
	cd "$CVS_HOME/martus"
	if [ $cvs_tag = 1 ]; then
		ant -f build-meta.xml release
	else
		ant -f build-meta.xml nosign.release
	fi
	status=$?
	
	# check the build.number file back into CVS
	echo
	echo "Updating to CVS: $BUILD_NUMBER_FILE"
	cvs commit -m "v $CVS_DATE" build.number
	
	if [ $status != 0 ]; then
		echo
		echo "Build Failed!"
		cd "$INITIAL_DIR"
	fi
	
	echo
	echo "Ant completed with status: $status"
	
	MARTUS_JAR_FILE=$BUILD_OUTPUT_DIR/martus-client-$CVS_DATE_FILENAME.$BUILD_NUMBER.jar
	if [ ! -f "$MARTUS_JAR_FILE" ]; then
		echo "BUILD FAILED!! Exit status $status"
		echo "Unable to find $MARTUS_JAR_FILE"
		echo "Please note any messages above"
		echo "Cleaning up..."
		cd /
		#rm -Rf $CVS_HOME
		cd "$INITIAL_DIR"
		echo "Exiting..."
		exit 1
	fi
	
	if [ $cvs_tag = 1 ]; then
		if [ ! -f "$MARTUS_JAR_FILE.sha" ]; then
			echo "BUILD FAILED!! Missing sha. Exit status $status"
			echo "Please note any messages above"
			echo "Unable to find $MARTUS_JAR_FILE.sha"
			echo "Cleaning up..."
			cd /
			#rm -Rf $CVS_HOME
			cd "$INITIAL_DIR"
			echo "Exiting..."
			exit 1
		fi
	fi
	cd "$INITIAL_DIR"
	
	# clean up temp dir
	if [ -d "$TEMP" ]; then
		find "$TEMP" -type "d" -name "\$\$\$*" -exec rm -fR '{}' \; > /dev/null
		find "$TEMP" -type "f" -name "\$\$\$*" -exec rm -fR '{}' \; > /dev/null
	fi

} # startAntBuild

#################################################
# copies successful build to CD Image
#################################################
copyAntBuildToCDBuild() 
{
	echo
	echo "Moving martus.jar to temp CD build location..."
	if [ -d "$PREVIOUS_RELEASE_DIR" ]; then
		rm -fR "$PREVIOUS_RELEASE_DIR"
	fi	

	if [ -d "$RELEASE_DIR" ]; then
		mv "$RELEASE_DIR" "$PREVIOUS_RELEASE_DIR"
	fi
	mkdir -p $RELEASE_DIR
	
	cp -v $BUILD_OUTPUT_DIR/*.jar $RELEASE_DIR/ || exit
	cp -v $BUILD_OUTPUT_DIR/*.zip $RELEASE_DIR/ || exit
	cp -v $BUILD_OUTPUT_DIR/*.sha $RELEASE_DIR/ || exit
	cp -v $BUILD_OUTPUT_DIR/martus-client-$CVS_DATE_FILENAME.$BUILD_NUMBER.jar $RELEASE_DIR/martus.jar
	
	if [ $build_client_cd = 0 ]; then
		return
	fi
	
	# copy bc-jce
	cp -v "$BUILD_OUTPUT_DIR/bc-jce-$CVS_DATE_FILENAME.$BUILD_NUMBER.jar" "$BUILDFILES_JARS/bc-jce.jar" || error "Unable to copy bc-jce jar"
	
} # copyAntBuildToCDBuild

#################################################
# updates CVS with successful builds
#################################################
updateCvsTree() 
{
	if [ $cvs_tag = 0 ]; then
		return
	fi
	
	# add build script to CVS
	echo 
	echo "Adding $CURRENT_SCRIPT to cvs"
	cp -v "$CURRENT_SCRIPT" "$CVS_HOME/martus/MartusBuild.sh"
	cd "$CVS_HOME/martus/"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" "MartusBuild.sh"
	cvs tag v${CVS_DATE}_build-$BUILD_NUMBER "MartusBuild.sh"
	
	# add bc-jce signature file to CVS
	echo 
	echo "Adding bc-jce signature file to cvs"
	cd "$CVS_HOME/martus-common/source/org/martus/common/crypto/"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" "SSMTSJAR.SIG"
	cvs tag v${CVS_DATE}_build-$BUILD_NUMBER "SSMTSJAR.SIG"
	
	echo
	echo "Labeling CVS with tag: v${CVS_DATE}_build-$BUILD_NUMBER"
	cd "$CVS_HOME"
	cvs tag v${CVS_DATE}_build-$BUILD_NUMBER martus martus-client martus-amplifier martus-common martus-jar-verifier martus-hrdag martus-meta martus-server martus-swing martus-utils martus-mspa martus-logi martus-bc-jce martus-clientside martus-js-xml-generator || error "Unable to add tag to CVS. Check any error messages displayed."
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
		cd "$CVS_HOME/binary-martus/Releases/ClientJar/"
		cvs add $CVS_YEAR  || exit
	fi
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY" ]; then
		mkdir $CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY
		cd "$CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR"
		cvs add $CVS_MONTH_DAY || exit
	fi
	
	# add client to CVS
	CVS_CLIENTJAR_DIR=$CVS_HOME/binary-martus/Releases/ClientJar/$CVS_YEAR/$CVS_MONTH_DAY
	cp -v $RELEASE_DIR/martus-client-*.jar $CVS_CLIENTJAR_DIR/ || error "unable to copy"
	cp -v $RELEASE_DIR/martus-client-*.jar.sha $CVS_CLIENTJAR_DIR/ || error "unable to copy"
	echo "Adding to client jar to CVS."
	cd "$CVS_CLIENTJAR_DIR"
	for filename in *$CVS_DATE_FILENAME.$BUILD_NUMBER.jar
		do
		cvs add $filename  || error "unable to cvs add $filename"
		cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $filename || error "unable to commit $filename"
		cvs tag v${CVS_DATE}_build-$BUILD_NUMBER $filename
	done
	
	for filename in *$CVS_DATE_FILENAME.$BUILD_NUMBER.jar.sha
		do
		cvs add $filename  || error "unable to cvs add $filename"
		cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $filename || error "unable to commit $filename"
		cvs tag v${CVS_DATE}_build-$BUILD_NUMBER $filename
	done

	#check if ServerJar directory structure already exists, if not add it
	cd "$CVS_HOME"
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
		cd "$CVS_HOME/binary-martus/Releases/ServerJar/"
		cvs.exe add $CVS_YEAR  || error "Unable to add to cvs: $CVS_YEAR"
	fi
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY" ]; then
		mkdir $CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY
		cd "$CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR"
		cvs.exe add $CVS_MONTH_DAY || error "Unable to add to cvs: $CVS_MONTH_DAY"
	fi

	# add server to CVS
	CVS_SERVERJAR_DIR=$CVS_HOME/binary-martus/Releases/ServerJar/$CVS_YEAR/$CVS_MONTH_DAY
	cp -v $RELEASE_DIR/martus-server-*.jar $CVS_SERVERJAR_DIR/ || error "Unable to copy"
	cp -v $RELEASE_DIR/martus-server-*.jar.sha $CVS_SERVERJAR_DIR || error "Unable to copy"
	
	cp -v $RELEASE_DIR/martus-meta-*.jar $CVS_SERVERJAR_DIR || error "Unable to copy"
	cp -v $RELEASE_DIR/martus-meta-*.jar.sha $CVS_SERVERJAR_DIR || error "Unable to copy"
	
	cp -v $RELEASE_DIR/martus-mspa-client-*.jar $CVS_SERVERJAR_DIR/ || error "Unable to copy"
	cp -v $RELEASE_DIR/martus-mspa-client-*.jar.sha $CVS_SERVERJAR_DIR/ || error "Unable to copy"
	
	echo "Adding to server jars to CVS"
	cd "$CVS_SERVERJAR_DIR"
	for filename in *$CVS_DATE_FILENAME.$BUILD_NUMBER.jar
		do
		cvs add $filename  || error "unable to cvs add $filename"
		cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $filename || error "unable to commit $filename"
		cvs tag v${CVS_DATE}_build-$BUILD_NUMBER $filename
	done
	
	for filename in *$CVS_DATE_FILENAME.$BUILD_NUMBER.jar.sha
		do
		cvs add $filename  || error "unable to cvs add $filename"
		cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" $filename || error "unable to commit $filename"
		cvs tag v${CVS_DATE}_build-$BUILD_NUMBER $filename
	done
	
	# add bc-jce to CVS
	cp -v "$RELEASE_DIR/bc-jce-$CVS_DATE_FILENAME.$BUILD_NUMBER.jar" "$CVS_HOME/martus-bc-jce/bc-jce.jar" || error "Unable to copy bc-jce jar to cvs checkin directory"
	cd "$CVS_HOME/martus-bc-jce/"
	cvs commit -m "v $CVS_DATE build $BUILD_NUMBER" "bc-jce.jar"
	cvs tag v${CVS_DATE}_build-$BUILD_NUMBER "bc-jce.jar"
} # updateCvsTree

#################################################
# removes files uncessesary from CD Image
#################################################
#TODO: get rid of this
removeUnnecesarryBuildFiles() 
{
	cd "$INITIAL_DIR"
	echo
	echo "removing extra dirs & files (ignore any File not Found messages)...";
	rm -fR $CVS_HOME/martus/TechDocs
	rm -fR $CVS_HOME/martus/org/martus/meta
	rm -fR $CVS_HOME/martus/org/martus/server
	rm -fR $CVS_HOME/martus/org/martus/mspa
	rm -fR $CVS_HOME/martus/org/martus/amplifier
	rm -fR $CVS_HOME/martus/org/martus/jarverifier
	rm -f $CVS_HOME/martus/.classpath
	rm -f $CVS_HOME/martus/.project
	rm -f $CVS_HOME/martus/build.number
	rm -f $CVS_HOME/martus/build.properties
} # removeUnnecesarryBuildFiles

#################################################
# 
#################################################
buildClientJarVerifier()
{
	echo
	echo "Building JarVerifier...";
	cd "$MARTUSBUILDFILES/Verify/source/org/martus/jarverifier" || error "cannot cd to $MARTUSBUILDFILES/Verify/source/org/martus/jarverifier"
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

#################################################
# 
#################################################
createAndFixCdDocuments()
{
	# create the license file for the installer
	if [ -f "$MARTUSBUILDFILES/combined-license.txt" ]; then
		rm -f $MARTUSBUILDFILES/combined-license.txt
	fi
	cat $MARTUSBUILDFILES/Documents/license.txt > $MARTUSBUILDFILES/combined-license.txt
	echo -e "\n\n\t**********************************\n\n" >> $MARTUSBUILDFILES/combined-license.txt
	cat $MARTUSBUILDFILES/Documents/gpl.txt >> $MARTUSBUILDFILES/combined-license.txt
	
	unix2dos.exe --unix2dos $MARTUSBUILDFILES/combined-license.txt
	find $MARTUSBUILDFILES/Documents -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	find $MARTUSBUILDFILES/verify -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	find $MARTUSBUILDFILES/Winsock95 -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	find $BUILDFILES_SRC_FILES -type "f" -name "*.txt" -exec unix2dos.exe --unix2dos '{}' \; > /dev/null
	
	cp $MARTUSBUILDFILES/Documents/license.txt $CVS_HOME/martus/
	cp $MARTUSBUILDFILES/Documents/gpl.txt $CVS_HOME/martus/
	
	cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSBUILDFILES/ProgramFiles/
	cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSBUILDFILES/ProgramFiles/
	
	cp $MARTUSBUILDFILES/Documents/license.txt $MARTUSBUILDFILES/Verify/
	cp $MARTUSBUILDFILES/Documents/gpl.txt $MARTUSBUILDFILES/Verify/
	
	cp $MARTUSBUILDFILES/Documents/license.txt "$BUILDFILES_SRC_FILES/Installer/NSIS Scripts/"
	cp $MARTUSBUILDFILES/Documents/gpl.txt "$BUILDFILES_SRC_FILES/Installer/NSIS Scripts/"
} # createAndFixCdDocuments

#################################################
# 
#################################################
createClientInstallers()
{
	cd "$INITIAL_DIR"
	echo
	echo "Starting the installer build..."
	
	RELEASE_FILE=$RELEASE_DIR/martus.jar
	export RELEASE_FILE
	
	if [ ! -f "$RELEASE_FILE" ]; then
		error "No Martus.jar was found to use in the build...."
	fi
	cp -v $RELEASE_FILE "$MARTUSBUILDFILES/ProgramFiles/martus.jar"
	
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
	
	mkdir "$INSTALLER_SRC_FILES/common"
	cp -v $MARTUSNSISPROJECTDIR/common/*.nsi "$INSTALLER_SRC_FILES/common" || error "Unable to copy *.nsi files"
	
	createAndFixCdDocuments
	buildClientJarVerifier
	createInstallerCdImage
	createMacLinuxZip
	createCdNsisInstaller
	createSingleNsisInstaller
	createUpgradeInstaller
	createCdIso
	createPieces
} # createClientInstallers

#################################################
# 
#################################################
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
		cp -v $MARTUSBUILDFILES/Documents/martus_user_guide_${martus_lang}.pdf $CD_IMAGE_DIR/Martus/Docs || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/martus_user_guide_${martus_lang}.pdf"
		cp -v $MARTUSBUILDFILES/Documents/quickstartguide_${martus_lang}.pdf $CD_IMAGE_DIR/Martus/Docs || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/quickstartguide_${martus_lang}.pdf"
		cp -v $MARTUSBUILDFILES/Verify/readme_verify_${martus_lang}.txt $CD_IMAGE_DIR/verify/ || message "ERROR: Unable to copy $CD_IMAGE_DIR/readme_verify_${martus_lang}.txt"
	done
	
	cp -v $MARTUSBUILDFILES/Documents/LinuxJavaInstall.txt $CD_IMAGE_DIR/Martus/Docs/
	
	cp -vr $BUILDFILES_LICENSES $CD_IMAGE_DIR/Martus/Docs/
	
	mkdir -p $CD_IMAGE_DIR/LibExt
	cp -v $MARTUSBUILDFILES/Jars/* $CD_IMAGE_DIR/LibExt/
	
	mkdir -p $CD_IMAGE_DIR/Sources/
	cp -v $CVS_HOME/martus/dist/martus-client-*.zip $CD_IMAGE_DIR/Sources/martus-client-$CURRENT_VERSION-src.zip
	
	mkdir -p $CD_IMAGE_DIR/Java/Linux/i586
	cd "$MARTUSBUILDFILES/Java redist/Linux/i586/"
	cp -v *.bin $CD_IMAGE_DIR/Java/Linux/i586/
	
	cd "$CD_IMAGE_DIR"
	find . -type "d" -name "CVS" -exec rm -fR '{}' \; > /dev/null
	cd "$INITIAL_DIR"
} # createInstallerCdImage

#################################################
# 
#################################################
function createMacLinuxZip()
{
	echo
	echo "Creating Mac/Linux zip file..."
	mkdir /tmp/MartusClient-$CURRENT_VERSION
	
	# copy verify
	cd "$CD_IMAGE_DIR"
	cp -v -r * /tmp/MartusClient-$CURRENT_VERSION/
	
	# remove unnecessary stuff
	find /tmp/MartusClient-$CURRENT_VERSION -type "f" -name "*.dll" -exec rm -vfR '{}' \; > /dev/null
	find /tmp/MartusClient-$CURRENT_VERSION -type "f" -name "*.exe" -exec rm -vfR '{}' \; > /dev/null
	find /tmp/MartusClient-$CURRENT_VERSION -type "f" -name "*.inf" -exec rm -vfR '{}' \; > /dev/null
	rm -vfr /tmp/MartusClient-$CURRENT_VERSION/Win95
	
	# zip up to release dir
	ZIPFILE_NAME="$RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG-MacLinux.zip"
	cd /tmp
	zip -r9v "$ZIPFILE_NAME" "MartusClient-$CURRENT_VERSION"

	if [ ! -f "$ZIPFILE_NAME" ]; then
		echo ""
		echo "Error: Unable to create $ZIPFILE_NAME !"
	else
		sha1sum "$ZIPFILE_NAME" > "$ZIPFILE_NAME.sha"
	fi
	
	rm -vfr /tmp/MartusClient-$CURRENT_VERSION
}

#################################################
# 
#################################################
createCdNsisInstaller()
{
	cd "$MARTUSNSISPROJECTDIR"
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
	cd "$INITIAL_DIR"
} # createCdNsisInstaller

#################################################
# 
#################################################
createSingleNsisInstaller()
{
	cp -v $CVS_HOME/martus/dist/martus-client-*.zip $MARTUSBUILDFILES/ || error "zip copy failed"
	
	cd "$MARTUSNSISPROJECTDIR"
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
	echo "generating checksums of Single installer..."
	cd "$RELEASE_DIR"
	sha1sum $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe > $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha
	echo -e "\n" >> $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha
} # createSingleNsisInstaller

#################################################
# 
#################################################
createUpgradeInstaller()
{
	cd "$MARTUSNSISPROJECTDIR"
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
	echo "generating checksums of Upgrade..."
	cd "$RELEASE_DIR"
	sha1sum $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe > $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha
	echo -e "\n" >> $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha
	
	cd "$INITIAL_DIR"
} # createUpgradeInstaller

#################################################
# 
#################################################
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
	echo "generating checksums of ISO..."
	cd "$RELEASE_DIR"
	sha1sum $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso > $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.sha
	echo -e "\n" >> $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.sha
} # createCdIso

#################################################
# 
#################################################
createPieces()
{
	echo 
	echo "Creating Pieces..."
	SPLITTER_PROGRAM="$MARTUSBUILDFILES/MartusSetupLauncher/filesplit-2.0.100/bin/filesplit.exe"
	if [ ! -f "$SPLITTER_PROGRAM" ]; then
		echo
		echo "Error: Splitter program not available!"
		return
	fi
	
	if [ ! -f "$RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe" ]; then
		echo
		echo "Error: cannot find $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe!"
		return
	fi
	
	if [ ! -d "$RELEASE_DIR/Pieces" ]; then
		mkdir -p "$RELEASE_DIR/Pieces"
	fi
	
	WINDOWS_RELEASE_DIR=$(cygpath -w $RELEASE_DIR)
	
	cd "$RELEASE_DIR"
	$SPLITTER_PROGRAM -s "MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe" 1400 "Pieces"
	
	echo
	echo "generating checksums of Pieces..."
	cd "$RELEASE_DIR/Pieces"
	for filename in *.cnk
		do
		sha1sum $filename > $filename.sha
		echo -e "\n" >> $filename.sha
	done
	
} # createPieces

#################################################
# 
#################################################
updateCvsTreeWithBinaries()
{
	if [ $cvs_tag = 0 ]; then
		return
	fi

	cd "$CVS_HOME" || exit	

	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientISO/CVS" ]; then
		mkdir -p $CVS_HOME/binary-martus/Releases/ClientISO/CVS || exit
		cd "$CVS_HOME/binary-martus/Releases/ClientISO/CVS"
		touch Entries
		echo "binary-martus/Releases/ClientISO" > Repository
		echo $CVSROOT > Root
	fi

	# add ISO checksum to CVS
	cp $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.sha $CVS_HOME/binary-martus/Releases/ClientISO || exit
	cd "$CVS_HOME/binary-martus/Releases/ClientISO/"
	echo "Adding to CVS: Martus-$BUILD_VERNUM_TAG.iso.sha"
	cvs.exe add Martus-$BUILD_VERNUM_TAG.iso.sha  || exit
	cvs.exe commit -m "v $CVS_DATE build $BUILD_NUMBER" Martus-$BUILD_VERNUM_TAG.iso.sha || exit
	
	# move ISO onto Network
	if [ -d "/cygdrive/h/Martus/ClientISO" ]; then
		echo "Moving ISO onto network drive"
		cp $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso /cygdrive/h/Martus/ClientISO/ || exit
		cp $RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso.sha /cygdrive/h/Martus/ClientISO/ || exit
	else
		echo "Beneserve2 not available. You must move the Client ISO onto //Beneserve2/Engineering/Martus/ClientISO/ , its sha has already been checked into CVS"
	fi
	
	# add Single Installer into CVS
	if [ ! -d "$CVS_HOME/binary-martus/Releases/ClientExe/CVS" ]; then
		mkdir -p $CVS_HOME/binary-martus/Releases/ClientExe/CVS || exit
		cd "$CVS_HOME/binary-martus/Releases/ClientExe/CVS"
		touch Entries
		echo "binary-martus/Releases/ClientExe" > Repository
		echo $CVSROOT > Root
	fi
	
	# add Single exe sha to CVS
	cp $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha $CVS_HOME/binary-martus/Releases/ClientExe || exit
	cd "$CVS_HOME/binary-martus/Releases/ClientExe/"
	echo "Adding to CVS: MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha"
	cvs.exe add MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha  || exit
	cvs.exe commit -m "v $CVS_DATE build $BUILD_NUMBER" MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha || exit
	
	# move Single Exe onto Network
	if [ -d "/cygdrive/h/Martus/ClientExe" ]; then
		echo "Moving ClientExe onto network drive"
		cp $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe /cygdrive/h/Martus/ClientExe/ || exit
		cp $RELEASE_DIR/MartusClient-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha /cygdrive/h/Martus/ClientExe/ || exit
	else
		echo "Beneserve2 not available. You must move the Client Single Exe onto //Beneserve2/Engineering/Martus/ClientExe/ , its sha has already been checked into CVS"
	fi
	
	# add Upgrade exe sha to CVS
	cp $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha $CVS_HOME/binary-martus/Releases/ClientExe || exit
	cd "$CVS_HOME/binary-martus/Releases/ClientExe/"
	echo "Adding to CVS: MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha"
	cvs.exe add MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha  || exit
	cvs.exe commit -m "v $CVS_DATE build $BUILD_NUMBER" MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha || exit
	
	# move Upgrade Exe onto Network
	if [ -d "/cygdrive/h/Martus/ClientExe" ]; then
		echo "Moving MartusSetupUpgrade onto network drive"
		cp $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe /cygdrive/h/Martus/ClientExe/ || exit
		cp $RELEASE_DIR/MartusSetupUpgrade-$CURRENT_VERSION-$BUILD_VERNUM_TAG.exe.sha /cygdrive/h/Martus/ClientExe/ || exit
	else
		echo "Beneserve2 not available. You must move the Client Upgrade Exe onto //Beneserve2/Engineering/Martus/ClientExe/ , its sha has already been checked into CVS"
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

createClientInstallers
updateCvsTreeWithBinaries

echo
echo "The build completed succesfully. The Release files are located in $RELEASE_DIR/ ."

# burn the cd image if required
if [ $burn_client_cd = 1 ]; then
	echo "Ready to burn image onto CD. Make sure a blank CD is in the CD burner, then press Enter to start:"
	read throw_away
	cdrecord dev=0,1,0 -v -eject -dao -data "$RELEASE_DIR/Martus-$BUILD_VERNUM_TAG.iso"
fi

cd "$INITIAL_DIR"

exit 0
