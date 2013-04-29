echo INPUT_BUILD_NUMBER=$INPUT_BUILD_NUMBER
buildr --trace -f martus-build/buildfile test=no \
clean \
martus-bc-jce:jar-with-shas 
