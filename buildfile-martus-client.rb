name = 'martus-client'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	main_source_dir = _('source', 'main', 'java')
	main_target_dir = _('target', 'main', 'classes')
	test_source_dir = _('source', 'test', 'java')
	test_target_dir = _('target', 'test', 'classes')


	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		project('martus-swing').packages.first,
		project('martus-clientside').packages.first,
		BCPROV_SPEC,
		LAYOUTS_SPEC,
		project('martus-jar-verifier').packages.first,
		VELOCITY_SPEC
	)

	build do
	  version_file = _('target', 'version.txt') 
    puts version_file
    FileUtils::mkdir_p(_('target'))
    File.open(version_file, "w") do | file |
      file.puts(Time.now)
    end

    filter(test_source_dir).include('**/test/*.mlp').into(test_target_dir).run
		filter(test_source_dir).include('**/test/Sample*.*').into(test_target_dir).run
		# TODO: Need to exclude unapproved mtf files like km
		filter(test_source_dir).include('**/test/Martus-*.mtf').into(test_target_dir).run
		filter(test_source_dir).include('**/test/MartusHelp-*.txt').into(test_target_dir).run
		filter(test_source_dir).include('**/test/MartusHelpTOC-*.txt').into(test_target_dir).run

		filter(main_source_dir).include('**/*.png').into(main_target_dir).run
		filter(main_source_dir).include('**/*.gif').into(main_target_dir).run
		filter(main_source_dir).include('**/*.jpg').into(main_target_dir).run

		filter(main_source_dir).include('org/martus/client/swingui/Martus-*.mtf').into(main_target_dir).run
		filter(main_source_dir).include('org/martus/client/swingui/MartusHelp-*.txt').into(main_target_dir).run
		filter(main_source_dir).include('org/martus/client/swingui/MartusHelpTOC-*.txt').into(main_target_dir).run

		filter(main_source_dir).include('org/martus/client/swingui/UnofficialTranslationMessage.txt').into(main_target_dir).run
		filter(main_source_dir).include('org/martus/client/swingui/UnofficialTranslationMessageRtoL.txt').into(main_target_dir).run

    bcjce_sig_file = extract_artifact_entry_task(artifact(BCJCE_SPEC), "META-INF/SSMTSJAR.SF")
    FileUtils.move(bcjce_sig_file, File.new(main_target_dir, "META-INF/SSMTSJAR.SIG"))
		#TODO: Need to extract BCKEY.SF from bcprov-xxx.jar, and add it to the jar as BCKEY.SIG
	end

	test.with(
		ICU4J_SPEC,
		BCPROV_SPEC,
		VELOCITY_DEP_SPEC
	)

	test.exclude('org.martus.client.test.TestImporterOfXmlFilesOfBulletins')
	test.exclude('org.martus.client.test.TestLocalization')
	test.exclude('org.martus.client.test.TestMartusApp_NoServer')


	package(:jar).with :manifest=>manifest.merge('Main-Class'=>'org.martus.client.swingui.Martus')

	package(:jar).include(File.join(_('source', 'test', 'java'), '**/*.mlp'))
	package(:jar).merge(project('martus-jar-verifier').package(:jar))
	package(:jar).merge(project('martus-common').package(:jar))
	package(:jar).merge(project('martus-utils').package(:jar))
	package(:jar).merge(project('martus-hrdag').package(:jar))
	package(:jar).merge(project('martus-logi').package(:jar))
	package(:jar).merge(project('martus-swing').package(:jar))
	package(:jar).merge(project('martus-clientside').package(:jar))
	package(:jar).merge(project('martus-js-xml-generator').package(:jar))

	# TODO: Old build script signed this jar

  package(:zip, :classifier=>'sources')
  package(:zip, :classifier=>'sources').include(File.join(_('source', 'test', 'java'), '**/*.mlp'))
  package(:zip, :classifier=>'sources').merge(project('martus-jar-verifier').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-common').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-utils').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-hrdag').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-logi').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-swing').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-clientside').package(:sources))
  package(:zip, :classifier=>'sources').merge(project('martus-js-xml-generator').package(:sources))

end
