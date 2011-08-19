repositories.remote << 'http://www.ibiblio.org/maven2/'
repositories.remote << 'http://repo1.maven.org/maven2/'
repositories.remote << 'http://download.java.net/maven/2'

$build_number = ENV['BUILD_NUMBER'] || 'TEST'

$client_version = 'pre-3.4'

def build_spec(group, name, type, version)
	return "#{group}:#{name}:#{type}:#{version}"
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

def build_logi_spec(type)
	return build_spec('org.logi', 'logi', type, '1.1.2')
end

# LibExt, not in public repository
BCPROV_SPEC = build_bcprov_spec('jar')
BCPROV_SOURCE_SPEC = build_bcprov_spec('sources')
BCPROV_LICENSE_SPEC = build_bcprov_spec('license')
JUNIT_SPEC = build_junit_spec('jar')
JUNIT_SOURCE_SPEC = build_junit_spec('sources')
JUNIT_LICENSE_SPEC = build_junit_spec('license')

# Common, not in public repository
INFINITEMONKEY_JAR_SPEC = build_infinitemonkey_spec('jar')
INFINITEMONKEY_DLL_SPEC = build_infinitemonkey_spec('dll')
INFINITEMONKEY_SOURCE_SPEC = build_infinitemonkey_spec('sources')
INFINITEMONKEY_LICENSE_SPEC = build_infinitemonkey_spec('license')
PERSIANCALENDAR_SPEC = build_persiancalendar_spec('jar')
PERSIANCALENDAR_SOURCE_SPEC = build_persiancalendar_spec('sources')
PERSIANCALENDAR_LICENSE_SPEC = build_persiancalendar_spec('license')
LOGI_LICENSE_SPEC = build_logi_spec('license')

# Common, from public repository
VELOCITY_SPEC = build_velocity_spec('jar')
VELOCITY_SOURCE_SPEC = build_velocity_spec('sources')
VELOCITY_LICENSE_SPEC = build_velocity_spec('license')
VELOCITY_DEP_SPEC = build_velocity_dep_spec('jar')
VELOCITY_DEP_SOURCE_SPEC = build_velocity_dep_spec('sources')
VELOCITY_DEP_LICENSE_SPEC = build_velocity_dep_spec('license')
XMLRPC_SPEC = build_xmlrpc_spec('jar')
XMLRPC_SOURCE_SPEC = build_xmlrpc_spec('sources')
XMLRPC_LICENSE_SPEC = build_xmlrpc_spec('license')
ICU4J_SPEC = build_icu4j_spec('jar')
ICU4J_SOURCE_SPEC = build_icu4j_spec('sources')
ICU4J_LICENSE_SPEC = build_icu4j_spec('license')

# Client, not in public repository
LAYOUTS_SPEC = build_layouts_spec('jar')
LAYOUTS_SOURCE_SPEC = build_layouts_spec('sources')
LAYOUTS_LICENSE_SPEC = build_layouts_spec('license')
RHINO_SPEC = build_rhino_spec('jar')
RHINO_SOURCE_SPEC = build_rhino_spec('sources')
RHINO_LICENSE_SPEC = build_rhino_spec('license')

# Server, from public repository
JETTY_SPEC = build_jetty_spec('jar')
JETTY_SOURCE_SPEC = build_jetty_spec('sources')
JETTY_LICENSE_SPEC = build_jetty_spec('license')
JAVAX_SERVLET_SPEC = build_javax_servlet_spec('jar')
JAVAX_SERVLET_LICENSE_SPEC = build_javax_servlet_spec('license')
LUCENE_SPEC = build_lucene_spec('jar')
LUCENE_SOURCE_SPEC = build_lucene_spec('sources')
LUCENE_LICENSE_SPEC = build_lucene_spec('license')
MAIL_SPEC = build_mail_spec('jar')
MAIL_LICENSE_SPEC = build_mail_spec('license')

MARTUSSETUP_EXE_SPEC = build_spec('org.martus', 'martus_setup', 'exe', $client_version)

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
	cmd = "cvs -d:ext:cvs.benetech.org/var/local/cvs co #{project}"
	IO.popen("#{cmd} 2>&1") do |pipe|
		out_err = ''
		while((line = pipe.gets))
			puts line
			out_err << line
		end
		if(pipe.closed? || pipe.eof?)
			break
		end
	end
	if $? != 0
		raise "Error checking out #{project} (#{$?}):#{outerr}"
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


def extract_artifact_entry_task(artifact_spec, entry)
	return extract_zip_entry_task(artifact(artifact_spec).to_s, entry)
end

def extract_zip_entry_task(zip_file, entry)
	target_dir = _('target', 'temp')
	license_file = File.join(target_dir, entry)
	unzip_task = unzip(target_dir=>zip_file).include(entry)
	return file license_file=>unzip_task
end

def sha(file_to_digest, sha_file)
	`sha1sum #{file_to_digest} > #{sha_file}`
	if $? != 0
		raise "Error generating SHA of #{file_to_digest}"
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

def third_party_client_licenses
	licenses = []
	licenses << artifact(BCPROV_LICENSE_SPEC)
	licenses << artifact(JUNIT_LICENSE_SPEC)
	licenses << artifact(INFINITEMONKEY_LICENSE_SPEC)
	licenses << artifact(PERSIANCALENDAR_LICENSE_SPEC)
	licenses << artifact(LOGI_LICENSE_SPEC)
	licenses << artifact(VELOCITY_LICENSE_SPEC)
	licenses << artifact(VELOCITY_DEP_LICENSE_SPEC)
	licenses << artifact(XMLRPC_LICENSE_SPEC)
	licenses << artifact(ICU4J_LICENSE_SPEC)
	licenses << artifact(LAYOUTS_LICENSE_SPEC)
	licenses << artifact(RHINO_LICENSE_SPEC)
	return licenses
end

def third_party_client_source
	licenses = []
	licenses << artifact(BCPROV_SOURCE_SPEC)
	licenses << artifact(JUNIT_SOURCE_SPEC)
	licenses << artifact(INFINITEMONKEY_SOURCE_SPEC)
	licenses << artifact(PERSIANCALENDAR_SOURCE_SPEC)
	licenses << artifact(VELOCITY_SOURCE_SPEC)
# TODO: Find velocity-dep source code
#	licenses << artifact(VELOCITY_DEP_SOURCE_SPEC)
	licenses << artifact(XMLRPC_SOURCE_SPEC)
# TODO: Find ICU4J source code
#	licenses << artifact(ICU4J_SOURCE_SPEC)
	licenses << artifact(LAYOUTS_SOURCE_SPEC)
	licenses << artifact(RHINO_SOURCE_SPEC)
	return licenses
end

def include_artifacts(target, artifacts, path)
	artifacts.each do | artifact |
		target.include(artifact, :path=>path)
	end
end

def fix_newlines(files)
	Dir.glob(files).each do | file |
		`unix2dos #{file}`
	end
end

def create_combined_license
	martus_license = File.readlines(_('BuildFiles', 'Documents', 'license.txt'))
	gpl = File.readlines(_('BuildFiles', 'Documents', 'gpl.txt'))
	File.open(_('BuildFiles', 'combined-license.txt'), "w") do | out |
		out.write(martus_license)
		out.write("\n\n\t**********************************\n\n")
		out.write(gpl)
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
	build do
		create_combined_license
	
		fix_newlines(_('BuildFiles', '*.txt'))
		fix_newlines(_('BuildFiles', 'Documents', '*.txt'))
		fix_newlines(_('BuildFiles', 'Windows', 'Winsock95', '*.txt'))
		fix_newlines(project('martus-jar-verifier').path_to('*.txt'))
	end

	#TODO: Set up a task that depends on: client exe, client iso+sha,
	# client chunks, client mac dmg, client linux zip, mlp files, 
	# server jar, mspa zip, and any other products
	# MAYBE have 'unsigned' and 'signed' tasks
	#task 'foo' => [project('martus-utils').package(:jar), project('martus-swing').package(:sources)]
end
	
