name = 'martus-client-nsis-cd'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  temp_dir = _(:temp)
	exe_name = 'MartusClientCDSetup.exe'
	exe_path = File.join(temp_dir, exe_name)

	file get_nsis_zip_file do
	  create_nsis_zip
	end
	
	file exe_path => get_nsis_zip_file do
    puts "Building NSIS CD installer"
    FileUtils.mkdir_p temp_dir
		run_nsis_task(get_nsis_zip_file, 'NSIS_Martus.nsi', exe_name)
		FileUtils.mv(_(:target, exe_name), exe_path)
	end
	
	build(exe_path)

  artifact(MARTUSSETUP_EXE_SPEC).from(exe_path)
end

