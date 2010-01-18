name = 'martus-client-nsis-single'

require 'buildfile-martus-client-nsis-common'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	define_nsis('NSIS_Martus_Single.nsi', 'MartusSetupSingle.exe')
end

