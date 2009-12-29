name = 'martus-client'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-common:jar:1',
		'org.martus:martus-swing:jar:1',
		'org.martus:martus-clientside:jar:1',
		'com.jhlabs:layouts:jar:2006-08-10',
		'org.martus:martus-jar-verifier:jar:1',
		'velocity:velocity:jar:1.4'
	)

  
	package(:jar).merge(project('martus-jar-verifier').packages.first)
	package(:jar).merge(project('martus-common').packages.first)
	package(:jar).merge(project('martus-utils').packages.first)
	package(:jar).merge(project('martus-hrdag').packages.first)
	package(:jar).merge(project('martus-logi').packages.first)
	package(:jar).merge(project('martus-swing').packages.first)
	package(:jar).merge(project('martus-clientside').packages.first)
	package(:jar).merge(project('martus-js-xml-generator').packages.first)

end
