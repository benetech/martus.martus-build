name = 'martus-client-iso'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	package(:zip)
	package(:zip).include(_('BuildFiles', 'Documents', 'license.txt'), :path=>'BuildFiles')
	package(:zip).include(_('BuildFiles', 'Documents', 'gpl.txt'), :path=>'BuildFiles')
	
	package(:zip).include(_('BuildFiles/Windows/Winsock95'), :path=>'BuildFiles/Win95')
	
	package(:zip).include(_('BuildFiles', 'ProgramFiles', 'autorun.inf'), :path=>'BuildFiles')
	
	package(:zip).include(_('BuildFiles', 'ProgramFiles'), :path=>'BuildFiles/Martus').exclude('autorun.inf')
	package(:zip).include(_('BuildFiles', 'Documents', 'license.txt'), :path=>'BuildFiles/Martus')
	package(:zip).include(_('BuildFiles', 'Documents', 'gpl.txt'), :path=>'BuildFiles/Martus')
	
	package(:zip).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/verify')
	package(:zip).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/verify')
	
	package(:zip).include(_('BuildFiles', 'Documents', 'README.txt'), :path=>'BuildFiles')
	package(:zip).include(_('BuildFiles', 'Documents', 'martus_user_guide.pdf'), :path=>'BuildFiles/Martus/Docs')
	package(:zip).include(_('BuildFiles', 'Documents', 'quickstartguide.pdf'), :path=>'BuildFiles/Martus/Docs')

	# TODO: Include documentation for all supported languages in CD ISO	
#	for martus_lang in $MARTUS_LANGUAGES
#		do
#		echo -e "\tcopying docs for language: ${martus_lang}"
#		cp -v $MARTUSBUILDFILES/Documents/README_${martus_lang}.txt $CD_IMAGE_DIR || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/README_${martus_lang}.txt"
#		cp -v $MARTUSBUILDFILES/Documents/martus_user_guide_${martus_lang}.pdf $CD_IMAGE_DIR/Martus/Docs || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/martus_user_guide_${martus_lang}.pdf"
#		cp -v $MARTUSBUILDFILES/Documents/quickstartguide_${martus_lang}.pdf $CD_IMAGE_DIR/Martus/Docs || message "ERROR: Unable to copy $MARTUSBUILDFILES/Documents/quickstartguide_${martus_lang}.pdf"
#		cp -v $MARTUSBUILDFILES/Verify/readme_verify_${martus_lang}.txt $CD_IMAGE_DIR/verify/ || message "ERROR: Unable to copy $CD_IMAGE_DIR/readme_verify_${martus_lang}.txt"
#	done

	package(:zip).include(_('BuildFiles', 'Documents', 'LinuxJavaInstall.txt'), :path=>'BuildFiles/Martus/Docs')

	#TODO: Need to include all licenses (avoid dupe code with buildfile-martus-client-nsis-common)
	package_artifacts(package(:zip), [artifact(BCPROV_LICENSE_SPEC)], 'BuildFiles/Martus/Docs')

	package_artifacts(package(:zip), [project('martus-bc-jce').package(:jar)], 'BuildFiles/LibExt')
	package_artifacts(package(:zip), third_party_client_jars, 'BuildFiles/LibExt')	
	package_artifacts(package(:zip), [project('martus-client').package(:sources)], 'BuildFiles/Sources')
	package_artifacts(package(:zip), [_('BuildFiles/JavaRedistributables/Linux')], 'BuildFiles/Java redist/Linux')
	package_artifacts(package(:zip), [project('martus-client-nsis-cd').path_to(:target, 'MartusSetup.exe')], 'BuildFiles')

	update_packaged_zip(package(:zip)) do | filespec |
		dest_dir = File.join(File.dirname(filespec), 'iso')
		Dir.mkdir(dest_dir)
		unzip_file(filespec, dest_dir)

		options = '-J -r -T -hide-joliet-trans-tbl -l'
		volume = "-V Martus-#{$build_number}"
		output = "-o #{_(:target)}/Martus-#{$build_number}.iso"
		`mkisofs #{options} #{volume} #{output} #{dest_dir}`
	end
end
