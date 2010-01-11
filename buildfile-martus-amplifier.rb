name = "martus-amplifier"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		JUNIT_SPEC,
		ICU4J_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		XMLRPC_SPEC,
		JETTY_SPEC,
		JAVAX_SERVLET_SPEC,
		LUCENE_SPEC,
		VELOCITY_SPEC
	)
  
	build do
		filter(_(:root, 'presentation')).into(_('target', 'presentation')).run
		from_dir = _(:source, :main, :java)
		to_dir = _(:target, :main, :classes)
		puts "From: #{from_dir} to #{to_dir}"
		filter(from_dir).include('**/*.txt').into(to_dir).run
	end
	
	test.with(
		BCPROV_SPEC
	)

	package :jar

	# NOTE: Old build script signed this jar

end
