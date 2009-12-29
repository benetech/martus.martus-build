name = "martus-swing"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		LAYOUTS_SPEC,
		'org.martus:martus-utils:jar:1'
	)
  
	package :jar
end
