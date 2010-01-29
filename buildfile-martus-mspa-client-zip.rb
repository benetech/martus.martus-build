name = "martus-mspa-client-zip"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	zip_name = _("#{name}/target/MartusMSPA.zip")
	package :zip, :file=>zip_name
	package(:zip).include(artifact(XMLRPC_SPEC))
	package(:zip).include(artifact(PERSIANCALENDAR_SPEC))
	package(:zip).include(artifact(ICU4J_SPEC))
	package(:zip).include(artifact(BCPROV_SPEC))
	package(:zip).include(artifact(LAYOUTS_SPEC))
	package(:zip).include(artifact(INFINITEMONKEY_DLL_SPEC))
	package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC))
	package(:zip).include(project('martus-common').package(:jar))
	package(:zip).include(project('martus-bc-jce').package(:jar))
	package(:zip).include(project('martus-mspa').package(:jar))
	package(:zip).include(_('BuildFiles', 'Documents', 'mspa_user_guide.pdf'))
	#TODO: Should mspa zip include all source code too?
	#TODO: Should mspa zip include license files?
end
