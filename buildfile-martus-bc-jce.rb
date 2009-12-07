define "martus-bc-jce", :layout=>create_layout_with_source_as_source do
	project.group = 'org.martus'
	project.version = '1'
	jar_file = _('target/martus-bc-jce.jar')
	
	task :checkout do
		cvs_checkout("martus-bc-jce")
	end

	compile.options.target = '1.5'
	compile.with(
		'bouncycastle:bcprov-jdk14:jar:135'
	)

	build do
		puts "Building martus-bc-jce"
		task('martus-thirdparty:install')
	end

	package :jar, :file=>jar_file

	# isn't there an easier way to ask the project for its artifact?
	jar_artifact_id = "#{project.group}:martus-bc-jce:jar:#{project.version}"
	install artifact(jar_artifact_id).from(jar_file)

end
