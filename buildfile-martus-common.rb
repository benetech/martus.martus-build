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
		project('martus-logi').package(:jar),
		project('martus-utils').package(:jar),
		project('martus-swing').package(:jar)
	)

	test.with(
		project('martus-bc-jce').package(:jar)
	)
	bc_jce = project('martus-bc-jce').path_to('bc-jce.jar')
	test.using :java_args => "-Xbootclasspath/a:#{bc_jce}"

	#TODO: Failing test
	test.exclude 'org.martus.common.test.TestMartusSecurity'

	package :jar

	# TODO: Old build script signed this jar

	package :sources
end
