sh $WORKSPACE/martus-build/checkout.sh
hg log -l 1 -R martus-common
buildr -f $WORKSPACE/martus-build/buildfile clean martus-client:build_unsigned test=no
