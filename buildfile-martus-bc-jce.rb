name = 'martus-bc-jce'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'
	
	compile.options.target = '1.5'
	compile.with(
		BCPROV_SPEC
	)

	package :jar

	# TODO: Old build script signed this jar,
	# then extracted the .SF and .SIG files,
	# so they would get embedded in org/martus/commmon/crypto

	package :sources
end
