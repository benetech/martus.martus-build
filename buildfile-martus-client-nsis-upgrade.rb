name = 'martus-client-nsis-upgrade'

require 'buildfile-martus-client-nsis-common'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	define_nsis('NSIS_Martus_Upgrade.nsi', 'MartusSetupUpgrade.exe')
end

