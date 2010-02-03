name = "martus-mspa-client-zip"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	zip_name = _("#{name}/target/MartusMSPA.zip")
	zip(zip_name)
	zip(zip_name).include(artifact(XMLRPC_SPEC))
	zip(zip_name).include(artifact(PERSIANCALENDAR_SPEC))
	zip(zip_name).include(artifact(ICU4J_SPEC))
	zip(zip_name).include(artifact(BCPROV_SPEC))
	zip(zip_name).include(artifact(LAYOUTS_SPEC))
	zip(zip_name).include(artifact(INFINITEMONKEY_DLL_SPEC))
	zip(zip_name).include(artifact(INFINITEMONKEY_JAR_SPEC))
	zip(zip_name).include(project('martus-common').package(:jar))
	zip(zip_name).include(project('martus-bc-jce').package(:jar))
	zip(zip_name).include(project('martus-mspa').package(:jar))
	zip(zip_name).include(_('BuildFiles', 'Documents', 'mspa_user_guide.pdf'))
	zip(zip_name).include(project('martus-mspa').package(:sources))
	#TODO: mspa zip should include all third-party source code
	#TODO: mspa zip should include martus and third-party license files

	build(zip_name)
end
