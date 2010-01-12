name = "martus-mspa"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		LAYOUTS_SPEC,
		MAIL_SPEC,
		project('martus-utils').packages.first,
		project('martus-swing').packages.first,
		project('martus-common').packages.first,
		project('martus-clientside').packages.first
	)


	# The following test fails on Linux because:
	#  1. It refers to a relative path instead of absolute, and
	#  2. There is a permissions error which I don't understand
	test.exclude 'org.martus.mspa.roothelper.TestProcessStdinStdOut'

	test.with(
		XMLRPC_SPEC,
		ICU4J_SPEC
	)

	jar_name = _("#{name}/target/martus-mspa-client-#{$build_number}.jar")
	puts jar_name
	package :jar, :file=>jar_name

	# TODO: Old build script signed this jar

	zip_name = _("#{name}/target/MartusMSPA.zip")
	package :zip, :file=>zip_name
	package(:zip).include(jar_name)
	package(:zip).include(artifact(XMLRPC_SPEC))
	package(:zip).include(artifact(PERSIANCALENDAR_SPEC))
	package(:zip).include(artifact(ICU4J_SPEC))
	package(:zip).include(artifact(BCPROV_SPEC))
	package(:zip).include(artifact(LAYOUTS_SPEC))
	package(:zip).include(artifact(INFINITEMONKEY_DLL_SPEC))
	package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC))
	package(:zip).include(project('martus-common').packages.first)
	package(:zip).include(project('martus-bc-jce').packages.first)
	#TODO: Add mspa client user guide to mspa client zip
#	package(:zip).include(mspa client user guide)

end
