name = 'martus-bc-jce'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER
	
	compile.options.target = '1.5'
	compile.with(
		BCPROV_SPEC
	)

	package :jar

	# NOTE: Old build script signed this jar

	package :sources
end
