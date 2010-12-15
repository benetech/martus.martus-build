name = 'martus-client-mac-dmg'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

    tmpdir = Dir.mktmpdir
    puts "Using temp dir: #{tmpdir}"

    buildfile_option = "-buildfile martus-client-mac-dmg.ant.xml"
    properties = ""
    properties << " -Dmac.app.name=Martus"
    properties << " -Dshort.app.name=Martus"
    properties << " -Dversion.full=3.5.1"
    properties << " -Dversion.timestamp="

    properties << " -Ddist.mactree=#{tmpdir}" #can be temp
    properties << " -Ddmg.dest.dir=/home/kevins/"
    properties << " -Drawdmgfile=/home/kevins/Martus.dmg"
    properties << " -Ddmgmount=/mounts/Martus/dmgfile"
    properties << " -Ddmg.size.megs=10"

    properties << " -Dinstaller.mac=BuildFiles/Mac/" #parent of JavaApplicationStub
    properties << " -Dapp.jar.name=martus-client-20090826.1911.jar"
    properties << " -Dapp.jar.dir=/home/kevins/work/martus/martus/0826-client/"
    properties << " -Dthirdparty.jars.dir=/home/kevins/work/martus/martus/martus-thirdparty/"
    properties << " -Dvm.options=-Xmx512m"

    ant = "ant #{buildfile_option} macdmgfile #{properties}"
    `#{ant}`
    if $CHILD_STATUS != 0
        raise "Failed in dmg ant script #{$CHILD_STATUS}"
    end
    
end
