name = "martus-common"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		XMLRPC_SPEC,
		ICU4J_SPEC,
		PERSIANCALENDAR_SPEC,
		BCPROV_SPEC,
		'org.martus:martus-logi:jar:1',
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-swing:jar:1'
	)
  
	package :jar
end
