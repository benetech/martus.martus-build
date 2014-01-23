export WORKSPACE=/home/kevins/temp/
cd $WORKSPACE/martus
pwd

buildr --trace -f martus-build/buildfile clean martus-server:everything test=no
echo "Ignore the RuntimeError : Error adding new sha files to hg"
echo "--we intentionally only do that on the real server"