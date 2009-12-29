name = "martus-amplifier"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		'junit:junit:jar:3.8.2',
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-common:jar:1',
		'jetty:jetty:jar:4.2.27',
		'jetty:javax.servlet:jar:5.1.12',
		'lucene:lucene:jar:1.3-rc1',
		'velocity:velocity:jar:1.4'
	)
  
	package :jar
end
