name = 'martus-client-nsis-single'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	package(:zip).include(project('martus-client').package(:sources), :path=>'BuildFiles')
	package(:zip).include(_('BuildFiles/*.txt'), :path=>'BuildFiles')

	#TODO: Need to include SourceFiles directory
	package(:zip).include(_('BuildFiles/Windows/Winsock95'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
	#TODO: Need to include MartusWin32SetupLauncher?
	package(:zip).include(_('BuildFiles/ProgramFiles'), :path=>'BuildFiles')
	package(:zip).include(_('BuildFiles/SampleDir'), :path=>'BuildFiles')
	#TODO: Need to include MartusSetupLauncher?

	#TODO: Should we really include the MSPA zip?
	package(:zip).include(_('martus-mspa/target/MartusMSPA.zip'), :path=>'BuildFiles/Jars')
	
	package(:zip).include(project('martus-bc-jce').package(:jar), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(RHINO_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(LAYOUTS_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(BCPROV_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(JUNIT_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(ICU4J_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(PERSIANCALENDAR_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(VELOCITY_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(VELOCITY_DEP_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(artifact(XMLRPC_SPEC), :path=>'BuildFiles/Jars')
	package(:zip).include(_('BuildFiles/JavaRedistributables/Win32'), :path=>'BuildFiles/Java redist/Win32')
	package(:zip).include(_('BuildFiles/Documents'), :path=>'BuildFiles')

	# Need to include ALL of the Documents/Licenses, not just one
	package(:zip).include(artifact(BCPROV_LICENSE_SPEC), :path=>'BuildFiles/Documents/Licenses')

	package(:zip).include(artifact(INFINITEMONKEY_DLL_SPEC), :path=>'BuildFiles/ProgramFiles')
	package(:zip).include(project('martus-client').package(:jar), :path=>'BuildFiles/ProgramFiles', :as=>'martus.jar')

	package(:zip).include(_('BuildFiles/Windows/Win32_NSIS'))


	update_packaged_zip(package(:zip)) do | filespec |
		dest_dir = File.join(File.dirname(filespec), 'Installer')
		Dir.mkdir(dest_dir)
		unzip_file(filespec, dest_dir)
		
		error_output = `makensis -V2 #{_(:target, '/Installer/Win32_NSIS/NSIS_Martus_Single.nsi')}`
		status = $?
		if status.exitstatus > 0
			raise "Error running makensis #{status.exitstatus}: #{error_output.split("\n").join("\n  ")}"
		end
		puts 'Finished makensis'
		mv _(:target, 'Installer/Win32_NSIS/MartusSetupSingle.exe'), _(:target, 'MartusSetupSingle.exe')
	end
end

