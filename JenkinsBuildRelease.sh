export RELEASE_IDENTIFIER=4.5.0
export NSIS_HOME="$HOME/.wine/drive_c/Program Files/NSIS/"
export ATTIC_DIR="/var/lib/hudson/martus-client/builds/$INPUT_BUILD_NUMBER/"
# INPUT_BUILD_NUMBER is set interactively (e.g. 3011)
sh $WORKSPACE/martus-build/checkout.sh
sh $WORKSPACE/martus-build/buildrelease.sh
