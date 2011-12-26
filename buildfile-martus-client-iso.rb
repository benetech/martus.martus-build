name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	base_file = "#{_(:target)}/Martus-#{$build_number}"
	zip_file = "#{base_file}.zip"
	iso_file = "#{base_file}.iso"
	sha_file = "#{iso_file}.iso"
	
	zip(zip_file).include(_('BuildFiles', 'Documents', 'license.txt'), :path=>'BuildFiles')
	zip(zip_file).include(_('BuildFiles', 'Documents', 'gpl.txt'), :path=>'BuildFiles')
		
	zip(zip_file).include(_('BuildFiles/Windows/Winsock95'), :path=>'BuildFiles/Win95')
	
	zip(zip_file).include(_('BuildFiles', 'ProgramFiles', 'autorun.inf'), :path=>'BuildFiles')
	
	zip(zip_file).include(_('BuildFiles', 'ProgramFiles'), :path=>'BuildFiles/Martus').exclude('autorun.inf')
	zip(zip_file).include(_('BuildFiles', 'Documents', 'license.txt'), :path=>'BuildFiles/Martus')
	zip(zip_file).include(_('BuildFiles', 'Documents', 'gpl.txt'), :path=>'BuildFiles/Martus')
	
	zip(zip_file).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/verify')
	zip(zip_file).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/verify')

	zip(zip_file).include(_('BuildFiles', 'Documents', 'README.txt'), :path=>'BuildFiles')
	zip(zip_file).include(_('BuildFiles', 'Documents', 'martus_user_guide.pdf'), :path=>'BuildFiles/Martus/Docs')
	zip(zip_file).include(_('BuildFiles', 'Documents', 'quickstartguide.pdf'), :path=>'BuildFiles/Martus/Docs')

	martus_languages = ['es','ru','ar','fr','th','ne']
	martus_languages.each do | language |
		zip(zip_file).include(_('BuildFiles', 'Documents', "README_#{language}.txt"), :path=>'BuildFiles')
		zip(zip_file).include(_('BuildFiles', 'Documents', "martus_user_guide_#{language}.pdf"), :path=>'BuildFiles/Martus/Docs')
		zip(zip_file).include(_('BuildFiles', 'Documents', "quickstartguide_#{language}.pdf"), :path=>'BuildFiles/Martus/Docs')
		zip(zip_file).include(project('martus-jar-verifier').path_to(:root, "readme_verify_#{language}.txt"), :path=>'BuildFiles')
	end

	zip(zip_file).include(_('BuildFiles', 'Documents', 'LinuxJavaInstall.txt'), :path=>'BuildFiles/Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_licenses, 'BuildFiles/Martus/Docs')
	include_artifacts(zip(zip_file), third_party_client_source, 'SourceFiles')	
	include_artifacts(zip(zip_file), third_party_client_jars, 'BuildFiles/LibExt')	
	include_artifacts(zip(zip_file), [project('martus-bc-jce').package(:jar)], 'BuildFiles/LibExt')
	include_artifacts(zip(zip_file), [project('martus-client').package(:sources)], 'BuildFiles/Sources')
	include_artifacts(zip(zip_file), [_('BuildFiles/JavaRedistributables/Linux')], 'BuildFiles/Java redist/Linux')
	include_artifacts(zip(zip_file), [project('martus-client-nsis-cd').path_to(:target, 'MartusSetup.exe')], 'BuildFiles')
	
	file iso_file => zip_file do
		dest_dir = _(:target, 'iso')
    FileUtils::rm_rf(dest_dir)
		FileUtils::mkdir(dest_dir)
		unzip_file(zip_file, dest_dir)

		options = '-J -r -T -hide-joliet-trans-tbl -l'
		volume = "-V Martus-#{$build_number}"
		output = "-o #{iso_file}"
		`mkisofs #{options} #{volume} #{output} #{dest_dir}`
	end

	file sha_file => iso_file do
		sha(iso_file, sha_file)
	end
	
	build(sha_file)
end
