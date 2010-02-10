def jar_file(project_name, directory, jar_name)
	return file(_(project_name, "#{directory}/bin/#{jar_name}"))
end

def license_file(project_name, directory, license_name)
	return file(_(project_name, "#{directory}/license/#{license_name}"))
end

def source_file(project_name, directory, source_name)
	return file(_(project_name, "#{directory}/source/#{source_name}"))
end

define "martus-thirdparty" do
	install do
		puts "Installing martus-thirdparty"
	end

	#libext
	install artifact(BCPROV_SPEC).from(jar_file(name, 'libext/BouncyCastle', 'bcprov-jdk14-135.jar'))
	install artifact(BCPROV_LICENSE_SPEC).from(license_file(name, 'libext/BouncyCastle', 'LICENSE.html'))

	install artifact(JUNIT_LICENSE_SPEC).from(license_file(name, 'libext/JUnit', 'cpl-v10.html'))

	#common
	install artifact(INFINITEMONKEY_JAR_SPEC).from(jar_file(name, 'common/InfiniteMonkey', 'InfiniteMonkey.jar'))
	install artifact(INFINITEMONKEY_DLL_SPEC).from(jar_file(name, 'common/InfiniteMonkey', 'infinitemonkey.dll'))
	install artifact(INFINITEMONKEY_LICENSE_SPEC).from(license_file(name, 'common/InfiniteMonkey', 'license.txt'))
	install artifact(PERSIANCALENDAR_SPEC).from(jar_file(name, 'common/PersianCalendar', 'persiancalendar.jar'))
	install artifact(PERSIANCALENDAR_LICENSE_SPEC).from(license_file(name, 'common/PersianCalendar', 'gpl.txt'))

	install artifact(VELOCITY_LICENSE_SPEC).from(license_file(name, 'common/Velocity', 'LICENSE.txt'))
	install artifact(VELOCITY_DEP_LICENSE_SPEC).from(license_file(name, 'common/Velocity', 'LICENSE.txt'))
	install artifact(XMLRPC_LICENSE_SPEC).from(license_file(name, 'common/XMLRPC', 'LICENSE.txt'))
	install artifact(ICU4J_LICENSE_SPEC).from(license_file(name, 'common/PersianCalendar', 'icu4j_3_2_license.html'))
	install artifact(LOGI_LICENSE_SPEC).from(license_file(name, 'common/Logi', 'license.html'))

	#client
	install artifact(LAYOUTS_SPEC).from(jar_file(name, 'client/jhlabs', 'layouts.jar'))
	install artifact(LAYOUTS_LICENSE_SPEC).from(license_file(name, 'client/jhlabs', 'LICENSE.TXT'))
	install artifact(RHINO_SPEC).from(jar_file(name, 'client/RhinoJavaScript', 'js.jar'))
	install artifact(RHINO_LICENSE_SPEC).from(license_file(name, 'client/RhinoJavaScript', 'license.txt'))
	#NOTE: Would like to include license for khmer fonts, but there are no license files
	#NOTE: Would like to include license for NSIS installer, but don't see any
	#TODO: Need to include client license files for Sun Java (after upgrading to Java 6)

	#server
	build do
		target_dir = _('target', 'jetty')
		license_name = 'LICENSE.html'
		unzip(target_dir=>artifact(JETTY_SPEC)).include("**/#{license_name}")
		license_file = File.join(target_dir, license_name)
		install artifact(JETTY_LICENSE_SPEC).from(license_file)
		FileUtils::rm_rf target_dir
	end

	build do
		target_dir = _('target', 'lucene')
		license_name = 'LICENSE.txt'
		unzip(target_dir=>source_file(name, 'server/Lucene', 'lucene-1.3-rc1-src.zip')).include("**/#{license_name}")
		license_file = File.join(target_dir, license_name)
		install artifact(JETTY_LICENSE_SPEC).from(license_file)
		FileUtils::rm_rf target_dir
	end

end
