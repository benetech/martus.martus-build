name = 'martus-client-nsis-cd'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']

	exe_name = 'MartusSetup.exe'
	exe_path = _(:target, exe_name)

	nsis_zip = create_nsis_zip_task
	file exe_path => nsis_zip do
    puts "Building NSIS CD installer"
		run_nsis_task(nsis_zip, 'NSIS_Martus.nsi', exe_name)
    destination = _(:target, "MartusCDClientSetup-#{project.version}-#{input_build_number}.exe")
    FileUtils.mv exe_path, destination
    create_sha_files(destination)
	end
	
	build(exe_path)

  artifact(MARTUSSETUP_EXE_SPEC).from(exe_path)
end

