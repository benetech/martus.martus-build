name = 'martus-client-nsis-cd'

require "#{File.dirname(__FILE__)}/buildfile-martus-client-nsis-common"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	exe_name = 'MartusSetup.exe'
	exe_path = _(:target, exe_name)

	nsis_zip = create_nsis_zip_task
	file exe_path => nsis_zip do
		run_nsis_task(nsis_zip, 'NSIS_Martus.nsi', exe_name)
	end
	
	build(exe_path)
end

