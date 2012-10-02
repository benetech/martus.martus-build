name = "martus-server"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

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

	package(:jar) do
	  puts "Packaging server"
	end
	package(:jar).with :manifest=>manifest.merge('Main-Class'=>'org.martus.server.main.MartusServer')

	package(:jar).merge(project('martus-jar-verifier').package(:jar))
	package(:jar).merge(project('martus-common').package(:jar))
	package(:jar).merge(project('martus-utils').package(:jar))
	package(:jar).merge(project('martus-hrdag').package(:jar))
	package(:jar).merge(project('martus-logi').package(:jar))
	package(:jar).merge(project('martus-swing').package(:jar))
	package(:jar).merge(project('martus-amplifier').package(:jar))
	package(:jar).merge(project('martus-mspa').package(:jar)).include('**/MSPAServer.class')
	package(:jar).merge(project('martus-mspa').package(:jar)).include('**/RootHelper.class')

	package(:jar).include(artifact(BCPROV_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(ICU4J_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(JAVAX_SERVLET_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(JUNIT_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(LUCENE_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(JETTY_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(PERSIANCALENDAR_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(VELOCITY_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(VELOCITY_DEP_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(XMLRPC_SPEC), :path=>'ThirdPartyJars')
	package(:jar).include(artifact(BCJCE_SPEC), :as=>'ThirdPartyJars/bc-jce.jar')

	# NOTE: Old build script signed this jar
end
