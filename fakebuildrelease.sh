export RELEASE_IDENTIFIER=pre-4.0
export INPUT_BUILD_NUMBER=TEST
export BUILD_NUMBER=NNN

# fake signing
pwd
cd martus-client/target
jarsigner -signed-jar /var/lib/hudson/martus-client/builds/TEST/martus-client-signed-TEST.jar martus-client-unsigned-TEST.jar SSMTSJAR


cd /home/kevins/temp/martus/
sh martus-build/buildrelease.sh
