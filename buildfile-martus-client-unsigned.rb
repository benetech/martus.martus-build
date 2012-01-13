name = 'martus-client-unsigned'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

  package(:zip).merge(project('martus-client').package(:jar))
end
