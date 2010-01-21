def define_nsis(nsi_name, exe_name)
	package(:zip).include(project('martus-client').package(:sources), :path=>'BuildFiles')
	package(:zip).include(_('BuildFiles/*.txt'), :path=>'BuildFiles')

	#TODO: Need to include SourceFiles directory
	package(:zip).include(_('BuildFiles/Windows/Winsock95'), :path=>'BuildFiles/Win95')
	package(:zip).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
	#TODO: Need to include MartusWin32SetupLauncher?
	package(:zip).include(_('BuildFiles/ProgramFiles'), :path=>'BuildFiles')
	package(:zip).include(_('BuildFiles/SampleDir'), :path=>'BuildFiles')
	#TODO: Need to include MartusSetupLauncher?

	#TODO: Should we really include the MSPA zip?
	#package(:zip).include(_('martus-mspa/target/MartusMSPA.zip'), :path=>'BuildFiles/Jars')

	package_artifacts(package(:zip), [project('martus-bc-jce').package(:jar)], 'BuildFiles/Jars')
	package_artifacts(package(:zip), third_party_client_jars, 'BuildFiles/Jars')	
	package_artifacts(package(:zip), [_('BuildFiles/JavaRedistributables/Win32')], 'BuildFiles/Java redist/Win32')
	package_artifacts(package(:zip), [_('BuildFiles/Documents')], 'BuildFiles')
	package_artifacts(package(:zip), third_party_client_jar_licenses, 'BuildFiles/Documents/Licenses')
	package_artifacts(package(:zip), [artifact(INFINITEMONKEY_DLL_SPEC)], 'BuildFiles/ProgramFiles')
	package(:zip).include(project('martus-client').package(:jar), :path=>'BuildFiles/ProgramFiles', :as=>'martus.jar')

	package(:zip).include(_('BuildFiles/Windows/Win32_NSIS'))


	update_packaged_zip(package(:zip)) do | filespec |
		dest_dir = File.join(File.dirname(filespec), 'Installer')
		FileUtils.rm_rf dest_dir
		Dir.mkdir(dest_dir)
		unzip_file(filespec, dest_dir)
		
		error_output = `makensis -V2 #{_(:target, "/Installer/Win32_NSIS/#{nsi_name}")}`
		status = $?
		if status.exitstatus > 0
			raise "Error running makensis #{status.exitstatus}: #{error_output.split("\n").join("\n  ")}"
		end
		puts 'Finished makensis'
		mv _(:target, "Installer/Win32_NSIS/#{exe_name}"), _(:target, exe_name)
	end
end

