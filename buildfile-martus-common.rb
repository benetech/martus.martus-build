name = "martus-common"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		'junit:junit:jar:3.8.2',
		"xmlrpc:xmlrpc:jar:1.2-b1",
		"com.ibm.icu:icu4j:jar:3.4.4",
		"com.ghasemkiani:persiancalendar:jar:2.1",
		"bouncycastle:bcprov-jdk14:jar:135",
		'org.martus:martus-logi:jar:1',
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-swing:jar:1'
	)
  
	package :jar
end
