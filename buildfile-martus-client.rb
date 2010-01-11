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
		LAYOUTS_SPEC,
		project('martus-jar-verifier').packages.first,
		VELOCITY_SPEC
	)

	build do
		filter(test_source_dir).include('**/test/*.mlp').into(test_target_dir).run
		filter(test_source_dir).include('**/test/Sample*.*').into(test_target_dir).run
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
	end

	test.with(
		ICU4J_SPEC,
		BCPROV_SPEC,
		VELOCITY_DEP_SPEC
	)

	package(:jar).with :manifest=>manifest.merge('Main-Class'=>'org.martus.client.swingui.Martus')

	package(:jar).include(File.join(_('source', 'test', 'java'), '**/*.mlp'))
	package(:jar).merge(project('martus-jar-verifier').packages.first)
	package(:jar).merge(project('martus-common').packages.first)
	package(:jar).merge(project('martus-utils').packages.first)
	package(:jar).merge(project('martus-hrdag').packages.first)
	package(:jar).merge(project('martus-logi').packages.first)
	package(:jar).merge(project('martus-swing').packages.first)
	package(:jar).merge(project('martus-clientside').packages.first)
	package(:jar).merge(project('martus-js-xml-generator').packages.first)

	# NOTE: Old build script signed this jar

end
