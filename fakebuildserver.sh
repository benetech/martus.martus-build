export WORKSPACE=/home/kevins/temp/
cd $WORKSPACE/martus
pwd

buildr --trace -f martus-build/buildfile clean martus-server:everything test=no
