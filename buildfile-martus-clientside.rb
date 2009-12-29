name = 'martus-clientside'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-common:jar:1',
		'org.martus:martus-swing:jar:1',
		LAYOUTS_SPEC,
		XMLRPC_SPEC,
		'org.martus:martus-jar-verifier:jar:1'
	)
  
	package :jar
end
