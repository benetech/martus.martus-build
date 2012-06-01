name = "martus-common"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

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
	)
	bc_jce = artifact(BCJCE_SPEC)
	test.using :java_args => "-Xbootclasspath/a:#{bc_jce}"

	#TODO: Failing test
	test.exclude 'org.martus.common.test.TestMartusSecurity'

  task 'insert_build_version' do
    version_file = _('martus-common', 'source', 'org', 'martus', 'common', 'VersionBuildDate.java')
    date = Time.now.strftime('%F')  # %F is ISO date YYYY-MM-DD
    contents = File.read(version_file)
    contents.gsub!('#{BUILDDATE}', "#{date}.#{$BUILD_NUMBER}")
    File.open(version_file, "w") do | f |
      f.write contents
    end
  end
  
	build ('insert_build_version') 
	package :jar

	# NOTE: Old build script signed this jar

	package :sources
end
