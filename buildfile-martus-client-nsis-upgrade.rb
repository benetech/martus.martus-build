name = 'martus-client-nsis-upgrade'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  exe_name = "MartusSetupUpgrade.exe"
  exe_path = _(:target, exe_name)

  nsis_zip_file = project('martus-client-nsis-zip').package(:zip)

  file exe_path => nsis_zip_file do
    puts "Building NSIS upgrade installer"
		run_nsis_task(nsis_zip_file.to_s, 'NSIS_Martus_Upgrade.nsi', exe_name)
    destination = _(:target, "MartusClientUpgrade-#{project.version}-#{input_build_number}-#{release_build_number}.exe")
    FileUtils.mv exe_path, destination
    create_sha_files(destination)
  end
  
  build(exe_path)

  artifact(MARTUSSETUP_UPGRADE_EXE_SPEC).from(exe_path)
end
