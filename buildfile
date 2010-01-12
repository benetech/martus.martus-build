repositories.remote << 'http://www.ibiblio.org/maven2/'
repositories.remote << 'http://ftp.cica.es/mirrors/maven2'
repositories.remote << 'http://download.java.net/maven/2'

$build_number = ENV['BUILD_NUMBER'] || 'TEST'

JUNIT_SPEC = 'junit:junit:jar:3.8.2'
XMLRPC_SPEC = 'xmlrpc:xmlrpc:jar:1.2-b1'
ICU4J_SPEC = 'com.ibm.icu:icu4j:jar:3.4.4'
LAYOUTS_SPEC = 'com.jhlabs:layouts:jar:2006-08-10'
VELOCITY_SPEC = 'velocity:velocity:jar:1.4'
VELOCITY_DEP_SPEC = 'velocity:velocity-dep:jar:1.4'
JETTY_SPEC = 'jetty:jetty:jar:4.2.27'
JAVAX_SERVLET_SPEC = 'jetty:javax.servlet:jar:5.1.12'
LUCENE_SPEC = 'lucene:lucene:jar:1.3-rc1'
PERSIANCALENDAR_SPEC = 'com.ghasemkiani:persiancalendar:jar:2.1'
BCPROV_SPEC = 'bouncycastle:bcprov-jdk14:jar:135'
MAIL_SPEC = 'javax.mail:mail:jar:1.4.3'
INFINITEMONKEY_JAR_SPEC = 'infinitemonkey:infinitemonkey:jar:1.0'
INFINITEMONKEY_DLL_SPEC = 'infinitemonkey:infinitemonkey:dll:1.0'

def create_layout_with_source_as_source(base)
	layout = Layout.new
	layout[:root] = "#{base}"
	layout[:source, :main, :java] = "#{base}/source"
	layout[:source, :test, :java] = "#{base}/source"
	layout[:target] = "#{base}/target"
	layout[:target, :main, :classes] = "#{base}/target/main/classes"
	layout[:target, :test, :classes] = "#{base}/target/test/classes"
	return layout
end

def cvs_checkout(project)
	if !system("cvs -d:extssh:kevins@cvs.benetech.org/var/local/cvs co #{project}")
		raise "Unable to check out #{project}"
	end
	if $? != 0
		raise "Error checking out #{project}"
	end
end

task nil do
end

task :checkout do
	cvs_checkout 'martus-thirdparty'
	cvs_checkout 'martus-bc-jce'
	cvs_checkout 'martus-logi'
	cvs_checkout 'martus-hrdag'
	cvs_checkout 'martus-utils'
	cvs_checkout 'martus-swing'
	cvs_checkout 'martus-common'

	cvs_checkout 'martus-js-xml-generator'
	cvs_checkout 'martus-jar-verifier'
	cvs_checkout 'martus-clientside'
	cvs_checkout 'martus-client'
	cvs_checkout 'martus-mspa'

	cvs_checkout 'martus-amplifier'
	cvs_checkout 'martus-server'

	cvs_checkout 'martus-meta'
end

define 'martus' do
	compile.with [
		project('martus-common'),
	]
end
	

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

#TODO: Need to use build numbers (from Hudson)
#TODO: Need to create Javaless NSIS installer
#TODO: Need to upgrade to Java 6 runtime
#TODO: Need to create NSIS installer
#TODO: Need to create ISO image
#TODO: Need to create Multi-part NSIS installer
#TODO: Need to create source zips
#TODO: Need to add third-party licenses
#TODO: Need to make sure all built artifacts are archived
#TODO: Need to "clean up" (unix2dos) all the txt files

#TODO: Would be nice to create friendly Mac installer
