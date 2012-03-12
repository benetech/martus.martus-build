cd /home/kevins/work/cvs/martus/temp

export RELEASE_IDENTIFIER=pre-4.0
export INPUT_BUILD_NUMBER=TEST
echo INPUT_BUILD_NUMBER=$INPUT_BUILD_NUMBER
buildr -f martus/buildfile martus-client-linux-zip:package martus-client-mac-dmg:package martus-client-linux-zip:sha1 martus-client-linux-zip:sha2 martus-client-nsis-single:build martus-client-nsis-upgrade:build test=no
