name = "martus-logi"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.target = '1.5'
	compile.with(
	)
  
	package :jar
	package :sources
end
