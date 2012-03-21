name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER

  iso_name = "MartusClientCD-#{project.version}-#{input_build_number}-#{release_build_number}.iso"
	zip_file = _(:temp, "#{iso_name}.zip")
	iso_file = _(:target, iso_name)
	volume_name = "Martus-#{project.version}-#{input_build_number}-#{release_build_number}"

	file iso_file do
    puts "Unzipping #{zip_file}"
    dest_dir = _(:target, 'iso')
    FileUtils::rm_rf(dest_dir)
    FileUtils::mkdir(dest_dir)
    unzip_file(zip_file, dest_dir)
  
    puts "Creating ISO"
    options = '-J -r -T -hide-joliet-trans-tbl -l'
    volume = "-V #{volume_name}"
    output = "-o #{iso_file}"
    `mkisofs #{options} #{volume} #{output} #{dest_dir}`
  
    create_sha_files(iso_file)
	end
	
  def package_as_iso(iso_file)
    return file iso_file
  end
	
	package(:zip, :file=>zip_file).tap do | p |
    p.include(_('martus', 'BuildFiles', 'ProgramFiles', 'autorun.inf'))
    p.include(_('martus', 'BuildFiles', 'ProgramFiles'), :path=>'Martus').exclude('autorun.inf')
    
    p.include(_('martus', 'BuildFiles', 'Documents', 'license.txt'), :path=>'Martus')
    p.include(_('martus', 'BuildFiles', 'Documents', 'gpl.txt'), :path=>'Martus')
    p.include(_('martus', "BuildFiles", "Documents", "installing_martus.txt"), :path=>'Martus')
    p.include(_('martus', 'BuildFiles', 'Documents', "client", 'README*.txt'), :path=>'Martus')
  
    p.include(_('martus-jar-verifier', '*.bat'), :path=>'verify')
    p.include(_('martus-jar-verifier', '*.txt'), :path=>'verify')
    p.include(_('martus-jar-verifier', "readme_verify*.txt"), :path=>'verify')
  
    p.include(_('martus', 'BuildFiles', 'Documents', "client", '*.pdf'), :path=>'Martus/Docs')
  #NOTE: For now at least, don't include Linux zip
  #  include_artifacts(zip(zip_file), [project('martus-client-linux-zip').package(:zip)], '')
    include_artifact(p, artifact(DMG_SPEC), '', "MartusClient-#{$client_version}.dmg")
    include_artifacts(p, third_party_client_licenses, 'Martus/Docs')
    include_artifacts(p, third_party_client_source, 'SourceFiles')  
    include_artifacts(p, third_party_client_jars, 'LibExt') 
    include_artifacts(p, [artifact(BCJCE_SPEC)], 'LibExt')
    include_artifacts(p, [project('martus-client').package(:sources)], 'Sources')
    include_artifacts(p, [_(:temp, 'iso', 'MartusClientCDSetup.exe')], '')
	end
	
	package :iso
	
end
