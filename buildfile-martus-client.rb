name = 'martus-client'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	compile.options.target = '1.5'
	compile.with(
		'junit:junit:jar:3.8.2',
		'org.martus:martus-utils:jar:1',
		'org.martus:martus-common:jar:1',
		'org.martus:martus-swing:jar:1',
		'org.martus:martus-clientside:jar:1',
		'com.jhlabs:layouts:jar:2006-08-10',
		'org.martus:martus-jar-verifier:jar:1',
		'velocity:velocity:jar:1.4'
	)

  
	package(:jar).include(project('martus-jar-verifier').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-common').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-utils').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-hrdag').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-logi').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-swing').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-clientside').path_to('target', 'main', 'classes'), :as=>'.')
	package(:jar).include(project('martus-js-xml-generator').path_to('target', 'main', 'classes'), :as=>'.')

end
