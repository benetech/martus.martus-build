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
require 'buildfile-martus-client-nsis-cd'
require 'buildfile-martus-client-iso'
require 'buildfile-martus-mspa-client-zip'

#TODO: Need to set up proper dependency chains
#TODO: Need to include MartusSetupLauncher?
#TODO: Need to upgrade to Java 6 runtime
#TODO: Need to create Multi-part NSIS installer
#TODO: Need to make sure all built artifacts are archived
#TODO: Need to use build numbers (from Hudson) [DONE??]
#TODO: Need to create amplifier tarball (build.xml#release)
#TODO: Maybe need to create 'clean' targets everywhere

#TODO: Would be nice to create friendly Mac installer
