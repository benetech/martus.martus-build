name = 'martus-client-nsis-single'

require 'buildfile-martus-client-nsis-common'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	exe_name = 'MartusSetupSingle.exe'
	exe_path = _(:target, exe_name)

	nsis_zip = create_nsis_zip_task
	file exe_path => nsis_zip do
		run_nsis_task(nsis_zip, 'NSIS_Martus_Single.nsi', exe_name)
	end
	
	build(exe_path)

	artifact(MARTUSSETUP_EXE_SPEC).from(exe_path)
	
  #TODO: Create SHA-1 of this file

end

