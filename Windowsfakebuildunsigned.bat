cd D:\temp\martus
call buildr --trace -f martus-build/buildfile clean martus-client:build_unsigned test=no

cd D:\temp\martus\martus-client\target
jarsigner -signed-jar D:/temp/martus/martus-client/builds/TEST/martus-client-signed-TEST.jar martus-client-unsigned-TEST.jar SSMTSJAR
pause
pause