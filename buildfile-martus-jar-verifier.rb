name = "martus-jar-verifier"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	main_source_dir = _('source', 'main', 'java')
	main_target_dir = _('target', 'main', 'classes')

	compile.options.target = '1.5'
	compile.with(
	)

	build do
		filter(main_source_dir).include('**/ssmartusclientks').into(main_target_dir).run
	end
  
	package :jar
	package :sources
end
