name = 'martus-client-linux-zip'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER
	
	puts "Defining package for linux-zip"
#  package(:zip, :file => _("target", "MartusClient-Linux-#{$BUILD_NUMBER}.zip")).include(_("BuildFiles", "Documents", "installing_martus.txt"))
	package(:zip, :file => _("target", "MartusClient-Linux-#{$BUILD_NUMBER}.zip")).tap do | p |
	  puts "Packaging linux zip #{_("BuildFiles")} !"
    p.include(_("BuildFiles", "Documents", "installing_martus.txt"))
    p.include(_("BuildFiles", "Documents", "license.txt"))
    p.include(_("BuildFiles", "Documents", "README*.txt"))
    p.include(_("martus-jar-verifier/*.bat"), :path=>'Verifier')
    p.include(_("martus-jar-verifier/*.txt"), :path=>'Verifier')
    p.include(_("BuildFiles/Windows/Winsock95"), :path=>"Win95")
    p.include(artifact(BCPROV_SPEC), :path=>'LibExt')
    p.include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'LibExt')
    p.include(artifact(JUNIT_SPEC), :path=>'LibExt')
    p.include(artifact(XMLRPC_SPEC), :path=>'LibExt')
    p.include(project('martus-client').package(:sources))
  #TODO: Add docs to Mac/Linux zip
  # zip(zip_name).include(docs)
    p.include(third_party_client_licenses, :path=>'BuildFiles/Documents/Licenses')
    p.include(third_party_client_source, :path=>'SourceFiles')
    p.include(third_party_client_jars, :path=>'ThirdParty')
	end
	
	#TODO: Create SHA-1 of this file
end
