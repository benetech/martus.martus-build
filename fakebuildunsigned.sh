cd /home/kevins/temp/martus
pwd

buildr --trace -f martus-build/buildfile clean martus-client:build_unsigned test=no

# fake signing
pwd
cd martus-client/target
jarsigner -signed-jar /var/lib/hudson/martus-client/builds/TEST/martus-client-signed-TEST.jar martus-client-unsigned-TEST.jar SSMTSJAR
