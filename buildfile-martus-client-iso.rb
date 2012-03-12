name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	base_file = "#{_(:target)}/Martus-#{$BUILD_NUMBER}"
	zip_file = "#{base_file}.zip"
	iso_file = "#{base_file}.iso"
	sha_file = "#{iso_file}.iso"
	
	zip(zip_file).include(project('martus').path_to('BuildFiles', 'ProgramFiles', 'autorun.inf'), :path=>'BuildFiles')
	zip(zip_file).include(project('martus').path_to('BuildFiles', 'ProgramFiles'), :path=>'BuildFiles/Martus').exclude('autorun.inf')

	zip(zip_file).include(project('martus').path_to('BuildFiles', 'Documents', 'license.txt'), :path=>'BuildFiles/Martus')
	zip(zip_file).include(project('martus').path_to('BuildFiles', 'Documents', 'gpl.txt'), :path=>'BuildFiles/Martus')
  zip(zip_file).include(project('martus').path_to("BuildFiles", "Documents", "installing_martus.txt"), :path=>'BuildFiles/Martus')
  zip(zip_file).include(project('martus').path_to("BuildFiles", "Documents", "license.txt"), :path=>'BuildFiles/Martus')
  zip(zip_file).include(project('martus').path_to("BuildFiles", "Documents", "gpl.txt"), :path=>'BuildFiles/Martus')
	zip(zip_file).include(project('martus').path_to('BuildFiles', 'Documents', "client", 'README*.txt'), :path=>'BuildFiles/Martus')

  zip(zip_file).include(project('martus-jar-verifier').path_to('*.bat'), :path=>'BuildFiles/verify')
	zip(zip_file).include(project('martus-jar-verifier').path_to('*.txt'), :path=>'BuildFiles/verify')
  zip(zip_file).include(project('martus-jar-verifier').path_to("readme_verify*.txt"), :path=>'BuildFiles/verify')

  zip(zip_file).include(project('martus').path_to('BuildFiles', 'Documents', "client", '*.pdf'), :path=>'BuildFiles/Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_licenses, 'BuildFiles/Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_source, 'SourceFiles')	
	include_artifacts(zip(zip_file), third_party_client_jars, 'BuildFiles/LibExt')	
	include_artifacts(zip(zip_file), [artifact(BCJCE_SPEC)], 'BuildFiles/LibExt')
	include_artifacts(zip(zip_file), [project('martus-client').package(:sources)], 'BuildFiles/Sources')
	include_artifacts(zip(zip_file), [_('BuildFiles/JavaRedistributables/Linux')], 'BuildFiles/Java redist/Linux')
	include_artifacts(zip(zip_file), [project('martus-client-nsis-cd').path_to(:target, 'MartusSetup.exe')], 'BuildFiles')
	
	file iso_file => zip_file do
		dest_dir = _(:target, 'iso')
    FileUtils::rm_rf(dest_dir)
		FileUtils::mkdir(dest_dir)
		unzip_file(zip_file, dest_dir)

		options = '-J -r -T -hide-joliet-trans-tbl -l'
		volume = "-V Martus-#{$BUILD_NUMBER}"
		output = "-o #{iso_file}"
		`mkisofs #{options} #{volume} #{output} #{dest_dir}`
	end

	file sha_file => iso_file do
		sha(iso_file, sha_file)
	end
	
	build(sha_file)
end
