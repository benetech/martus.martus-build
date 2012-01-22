mkdir /home/kevins/work/cvs/martus/temp
cd /home/kevins/work/cvs/martus/temp

cd martus
buildr clean martus-client-unsigned:package martus-thirdparty:package test=no
cd martus-client/target

#jarsigner -keystore ~/keystore.jks -signed-jar martus-client-signed-1.jar martus-client-unsigned-1.jar SSMTSJAR
