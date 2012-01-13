mkdir /home/kevins/work/cvs/martus/temp
cd /home/kevins/work/cvs/martus/temp
cvs -d :ext:cvs.benetech.org:/var/local/cvs checkout martus
cd martus
buildr clean checkout martus-client-unsigned:package martus-thirdparty:package test=no
