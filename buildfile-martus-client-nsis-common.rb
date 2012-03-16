def create_nsis_zip_task
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']

  zip_file = _('temp', 'nsis.zip')

	nsis_zip = zip(zip_file)

  attic_dir = "/var/lib/hudson/martus-client/builds/#{input_build_number}/"

  signed_jar = "#{attic_dir}/martus-client-signed-#{input_build_number}.jar"
  zip(zip_file).include(signed_jar, :as=>"martus.jar")
  source_zip = "#{attic_dir}/martus-client-sources-#{input_build_number}.zip"
  zip(zip_file).include(source_zip, :path=>'BuildFiles/SourceFiles')

	zip(zip_file).include(_('martus', 'BuildFiles', '*.txt'), :path=>'BuildFiles')

	include_artifacts(zip(zip_file), third_party_client_source, 'SourceFiles')	
	
	zip(zip_file).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
	zip(zip_file).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
	zip(zip_file).include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
	#TODO: Need to include MartusWin32SetupLauncher?
	zip(zip_file).include(_('martus', 'BuildFiles', 'ProgramFiles'), :path=>'BuildFiles')
	zip(zip_file).include(_('martus', 'BuildFiles', 'SampleDir'), :path=>'BuildFiles')
	#TODO: Need to include MartusSetupLauncher?

	include_artifact(zip(zip_file), artifact(BCJCE_SPEC), 'BuildFiles/Jars', 'bc-jce.jar')
	include_artifacts(zip(zip_file), third_party_client_jars, 'BuildFiles/Jars')	
	include_artifacts(zip(zip_file), [_('martus', 'BuildFiles', 'JavaRedistributables', 'Win32', 'jre6')], 'BuildFiles/jre6')
	include_artifacts(zip(zip_file), [_('martus', 'BuildFiles', 'Documents')], 'BuildFiles')
	include_artifacts(zip(zip_file), third_party_client_licenses, 'BuildFiles/Documents/Licenses')

	
	zip(zip_file).include(_('martus', 'BuildFiles', 'Windows', 'Win32_NSIS'))

	return zip_file
end

def run_nsis_task(nsis_zip, nsi_name, exe_name)
  puts "Unzipping NSIS zip..."
  unzipped_dir = 'FilesForWindowsInstaller'
	dest_dir = _(:temp, unzipped_dir)
	FileUtils.rm_rf dest_dir
	FileUtils.mkdir_p(dest_dir)
	unzip_file(nsis_zip, dest_dir)
	
	puts "Running makensis from: #{Dir.pwd}"
	puts ":target is #{_(:temp)}"
	error_output = `makensis -V2 #{_(:temp, unzipped_dir, "Win32_NSIS", "#{nsi_name}")}`
	status = $?
	if status.exitstatus > 0
		raise "Error running makensis #{status.exitstatus}: #{error_output.split("\n").join("\n  ")}"
	end
	puts 'Finished makensis'
	mv _(:temp, unzipped_dir, 'Win32_NSIS', "#{exe_name}"), _(:target, exe_name)
	
	# Uncomment 
	#FileUtils.rm_rf dest_dir
end
