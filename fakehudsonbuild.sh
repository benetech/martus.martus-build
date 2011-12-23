mkdir /home/kevins/work/cvs/martus/temp
cd /home/kevins/work/cvs/martus/temp
cvs -d :ext:cvs.benetech.org:/var/local/cvs checkout martus
cd martus
buildr checkout build martus-client:package test=no
