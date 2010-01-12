name = 'martus-client-linux-zip'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'
	
	zip_name = _("target/MartusClient-#{project.version}-MacLinux.zip")
	package :zip, :file=>zip_name
	#TODO: Do we really want to include Java for Linux here??
	package(:zip).include(_("BuildFiles/JavaRedistributables/Linux"), :path=>'Java')
	package(:zip).include(_("BuildFiles/Documents/installing_martus.txt"))
	package(:zip).include(_("BuildFiles/Documents/license.txt"))
	package(:zip).include(_("BuildFiles/Documents/README*.txt"))
	package(:zip).include(_("martus-jar-verifier/*.bat"), :path=>'Verifier')
	package(:zip).include(_("martus-jar-verifier/*.txt"), :path=>'Verifier')
	package(:zip).include(_("BuildFiles/Windows/Winsock95"), :path=>"Win95")
	package(:zip).include(artifact(BCPROV_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(JUNIT_SPEC), :path=>'LibExt')
	package(:zip).include(artifact(XMLRPC_SPEC), :path=>'LibExt')
#TODO: Add files to Mac/Linux zip
#	package(:zip).include(client source zip)
#	package(:zip).include(docs)
#	package(:zip).include(licenses)
	

end
