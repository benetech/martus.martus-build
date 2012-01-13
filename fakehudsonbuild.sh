mkdir /home/kevins/work/cvs/martus/temp
cd /home/kevins/work/cvs/martus/temp
cvs -d :ext:cvs.benetech.org:/var/local/cvs checkout martus
cd martus
buildr  checkout martus-client-unsigned:package martus-thirdparty:package test=no
cd martus-client-unsigned/target
jarsigner -keystore ~/keystore.jks -signed-jar martus-client-signed-1.jar martus-client-unsigned-1.unsigned_jar SSMTSJAR
