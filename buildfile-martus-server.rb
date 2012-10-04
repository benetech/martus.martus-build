name = "martus-server"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER
  jarpath = _(:target, "martus-server-#{today_as_iso_date}-#{project.version}.jar")

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

  package(:jar, :file => jarpath).tap do | p |
    puts "Packaging server #{p.to_s}"
    p.with :manifest=>manifest.merge('Main-Class'=>'org.martus.server.main.MartusServer')

    p.merge(project('martus-jar-verifier').package(:jar))
    p.merge(project('martus-common').package(:jar))
    p.merge(project('martus-utils').package(:jar))
    p.merge(project('martus-hrdag').package(:jar))
    p.merge(project('martus-logi').package(:jar))
    p.merge(project('martus-swing').package(:jar))
    p.merge(project('martus-amplifier').package(:jar))
    p.merge(project('martus-mspa').package(:jar)).include('**/MSPAServer.class')
    p.merge(project('martus-mspa').package(:jar)).include('**/RootHelper.class')

    p.include(artifact(BCPROV_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(ICU4J_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(JAVAX_SERVLET_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(JUNIT_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(LUCENE_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(JETTY_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(PERSIANCALENDAR_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(VELOCITY_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(VELOCITY_DEP_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(XMLRPC_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(BCJCE_SPEC), :as=>'ThirdPartyJars/bc-jce.jar')
  end
  
  sha1path = "#{jarpath}.sha1"
  task 'sha1' => jarpath do
    create_sha1(jarpath)
  end

  sha2path = "#{jarpath}.sha2"
  task 'sha2' => jarpath do
    create_sha2(jarpath)
  end
  
  task 'everything' => [package(:jar), 'sha1', 'sha2']
  
	# NOTE: Old build script signed this jar
end
