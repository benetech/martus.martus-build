name = "martus-swing"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		"com.jhlabs:layouts:jar:2006-08-10",
		'org.martus:martus-utils:jar:1'
	)
  
	package :jar
end
