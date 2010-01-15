name = 'martus-client-nsis-upgrade'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	package(:zip).include(_('BuildFiles/*.txt'))

	# Need to include SourceFiles
	package(:zip).include(_('BuildFiles/Windows/Winsock95'), :path=>'Verifier')
	package(:zip).include(_('martus-jar-verifier/*.txt'), :path=>'Verifier')
	package(:zip).include(_('martus-jar-verifier/*.bat'), :path=>'Verifier')
	package(:zip).include(_('martus-jar-verifier/source'), :path=>'Verifier')
	# Need to include MartusWin32SetupLauncher?
	package(:zip).include(_('BuildFiles/ProgramFiles'))
	package(:zip).include(_('BuildFiles/SampleDir'))
	# Need to include MartusSetupLauncher?

	#TODO: Should we really include the MSPA zip?
	package(:zip).include(_('martus-mspa/target/MartusMSPA.zip'), :path=>'Jars')
	package(:zip).include(project('martus-bc-jce').package(:jar), :path=>'Jars')
	package(:zip).include(artifact(RHINO_SPEC), :path=>'Jars')
	package(:zip).include(artifact(LAYOUTS_SPEC), :path=>'Jars')
	package(:zip).include(artifact(BCPROV_SPEC), :path=>'Jars')
	package(:zip).include(artifact(JUNIT_SPEC), :path=>'Jars')
	package(:zip).include(artifact(ICU4J_SPEC), :path=>'Jars')
	package(:zip).include(artifact(PERSIANCALENDAR_SPEC), :path=>'Jars')
	package(:zip).include(artifact(VELOCITY_SPEC), :path=>'Jars')
	package(:zip).include(artifact(VELOCITY_DEP_SPEC), :path=>'Jars')
	package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'Jars')
	package(:zip).include(artifact(XMLRPC_SPEC), :path=>'Jars')
	package(:zip).include(_('BuildFiles/JavaRedistributables/Win32'), :path=>'Java redist/Win32')
	package(:zip).include(_('BuildFiles/Documents'))
	# Need to include Documents/Licenses

	package(:zip).include(artifact(INFINITEMONKEY_DLL_SPEC), :path=>'ProgramFiles')
	package(:zip).include(project('martus-client').package(:jar), :path=>'ProgramFiles', :as=>'martus.jar')

	package(:zip).include(_('BuildFiles/Windows/Win32_NSIS'), :path=>'Installer/NSIS Scripts')


	update_packaged_zip(package(:zip)) do | filespec |
		dest_dir = File.join(File.dirname(filespec), 'temp')
		Dir.mkdir(dest_dir)
		unzip_file(filespec, dest_dir)
		#TODO: Need to run makensis in that directory
	end
end

