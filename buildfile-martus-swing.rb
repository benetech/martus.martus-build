name = "martus-swing"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		LAYOUTS_SPEC,
		project('martus-utils').packages.first
	)
  
	package :jar
	package :sources
end
