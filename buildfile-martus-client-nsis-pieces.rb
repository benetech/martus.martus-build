name = "martus-client-nsis-pieces"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	build(artifact(MARTUSSETUP_EXE_SPEC)) do
		#TODO: Run filesplit utility
		# /MartusSetupLauncher/filesplit-2.0.100/bin/filesplit.exe
		#TODO: Need to generate SHA1's of pieces
		#TODO: Need to package up the piece restorer?
	end
	
end
