cd /home/kevins/work/cvs/martus/temp

export RELEASE_IDENTIFIER=pre-4.0
export INPUT_BUILD_NUMBER=TEST
echo INPUT_BUILD_NUMBER=$INPUT_BUILD_NUMBER
buildr -f martus/buildfile martus-client-linux-zip:package martus-client-mac-dmg:package test=no
