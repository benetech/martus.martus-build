name = "martus-js-xml-generator"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		'junit:junit:jar:3.8.2',
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-common:jar:1'
	)
  
	package :jar
end
