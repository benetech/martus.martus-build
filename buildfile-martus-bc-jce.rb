name = 'martus-bc-jce'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER
  jarpath = _(:target, "bc-jce-unsigned.#{project.version}.jar")
	
	compile.options.target = '1.5'
	compile.with(
		BCPROV_SPEC
	)

	package(:jar, :file => jarpath)

  task 'sha1' => package(:jar) do
    sha = create_server_sha1(jarpath)
  end

  task 'sha2' => package(:jar) do
    sha = create_sha2(jarpath)
  end
  
  task 'jar-with-shas' => ['sha1', 'sha2', package(:jar)]
  
	# TODO: Old build script signed this jar

	package (:sources, :file => jarpath)
	
end
