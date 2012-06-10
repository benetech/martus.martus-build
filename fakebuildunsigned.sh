cd /home/kevins/work/hg/martus/martus/temp

buildr --trace -f martus-build/buildfile clean martus-client:build_unsigned test=no

# fake signing, when we are ready for that
cd martus-client/target
jarsigner -keystore ~/keystore.jks -signed-jar /var/lib/hudson/martus-client/builds/TEST/martus-client-signed-TEST.jar martus-client-unsigned-TEST.jar SSMTSJAR
