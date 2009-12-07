define "martus-swing", :layout=>create_layout_with_source_as_source do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		'junit:junit:jar:3.8.2',
		'org.martus:martus-utils:jar:1'
	)
  
	build do
		puts "Building martus-swing"
		task('martus-thirdparty:install')
	end
  
	package :jar
end
