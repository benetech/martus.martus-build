name = "martus-server"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER
  date = today_as_iso_date.gsub(/-/, '')
  jarpath = _(:target, "martus-server-#{date}.#{project.version}.jar")

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
    p.include(artifact(VELOCITY_DEP_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(XMLRPC_SPEC), :path=>'ThirdPartyJars')
    p.include(artifact(BCJCE_SPEC), :as=>'ThirdPartyJars/bc-jce.jar')
  end
  
  task 'sha1' => package(:jar) do
    sha = create_server_sha1(jarpath)
    sha_dest_dir = get_sha_dest_dir
    FileUtils::cp(sha, File.join(sha_dest_dir, File::basename(sha)))
  end

  task 'sha2' => package(:jar) do
    sha = create_sha2(jarpath)
    sha_dest_dir = get_sha_dest_dir
    FileUtils::cp(sha, File.join(sha_dest_dir, File::basename(sha)))
  end
  
  def get_sha_dest_dir
    now = today_as_iso_date
    year = now[0,4]
    month = now[5,2]
    day = now[8,2]
    year_dir = File.join(sha_root_dir, year)
    month_dir = File.join(year_dir, month)
    day_dir = File.join(month_dir, day)
    puts "Creating sha dir: #{day_dir}"
    FileUtils.mkdir_p(day_dir)
    return day_dir
  end
  
  task 'push-sha-files' => ['sha1', 'sha2'] do
    cmd = "hg -R #{sha_root_dir} add -S #{sha_root_dir}/."
    puts cmd
    result = `#{cmd} 2>&1`
    if $? != 0
      raise "Error adding new sha files to hg: #{cmd}\n#{result}"
    end

    cmd = "hg -R #{sha_root_dir} commit -m'New sha files from build #{project.version}'"
    puts cmd
    result = `#{cmd} 2>&1`
    if $? != 0
      raise "Error committing new sha files to hg: #{cmd}\n#{result}"
    end

    cmd = "hg -R #{sha_root_dir} push"
    puts cmd
    result = `#{cmd} 2>&1`
    if $? != 0
      raise "Error pushing new sha files to hg: #{cmd}\n#{result}"
    end
  end
  
  def sha_root_dir 
    return File.join(ENV['WORKSPACE'], 'martus-sha')
  end

  task 'everything' => [package(:jar), 'push-sha-files']
  
	# NOTE: Old build script signed this jar
end
