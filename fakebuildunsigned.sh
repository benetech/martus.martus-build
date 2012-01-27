cd /home/kevins/work/cvs/martus/temp

buildr -f martus/buildfile clean martus-client:package martus-thirdparty:package test=no

# fake signing, when we are ready for that
cd martus-client/target
jarsigner -keystore ~/keystore.jks -signed-jar /var/lib/hudson/input/martus-client-signed-TEST.jar martus-client-unsigned-TEST.jar SSMTSJAR
