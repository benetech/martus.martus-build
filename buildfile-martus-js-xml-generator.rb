name = "martus-js-xml-generator"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first
	)

	main_source_dir = _('source', 'main', 'java')
	main_target_dir = _('target', 'main', 'classes')

	build do
		filter(main_source_dir).include('**/*.csv').into(main_target_dir).run
		filter(main_source_dir).include('**/*.js').into(main_target_dir).run
		filter(main_source_dir).include('**/*.xml').into(main_target_dir).run
	end
	
	package :jar
end
