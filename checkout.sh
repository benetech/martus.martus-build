#cvs -d :ext:cvs.benetech.org:/var/local/cvs checkout martus
cd $WORKSPACE
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-amplifier
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-bc-jce
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-client
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-clientside
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-common
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-docs
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-hrdag
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-jar-verifier
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-js-xml-generator
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-logi
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-meta
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-mspa
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-server
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-swing
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-thirdparty
hg clone --rev Branch_Server_4.4 ssh://mvcs/martus/martus-utils
hg clone ssh://mvcs/martus-sha
