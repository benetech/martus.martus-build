def run_nsis_task(nsis_zip, nsi_name, exe_name)
  puts "Unzipping NSIS zip..."
  unzipped_dir = 'FilesForWindowsInstaller'
	dest_dir = _(:temp, unzipped_dir)
	FileUtils.rm_rf dest_dir
	FileUtils.mkdir_p(dest_dir)
	unzip_file(nsis_zip, dest_dir)
	
	puts "Running makensis from: #{Dir.pwd}"
	puts "temp is #{_(:temp)}"
	error_output = `makensis -V2 #{_(:temp, unzipped_dir, "Win32_NSIS", "#{nsi_name}")}`
	status = $?
	if status.exitstatus > 0
		raise "Error running makensis #{status.exitstatus}: #{error_output.split("\n").join("\n  ")}"
	end
	puts 'Finished makensis'
	FileUtils.mkdir_p _(:target)
	mv _(:temp, unzipped_dir, 'Win32_NSIS', "#{exe_name}"), _(:target, exe_name)
	
	# Uncomment 
	#FileUtils.rm_rf dest_dir
end
