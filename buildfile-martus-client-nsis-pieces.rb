name = "martus-client-nsis-pieces"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  setup_artifact = project('martus-client-nsis-single').artifact(MARTUSSETUP_EXE_SPEC)
  
  temp_dir = File.join(_(:temp), 'chunks')
  base_name = "MartusClientSetupMultiPart-#{project.version}-#{input_build_number}-#{release_build_number}"
  zip_file = _(:target, "#{base_name}.zip")
  original_exe_file = setup_artifact.to_s
  renamed_exe_file = _(:temp, "#{base_name}")
  original_merger_file = _('martus', 'BuildFiles', 'MartusSetupLauncher', 'Release', 'MartusSetupBuilder.exe')
  renamed_merger_file = File.join(temp_dir, "#{base_name}.exe")

  puts "Setting up build dependency: #{setup_artifact.to_s}"
  build(setup_artifact.to_s) do
    FileUtils.mkdir_p(temp_dir)
    FileUtils.cp original_exe_file, renamed_exe_file
    
    puts "Copying #{original_merger_file} to #{renamed_merger_file}"
    FileUtils.cp original_merger_file, renamed_merger_file

    command = "filesplit -s #{renamed_exe_file} 1400 #{temp_dir}/"
    puts command
    result = `#{command}` 
    puts "#{command}\n#{result}"
    if $CHILD_STATUS != 0
      raise "Failed in filesplit #{$CHILD_STATUS}"
    end
    if result.index('Error: ') # filesplit does exit(0) on errors, unfortunately
      raise "Failed in filesplit"
    end
    
    Dir.glob(File.join(temp_dir, '*.cnk')).each do | chunk |
      create_sha_files(chunk)
    end
    
  end

  package(:zip, :file=>zip_file).tap do | p | 
    puts "Creating chunks zip: #{zip_file}"
    p.include(File.join(temp_dir, '*'))
  end
end
