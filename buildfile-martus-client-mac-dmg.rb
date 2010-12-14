name = 'martus-client-mac-dmg'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

    `ant -buildfile martus-client-mac-dmg.ant.xml macdmgfile`
    if $CHILD_STATUS != 0
        raise "Failed in dmg ant script"
    end
    
end
