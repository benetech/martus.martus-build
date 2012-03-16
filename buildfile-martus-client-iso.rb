name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  iso_name = "MartusClientCD-#{project.version}-#{input_build_number}-#{release_build_number}.iso"
	zip_file = _(:temp, "#{iso_name}.zip")
	iso_file = _(:target, iso_name)
	
	zip(zip_file).include(_('martus', 'BuildFiles', 'ProgramFiles', 'autorun.inf'))
	zip(zip_file).include(_('martus', 'BuildFiles', 'ProgramFiles'), :path=>'Martus').exclude('autorun.inf')
	
	zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', 'license.txt'), :path=>'Martus')
	zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', 'gpl.txt'), :path=>'Martus')
  zip(zip_file).include(_('martus', "BuildFiles", "Documents", "installing_martus.txt"), :path=>'Martus')
	zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', "client", 'README*.txt'), :path=>'Martus')

  zip(zip_file).include(_('martus-jar-verifier', '*.bat'), :path=>'verify')
	zip(zip_file).include(_('martus-jar-verifier', '*.txt'), :path=>'verify')
  zip(zip_file).include(_('martus-jar-verifier', "readme_verify*.txt"), :path=>'verify')

  zip(zip_file).include(_('martus', 'BuildFiles', 'Documents', "client", '*.pdf'), :path=>'Martus/Docs')
#NOTE: For now at least, don't include Linux zip
#  include_artifacts(zip(zip_file), [project('martus-client-linux-zip').package(:zip)], '')
  # TODO: Include Mac DMG here
	include_artifacts(zip(zip_file), third_party_client_licenses, 'Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_source, 'SourceFiles')	
	include_artifacts(zip(zip_file), third_party_client_jars, 'LibExt')	
	include_artifacts(zip(zip_file), [artifact(BCJCE_SPEC)], 'LibExt')
	include_artifacts(zip(zip_file), [project('martus-client').package(:sources)], 'Sources')
	include_artifacts(zip(zip_file), [_(:temp, 'iso', 'MartusClientCDSetup.exe')], '')
	
	file iso_file => zip_file do
	  puts "Unzipping #{zip_file}"
		dest_dir = _(:target, 'iso')
    FileUtils::rm_rf(dest_dir)
		FileUtils::mkdir(dest_dir)
		unzip_file(zip_file, dest_dir)

		puts "Creating ISO"
		options = '-J -r -T -hide-joliet-trans-tbl -l'
		volume = "-V Martus-#{project.version}-#{input_build_number}-#{release_build_number}"
		output = "-o #{iso_file}"
		`mkisofs #{options} #{volume} #{output} #{dest_dir}`

    create_sha_files(iso_file)
	end

	build(iso_file)
end
