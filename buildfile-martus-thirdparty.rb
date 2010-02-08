def jar_file(project_name, directory, jar_name)
	return file(_(project_name, "#{directory}/bin/#{jar_name}"))
end

def license_file(project_name, directory, license_name)
	return file(_(project_name, "#{directory}/license/#{license_name}"))
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
	#TODO: Need to include common license files for Logi

	#client
	install artifact(LAYOUTS_SPEC).from(jar_file(name, 'client/jhlabs', 'layouts.jar'))
	install artifact(LAYOUTS_LICENSE_SPEC).from(license_file(name, 'client/jhlabs', 'LICENSE.TXT'))
	install artifact(RHINO_SPEC).from(jar_file(name, 'client/RhinoJavaScript', 'js.jar'))
	install artifact(RHINO_LICENSE_SPEC).from(license_file(name, 'client/RhinoJavaScript', 'license.txt'))
	#TODO: Need to include client license files for fonts, installer, Sun Java

	#server
	#TODO: Need to include server license files for Jetty, Lucene, Sun Java
	
end
