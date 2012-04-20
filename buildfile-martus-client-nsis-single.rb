name = 'martus-client-nsis-single'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

	exe_name = "MartusSetupSingle.exe"
	exe_path = _(:target, exe_name)
  destination = _(:target, "MartusClientSetup-#{project.version}-#{input_build_number}-#{release_build_number}.exe")

  file get_nsis_zip_file do
    create_nsis_zip
  end
  
	file exe_path => get_nsis_zip_file do
	  puts "Building NSIS Single installer"
		run_nsis_task(nsis_zip, 'NSIS_Martus_Single.nsi', exe_name)
		FileUtils.mv exe_path, destination
    create_sha_files(destination)
	end
	
	build(exe_path) do
	  artifact(MARTUSSETUP_EXE_SPEC).from(destination)
	  puts "Created artifact #{MARTUSSETUP_EXE_SPEC}"
	end
	
end

