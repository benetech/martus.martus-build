def create_nsis_zip_task
	zip_file = _(:target, 'nsis.zip')

	nsis_zip = zip(zip_file)
	zip(zip_file).include(project('martus-client').package(:sources), :path=>'BuildFiles')
	zip(zip_file).include(_('BuildFiles/*.txt'), :path=>'BuildFiles')

	#TODO: Need to include SourceFiles directory
	zip(zip_file).include(_('BuildFiles/Windows/Winsock95'), :path=>'BuildFiles/Win95')
	zip(zip_file).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
	zip(zip_file).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
	zip(zip_file).include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
	#TODO: Need to include MartusWin32SetupLauncher?
	zip(zip_file).include(_('BuildFiles/ProgramFiles'), :path=>'BuildFiles')
	zip(zip_file).include(_('BuildFiles/SampleDir'), :path=>'BuildFiles')
	#TODO: Need to include MartusSetupLauncher?

	include_artifacts(zip(zip_file), [project('martus-bc-jce').package(:jar)], 'BuildFiles/Jars')
	include_artifacts(zip(zip_file), third_party_client_jars, 'BuildFiles/Jars')	
	include_artifacts(zip(zip_file), [_('BuildFiles/JavaRedistributables/Win32')], 'BuildFiles/Java redist/Win32')
	include_artifacts(zip(zip_file), [_('BuildFiles/Documents')], 'BuildFiles')
	include_artifacts(zip(zip_file), third_party_client_jar_licenses, 'BuildFiles/Documents/Licenses')
	include_artifacts(zip(zip_file), [artifact(INFINITEMONKEY_DLL_SPEC)], 'BuildFiles/ProgramFiles')
	zip(zip_file).include(project('martus-client').package(:jar), :path=>'BuildFiles/ProgramFiles', :as=>'martus.jar')

	zip(zip_file).include(_('BuildFiles/Windows/Win32_NSIS'))

	return zip_file
end

def run_nsis_task(nsis_zip, nsi_name, exe_name)
		dest_dir = _(:target, 'Installer')
		FileUtils.rm_rf dest_dir
		Dir.mkdir(dest_dir)
		unzip_file(nsis_zip, dest_dir)
		
		error_output = `makensis -V2 #{_(:target, "/Installer/Win32_NSIS/#{nsi_name}")}`
		status = $?
		if status.exitstatus > 0
			raise "Error running makensis #{status.exitstatus}: #{error_output.split("\n").join("\n  ")}"
		end
		puts 'Finished makensis'
		mv _(:target, "Installer/Win32_NSIS/#{exe_name}"), _(:target, exe_name)

end
