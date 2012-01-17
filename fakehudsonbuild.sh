mkdir /home/kevins/work/cvs/martus/temp
cd /home/kevins/work/cvs/martus/temp
cvs -d :ext:cvs.benetech.org:/var/local/cvs checkout martus
cd martus
buildr clean checkout martus-client:package martus-thirdparty:package test=no
cd martus-client/target
jarsigner -keystore ~/keystore.jks -signed-jar martus-client-signed-1.jar martus-client-unsigned-1.jar SSMTSJAR
