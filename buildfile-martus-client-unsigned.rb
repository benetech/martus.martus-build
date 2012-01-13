name = 'martus-client-unsigned'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	def package_as_unsigned_jar(file_name)
	  Rake::FileTask.define_task(file_name) do
      FileUtils.cp project('martus-client').package(:jar).to_s, file_name
	  end
	end
	
	
	
  package(:unsigned_jar)
end
