name = 'martus-client-nsis-upgrade'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	package(:zip).include(project('martus-client').package(:jar))

	update_packaged_zip(package(:zip)) do | filespec |
		#TODO: Need to unzip contents, then run makensis
		puts "Ready to unzip #{filespec}"
	end
end

