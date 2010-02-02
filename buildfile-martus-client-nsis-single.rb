name = 'martus-client-nsis-single'

require 'buildfile-martus-client-nsis-common'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	exe_name = 'MartusSetupSingle.exe'
	exe_path = _(:target, exe_name)
	file(exe_path) do
		define_nsis('NSIS_Martus_Single.nsi', exe_name)
	end
	
	exe = artifact(MARTUSSETUP_EXE_SPEC).from(exe_path)
	install exe

end

