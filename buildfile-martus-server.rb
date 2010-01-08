name = "martus-server"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		project('martus-amplifier').packages.first
	)

	test.with(
		BCPROV_SPEC,
		JETTY_SPEC,
		ICU4J_SPEC,
		XMLRPC_SPEC
	)

	package(:jar).merge(project('martus-jar-verifier').packages.first)
	package(:jar).merge(project('martus-common').packages.first)
	package(:jar).merge(project('martus-utils').packages.first)
	package(:jar).merge(project('martus-hrdag').packages.first)
	package(:jar).merge(project('martus-logi').packages.first)
	package(:jar).merge(project('martus-swing').packages.first)
	package(:jar).merge(project('martus-amplifier').packages.first)

end
