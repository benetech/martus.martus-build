export ATTIC_DIR="/var/lib/jenkins/martus-client/builds/$INPUT_BUILD_NUMBER/"

cd /home/kevins/temp/martus
pwd

buildr --trace -f martus-build/buildfile clean martus-client:build_unsigned test=no
