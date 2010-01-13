name = 'martus-client-nsis-upgrade'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	package(:zip).include(artifact(ICU4J_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(INFINITEMONKEY_DLL_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(BCPROV_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(PERSIANCALENDAR_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(XMLRPC_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(JUNIT_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(VELOCITY_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(VELOCITY_DEP_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(LAYOUTS_SPEC), :path=>'LibExt')
	package(:zip).include(project('martus-client').package(:jar))
	#TODO: need to zip up everything else that goes into the nsis upgrade installer

	update_packaged_zip(package(:zip)) do | filespec |
		dest_dir = File.join(File.dirname(filespec), 'temp')
		Dir.mkdir(dest_dir)
		unzip_file(filespec, dest_dir)
		#TODO: Need to run makensis in that directory
	end
end

