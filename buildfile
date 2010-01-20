repositories.remote << 'http://www.ibiblio.org/maven2/'
repositories.remote << 'http://ftp.cica.es/mirrors/maven2'
repositories.remote << 'http://download.java.net/maven/2'

$build_number = ENV['BUILD_NUMBER'] || 'TEST'

def build_spec(group, name, type, version)
	return "#{group}:#{name}:#{type}:#{version}"
end

def build_ant_spec(type)
	#TODO: should switch to org.apache.ant
	return build_spec('ant', 'ant', type, '1.6.2')
end

def build_ant_junit_spec(type)
	#TODO: should switch to org.apache.ant
	return build_spec('ant', 'ant-junit', type, '1.6.2')
end

def build_junit_spec(type)
	return build_spec('junit', 'junit', type, '3.8.2')
end

def build_xmlrpc_spec(type)
	return build_spec('xmlrpc', 'xmlrpc', type, '1.2-b1')
end

def build_icu4j_spec(type)
	return build_spec('com.ibm.icu', 'icu4j', type, '3.4.4')
end

def build_layouts_spec(type)
	return build_spec('com.jhlabs', 'layouts', type, '2006-08-10')
end

def build_velocity_spec(type)
	return build_spec('velocity', 'velocity', type, '1.4')
end

def build_velocity_dep_spec(type)
	return build_spec('velocity', 'velocity-dep', type, '1.4')
end

def build_jetty_spec(type)
	return build_spec('jetty', 'jetty', type, '4.2.27')
end

def build_javax_servlet_spec(type)
	return build_spec('jetty', 'javax.servlet', type, '5.1.12')
end

def build_lucene_spec(type)
	return build_spec('lucene', 'lucene', type, '1.3-rc1')
end

def build_persiancalendar_spec(type)
	return build_spec('com.ghasemkiani', 'persiancalendar', type, '2.1')
end

def build_bcprov_spec(type)
	return build_spec('bouncycastle', 'bcprov-jdk14', type, '135')
end

def build_mail_spec(type)
	return build_spec('javax.mail', 'mail', type, '1.4.3')
end

def build_infinitemonkey_spec(type)
	return build_spec('infinitemonkey', 'infinitemonkey', type, '1.0')
end

def build_rhino_spec(type)
	return build_spec('org.mozilla.rhino', 'js', type, '2006-03-08')
end


ANT_SPEC = build_ant_spec('jar')
ANT_JUNIT_SPEC = build_ant_junit_spec('jar')
JUNIT_SPEC = build_junit_spec('jar')
XMLRPC_SPEC = build_xmlrpc_spec('jar')
ICU4J_SPEC = build_icu4j_spec('jar')
LAYOUTS_SPEC = build_layouts_spec('jar')
VELOCITY_SPEC = build_velocity_spec('jar')
VELOCITY_DEP_SPEC = build_velocity_dep_spec('jar')
JETTY_SPEC = build_jetty_spec('jar')
JAVAX_SERVLET_SPEC = build_javax_servlet_spec('jar')
LUCENE_SPEC = build_lucene_spec('jar')
PERSIANCALENDAR_SPEC = build_persiancalendar_spec('jar')
BCPROV_SPEC = build_bcprov_spec('jar')
BCPROV_LICENSE_SPEC = build_bcprov_spec('license')
MAIL_SPEC = build_mail_spec('jar')
INFINITEMONKEY_JAR_SPEC = build_infinitemonkey_spec('jar')
INFINITEMONKEY_DLL_SPEC = build_infinitemonkey_spec('dll')
RHINO_SPEC = build_rhino_spec('jar')

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

def update_packaged_zip(package)
	package.enhance do | task |
		task.enhance do
			yield package.name
		end
	end
end

def unzip_file (file, destination)
	Zip::ZipFile.open(file) do |zip_file|
		zip_file.each do |f|
			f_path=File.join(destination, f.name)
			FileUtils.mkdir_p(File.dirname(f_path))
			if File.exist?(f_path) && !File.directory?(f_path)
				raise "Can't overwrite #{f_path}"
			end
			zip_file.extract(f, f_path) 
		end
	end
end

def third_party_client_jars
	jars = []
	jars << artifact(RHINO_SPEC)
	jars << artifact(LAYOUTS_SPEC)
	jars << artifact(BCPROV_SPEC)
	jars << artifact(JUNIT_SPEC)
	jars << artifact(ICU4J_SPEC)
	jars << artifact(PERSIANCALENDAR_SPEC)
	jars << artifact(VELOCITY_SPEC)
	jars << artifact(VELOCITY_DEP_SPEC)
	jars << artifact(INFINITEMONKEY_JAR_SPEC)
	jars << artifact(XMLRPC_SPEC)
	return jars
end

def package_artifacts(target, artifacts, path)
	artifacts.each do | artifact |
		target.include(artifact, :path=>path)
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
#TODO: Need to "clean up" (unix2dos) all the txt files
#TODO: Need to use build numbers (from Hudson) [DONE??]
#TODO: Need to create amplifier tarball (build.xml#release)
#TODO: Maybe need to create 'clean' targets everywhere

#TODO: Would be nice to create friendly Mac installer
