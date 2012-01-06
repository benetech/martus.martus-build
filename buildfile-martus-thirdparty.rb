name = "martus-thirdparty"

def jar_file(project_name, directory, jar_name)
	return file(_(project_name, "#{directory}/bin/#{jar_name}"))
end

def license_file(project_name, directory, license_name)
	return file(_(project_name, "#{directory}/license/#{license_name}"))
end

def source_file(project_name, directory, source_name)
	return file(_(project_name, "#{directory}/source/#{source_name}"))
end

define name, :layout=>create_layout_with_source_as_source(name) do
  project.group = 'org.martus'
  project.version = '1'

	install do
		puts "Installing martus-thirdparty"
	end

	#libext
	install artifact(BCPROV_SPEC).from(jar_file(name, 'libext/BouncyCastle', 'bcprov-jdk14-135.jar'))
	install artifact(BCPROV_SOURCE_SPEC).from(source_file(name, 'libext/BouncyCastle', 'bcprov-jdk14-135.zip'))
	install artifact(BCPROV_LICENSE_SPEC).from(license_file(name, 'libext/BouncyCastle', 'LICENSE.html'))
	install artifact(JUNIT_SOURCE_SPEC).from(source_file(name, 'libext/JUnit', 'junit3.8.1.zip'))
  install artifact(BCJCE_SPEC).from(jar_file(name, 'libext/bc-jce', 'bc-jce-2012-01-05.jar'))
  install artifact(BCJCE_LICENSE_SPEC).from(license_file(name, 'libext/bc-jce', 'LICENSE.html'))

	#common
	install artifact(INFINITEMONKEY_JAR_SPEC).from(jar_file(name, 'common/InfiniteMonkey', 'InfiniteMonkey.jar'))
	install artifact(INFINITEMONKEY_DLL_SPEC).from(jar_file(name, 'common/InfiniteMonkey', 'infinitemonkey.dll'))
	install artifact(INFINITEMONKEY_SOURCE_SPEC).from(source_file(name, 'common/InfiniteMonkey', 'InfiniteMonkey.zip'))
	install artifact(INFINITEMONKEY_LICENSE_SPEC).from(license_file(name, 'common/InfiniteMonkey', 'license.txt'))
	install artifact(PERSIANCALENDAR_SPEC).from(jar_file(name, 'common/PersianCalendar', 'persiancalendar.jar'))
	install artifact(PERSIANCALENDAR_SOURCE_SPEC).from(source_file(name, 'common/PersianCalendar', 'PersianCalendar_2_1.zip'))
	install artifact(PERSIANCALENDAR_LICENSE_SPEC).from(license_file(name, 'common/PersianCalendar', 'gpl.txt'))
	install artifact(LOGI_LICENSE_SPEC).from(license_file(name, 'common/Logi', 'license.html'))

	install artifact(VELOCITY_LICENSE_SPEC).from(license_file(name, 'common/Velocity', 'LICENSE.txt'))
	install artifact(VELOCITY_SOURCE_SPEC).from(source_file(name, 'common/Velocity', 'velocity-1.4-rc1.zip'))
	install artifact(VELOCITY_DEP_LICENSE_SPEC).from(license_file(name, 'common/Velocity', 'LICENSE.txt'))
# TODO: Find velocity-dep source code
#	install artifact(VELOCITY_DEP_SOURCE_SPEC).from(source_file(name, 'common/Velocity', ''))
	install artifact(XMLRPC_SOURCE_SPEC).from(source_file(name, 'common/XMLRPC', 'xmlrpc-1.2-b1-src.zip'))
	install artifact(XMLRPC_LICENSE_SPEC).from(license_file(name, 'common/XMLRPC', 'LICENSE.txt'))
# TODO: Find ICU4J source code
#	install artifact(ICU4J_SOURCE_SPEC).from(source_file(name, 'common/PersianCalendar', 'icu4j_3_2_license.html'))
	install artifact(ICU4J_LICENSE_SPEC).from(license_file(name, 'common/PersianCalendar', 'icu4j_3_2_license.html'))

	#client
	install artifact(LAYOUTS_SPEC).from(jar_file(name, 'client/jhlabs', 'layouts.jar'))
	install artifact(LAYOUTS_SOURCE_SPEC).from(source_file(name, 'client/jhlabs', 'layouts.zip'))
	install artifact(LAYOUTS_LICENSE_SPEC).from(license_file(name, 'client/jhlabs', 'LICENSE.TXT'))
	install artifact(RHINO_SPEC).from(jar_file(name, 'client/RhinoJavaScript', 'js.jar'))
	install artifact(RHINO_SOURCE_SPEC).from(source_file(name, 'client/RhinoJavaScript', 'Rhino-src.zip'))
	install artifact(RHINO_LICENSE_SPEC).from(license_file(name, 'client/RhinoJavaScript', 'license.txt'))
	#NOTE: Would like to include license for khmer fonts, but there are no license files
	#NOTE: Would like to include license for NSIS installer, but don't see any
	#TODO: Need to include client license files for Sun Java (after upgrading to Java 6)

	#server
	install artifact(JETTY_SOURCE_SPEC).from(source_file(name, 'server/Jetty', 'jetty-4.2.24-all.tar.gz'))
	license_task = extract_artifact_entry_task(JETTY_SPEC, 'org/mortbay/LICENSE.html')
	install artifact(JETTY_LICENSE_SPEC).from(license_task)
	install artifact(LUCENE_SOURCE_SPEC).from(source_file(name, 'server/Lucene', 'lucene-1.3-rc1-src.zip'))
	license_task = extract_artifact_entry_task(LUCENE_SOURCE_SPEC, 'lucene-1.3-rc1-src/LICENSE.txt')
	install artifact(LUCENE_LICENSE_SPEC).from(license_task)
	# TODO: Should include source/license for javax.servlet.jar
	# TODO: Should include source/license for javax.mail.jar
	
  package(:zip).include(artifact(JUNIT_SPEC))
	package(:zip).include(artifact(BCPROV_SPEC))
  package(:zip).include(artifact(BCJCE_SPEC))
  package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC))
  package(:zip).include(artifact(INFINITEMONKEY_DLL_SPEC))
  package(:zip).include(artifact(PERSIANCALENDAR_SPEC))
  package(:zip).include(artifact(VELOCITY_SPEC))
  package(:zip).include(artifact(VELOCITY_DEP_SPEC))
  package(:zip).include(artifact(XMLRPC_SPEC))
  package(:zip).include(artifact(ICU4J_SPEC))
  package(:zip).include(artifact(LAYOUTS_SPEC))
  package(:zip).include(artifact(RHINO_SPEC))
end
