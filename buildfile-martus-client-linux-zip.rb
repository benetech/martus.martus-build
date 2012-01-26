name = 'martus-client-linux-zip'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
	
	puts "Defining package for linux-zip #{project.version} #{input_build_number}"
	package(:zip, :file => _("target", "MartusClient-Linux-#{project.version}.zip")).tap do | p |
	  signed_jar = "/var/lib/hudson/jobs/martus-client/martus-client-signed-#{input_build_number}.jar"
	  p.include(signed_jar, :as=>"martus.jar")

	  p.include(_("martus", "BuildFiles", "Documents", "installing_martus.txt"))
    p.include(_("martus", "BuildFiles", "Documents", "license.txt"))
    p.include(_("martus", "BuildFiles", "Documents", "README*.txt"))
    p.include(_("martus", "BuildFiles", "Windows", "Winsock95"), :path=>"Win95")
    
    p.include(_("martus-jar-verifier/*.bat"), :path=>'Verifier')
    p.include(_("martus-jar-verifier/*.txt"), :path=>'Verifier')
    
    #TODO: Add docs to Mac/Linux zip
    p.include(artifact(BCPROV_SPEC), :path=>'ThirdParty')
    p.include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'ThirdParty')
    p.include(artifact(JUNIT_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_SPEC), :path=>'ThirdParty')
    p.include(third_party_client_jars, :path=>'ThirdParty')
    p.include(third_party_client_licenses, :path=>'BuildFiles/Documents/Licenses')

    source_zip = _("martus-client", "target", "martus-client-sources-#{input_build_number}.zip")
	  p.include(source_zip, :as=>"SourceFiles/martus-sources.zip")
    p.include(third_party_client_source, :path=>'SourceFiles')
	end
	
	#TODO: Create SHA-1 of this file
end
