def create_nsis_zip_task
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']

  zip_file = _('temp', 'nsis.zip')

	nsis_zip = zip(zip_file)

  attic_dir = "/var/lib/hudson/martus-client/builds/#{input_build_number}/"
  source_zip_name = "martus-client-sources-#{input_build_number}.zip"
	zip(zip_file).include("#{attic_dir}/#{source_zip_name}", :path=>'BuildFiles')
	zip(zip_file).include(_('martus', 'BuildFiles', '*.txt'), :path=>'BuildFiles')

	include_artifacts(zip(zip_file), third_party_client_source, 'SourceFiles')	
	
	zip(zip_file).include(_('martus', 'BuildFiles', 'Windows', 'Winsock95'), :path=>'BuildFiles/Win95')
	zip(zip_file).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
	zip(zip_file).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
	zip(zip_file).include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
	#TODO: Need to include MartusWin32SetupLauncher?
	zip(zip_file).include(_('martus', 'BuildFiles', 'ProgramFiles'), :path=>'BuildFiles')
	zip(zip_file).include(_('martus', 'BuildFiles', 'SampleDir'), :path=>'BuildFiles')
	#TODO: Need to include MartusSetupLauncher?

	include_artifacts(zip(zip_file), [artifact(BCJCE_SPEC)], 'BuildFiles/Jars')
	include_artifacts(zip(zip_file), third_party_client_jars, 'BuildFiles/Jars')	
	include_artifacts(zip(zip_file), [_('martus', 'BuildFiles', 'JavaRedistributables', 'Win32', 'jre6')], 'BuildFiles/Java redist/Win32')
	include_artifacts(zip(zip_file), [_('martus', 'BuildFiles', 'Documents')], 'BuildFiles')
	include_artifacts(zip(zip_file), third_party_client_licenses, 'BuildFiles/Documents/Licenses')
	include_artifacts(zip(zip_file), [artifact(INFINITEMONKEY_DLL_SPEC)], 'BuildFiles/ProgramFiles')

	input_dir = "/var/lib/hudson/martus-client/builds/#{input_build_number}"
	signed_jar = "#{input_dir}/martus-client-signed-#{input_build_number}.jar"
	source_zip = "#{input_dir}/martus-client-sources-#{input_build_number}.zip"
	zip(zip_file).include(signed_jar, :as=>"BuildFiles/ProgramFiles/martus.jar")
	zip(zip_file).include(source_zip, :path=>'BuildFiles/SourceFiles')
	
	zip(zip_file).include(_('martus', 'BuildFiles', 'Windows', 'Win32_NSIS'))

	return zip_file
end

def run_nsis_task(nsis_zip, nsi_name, exe_name)
		dest_dir = _(:target, 'Installer')
		FileUtils.rm_rf dest_dir
		Dir.mkdir(dest_dir)
		unzip_file(nsis_zip, dest_dir)
		
		puts "Running makensis from: #{Dir.pwd}"
		puts ":target is #{_(:target)}"
		error_output = `makensis -V2 #{_(:target, "/Installer/Win32_NSIS/#{nsi_name}")}`
		status = $?
		if status.exitstatus > 0
			raise "Error running makensis #{status.exitstatus}: #{error_output.split("\n").join("\n  ")}"
		end
		puts 'Finished makensis'
		mv _(:target, "Installer/Win32_NSIS/#{exe_name}"), _(:target, exe_name)
		FileUtils.rm_rf dest_dir
end
