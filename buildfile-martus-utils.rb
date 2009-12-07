define "martus-utils", :layout=>create_layout_with_source_as_source do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		'junit:junit:jar:3.8.2',
		'persiancalendar:persiancalendar:jar:2.1',
		'com.ibm.icu:icu4j:jar:3.4.4'
	)
  
	build do
		puts "Building martus-utils"
		task('martus-thirdparty:install')
	end
  
	package :jar
end
