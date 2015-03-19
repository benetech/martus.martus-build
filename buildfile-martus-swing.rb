name = "martus-swing"

def main_source_dir
  return _('source', 'main', 'java')
end

def main_target_dir
  return _('target', 'main', 'classes')
end

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		LAYOUTS_SPEC,
		project('martus-utils').packages.first
	)
 	
 	filter(main_source_dir).include('**/*.png').into(main_target_dir).run
  
	package :jar
	package :sources
end
