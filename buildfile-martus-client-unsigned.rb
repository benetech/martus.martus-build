name = 'martus-client-unsigned'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	def package_as_unsigned_jar(file_name)
	  file file_name => project('martus-client').package(:jar) do
	    FileUtils.mkdir_p(_('target'))
      FileUtils.cp project('martus-client').package(:jar).to_s, file_name
	  end
	end
	
#  package(:unsigned_jar)
	
   package(:jar).with :manifest=>manifest.merge('Main-Class'=>'org.martus.client.swingui.Martus')
   package(:jar).include(bcjce_sig_file)
   package(:jar).include(bcprov_sig_file)
 
   package(:jar).include(File.join(_('source', 'test', 'java'), '**/*.mlp'))
   package(:jar).merge(project('martus-jar-verifier').package(:jar))
   package(:jar).merge(project('martus-common').package(:jar))
   package(:jar).merge(project('martus-utils').package(:jar))
   package(:jar).merge(project('martus-hrdag').package(:jar))
   package(:jar).merge(project('martus-logi').package(:jar))
   package(:jar).merge(project('martus-swing').package(:jar))
   package(:jar).merge(project('martus-clientside').package(:jar))
   package(:jar).merge(project('martus-js-xml-generator').package(:jar))

end
