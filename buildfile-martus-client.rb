name = 'martus-client'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		project('martus-swing').packages.first,
		project('martus-clientside').packages.first,
		LAYOUTS_SPEC,
		project('martus-jar-verifier').packages.first,
		VELOCITY_SPEC
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
