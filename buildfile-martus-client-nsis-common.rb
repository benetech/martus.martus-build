def run_nsis_task(nsis_zip, nsi_name, exe_name)
	puts "Unzipping NSIS zip..."
	previous_pwd = Dir.pwd
    
    unzipped_dir = 'FilesForWindowsInstaller'
	dest_dir = _(:temp, unzipped_dir)
	FileUtils.rm_rf dest_dir
	FileUtils.mkdir_p(dest_dir)
	unzip_file(nsis_zip, dest_dir)
	
	FileUtils.chdir File.join(dest_dir, $nsis_script_dir)
	puts "temp is #{_(:temp)}"
	puts "Running makensis from: #{Dir.pwd}"
	nsis_cmd = "#{$nsis_command} #{nsi_name}"
	puts "Running: #{nsis_cmd}"
	error_output = `#{nsis_cmd}`
	status = $?
	if status.exitstatus > 0
		error = error_output.split("\n").join("\n  ")
		raise "Error running makensis #{status.exitstatus}: #{error}"
	end
	puts 'Finished makensis'
	
	FileUtils.mkdir_p _(:target)
	mv exe_name, _(:target, exe_name)
	
	# Uncomment to clean up, but leave commented out for easier debugging 
	#FileUtils.rm_rf dest_dir

	FileUtils.chdir previous_pwd
end
