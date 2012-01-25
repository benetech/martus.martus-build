cd /home/kevins/work/cvs/martus/temp

cd martus
buildr -f martus/buildfile clean martus-client:package martus-thirdparty:package test=no

#old command
#buildr clean martus-client-unsigned:package martus-thirdparty:package test=no

# fake signing, when we are ready for that
#cd martus-client/target
#jarsigner -keystore ~/keystore.jks -signed-jar martus-client-signed-1.jar martus-client-unsigned-1.jar SSMTSJAR
