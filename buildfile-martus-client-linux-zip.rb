name = 'martus-client-linux-zip'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
	
	puts "Defining package for linux-zip #{project.version} #{input_build_number}"
	zippath = _("target", "MartusClient-Linux-#{project.version}-#{input_build_number}.zip")
	package(:zip, :file => zippath).path("MartusClient-#{project.version}").tap do | p |
	  input_dir = "/var/lib/hudson/martus-client/builds/#{input_build_number}"
	  signed_jar = "#{input_dir}/martus-client-signed-#{input_build_number}.jar"
	  source_zip_name = "martus-client-sources-#{input_build_number}.zip"
    source_zip = "#{input_dir}/#{source_zip_name}"
	  p.include(signed_jar, :as=>"martus.jar")
    p.include(source_zip, :as=>"SourceFiles/martus-sources.zip")

	  p.include(_("martus", "BuildFiles", "Documents", "installing_martus.txt"))
    p.include(_("martus", "BuildFiles", "Documents", "license.txt"))
    p.include(_("martus", "BuildFiles", "Documents", "README*.txt"))
    p.include(_("martus", "BuildFiles", "Windows", "Winsock95"), :path=>"Win95")
    
    p.include(_("martus-jar-verifier/*.bat"), :path=>'Verifier')
    p.include(_("martus-jar-verifier/*.txt"), :path=>'Verifier')
    
    p.include(artifact(BCPROV_SPEC), :path=>'ThirdParty')
    p.include(artifact(INFINITEMONKEY_JAR_SPEC), :path=>'ThirdParty')
    p.include(artifact(JUNIT_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_SPEC), :path=>'ThirdParty')
    p.include(third_party_client_jars, :path=>'ThirdParty')
    p.include(third_party_client_licenses, :path=>'Documents/Licenses')
    p.include(third_party_client_source, :path=>'SourceFiles')

    #TODO: Add docs to Mac/Linux zip
	end
	
	sha1path = "#{zippath}.sha1"
	task 'sha1' => zippath do
	  create_sha1(zippath)
	end

  sha2path = "#{zippath}.sha2"
  task 'sha2' => zippath do
    create_sha2(zippath)
  end
end
