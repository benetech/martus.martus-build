require 'fileutils'

require 'buildfile-martus'

require 'buildfile-martus-thirdparty'
require 'buildfile-martus-bc-jce'
require 'buildfile-martus-logi'
require 'buildfile-martus-hrdag'
require 'buildfile-martus-utils'
require 'buildfile-martus-swing'
require 'buildfile-martus-common'
require 'buildfile-martus-js-xml-generator'
require 'buildfile-martus-jar-verifier'
require 'buildfile-martus-clientside'
require 'buildfile-martus-mlp'
require 'buildfile-martus-client'
require 'buildfile-martus-mspa'
require 'buildfile-martus-amplifier'
require 'buildfile-martus-server'
require 'buildfile-martus-meta'

require 'buildfile-martus-client-linux-zip'
require 'buildfile-martus-client-nsis-upgrade'
require 'buildfile-martus-client-nsis-single'
require 'buildfile-martus-client-nsis-pieces'
require 'buildfile-martus-client-nsis-cd'
require 'buildfile-martus-client-iso'
require 'buildfile-martus-mspa-client-zip'

#TODO: Need to set up proper dependency chains
#TODO: Need to upgrade to Java 6 runtime
#TODO: Need to make sure all built artifacts are archived
#TODO: Need to use build numbers (from Hudson) [DONE??]

#TODO: Would be nice to create friendly Mac installer

#NOTE: Old script created amplifier tarball (build.xml#release) but
# Scott confirms it is not needed/used
