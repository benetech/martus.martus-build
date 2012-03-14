name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']

	base_file = _(:target, "Martus-#{project.version}-#{input_build_number}")
	zip_file = "#{base_file}.zip"
	iso_file = "#{base_file}.iso"
	sha_file = "#{iso_file}.sha1"
	
	puts "*** " + project('martus').path_to('BuildFiles')

	zip(zip_file).include(_('martus', 'BuildFiles', 'ProgramFiles', 'autorun.inf'), :path=>'BuildFiles')
	zip(zip_file).include(_('martus', 'BuildFiles', 'ProgramFiles'), :path=>'BuildFiles/Martus').exclude('autorun.inf')

	zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', 'license.txt'), :path=>'BuildFiles/Martus')
	zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', 'gpl.txt'), :path=>'BuildFiles/Martus')
  zip(zip_file).include(_('martus', "BuildFiles", "Documents", "installing_martus.txt"), :path=>'BuildFiles/Martus')
	zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', "client", 'README*.txt'), :path=>'BuildFiles/Martus')

  zip(zip_file).include(_('martus-jar-verifier', '*.bat'), :path=>'BuildFiles/verify')
	zip(zip_file).include(_('martus-jar-verifier', '*.txt'), :path=>'BuildFiles/verify')
  zip(zip_file).include(_('martus-jar-verifier', "readme_verify*.txt"), :path=>'BuildFiles/verify')

  zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', "client", '*.pdf'), :path=>'BuildFiles/Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_licenses, 'BuildFiles/Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_source, 'SourceFiles')	
	include_artifacts(zip(zip_file), third_party_client_jars, 'BuildFiles/LibExt')	
	include_artifacts(zip(zip_file), [artifact(BCJCE_SPEC)], 'BuildFiles/LibExt')
	include_artifacts(zip(zip_file), [project('martus-client').package(:sources)], 'BuildFiles/Sources')
	include_artifacts(zip(zip_file), [_(:target, 'MartusCDClientSetup-#{project.version}-#{input_build_number}.exe')], 'BuildFiles')
	
	file iso_file => zip_file do
	  puts "Unzipping #{zip_file}"
		dest_dir = _(:target, 'iso')
    FileUtils::rm_rf(dest_dir)
		FileUtils::mkdir(dest_dir)
		unzip_file(zip_file, dest_dir)

		puts "Creating ISO"
		options = '-J -r -T -hide-joliet-trans-tbl -l'
		volume = "-V Martus-#{project.version}-#{input_build_number}"
		output = "-o #{iso_file}"
		`mkisofs #{options} #{volume} #{output} #{dest_dir}`

    create_sha_files(iso_file)
	end

	build(iso_file)
end
