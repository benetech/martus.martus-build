name = 'martus-client-nsis-upgrade'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	package(:zip).include(project('martus-client').package(:jar))
	#TODO: need to zip up everything else that goes into the nsis upgrade installer

	update_packaged_zip(package(:zip)) do | filespec |
		dest_dir = File.join(File.dirname(filespec), 'temp')
		Dir.mkdir(dest_dir)
		unzip_file(filespec, dest_dir)
		#TODO: Need to run makensis in that directory
	end
end

