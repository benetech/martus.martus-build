name = 'martus-client-mac-dmg'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

    version = "3.5.1"
    timestamp = "20101116.1964"
    hudson_job_dir = "/var/lib/hudson/jobs/martus-client-unsigned"
    dmg_mount_point = File.join(hudson_job_dir, "mounts/dmg")
    dmg_file = File.join(hudson_job_dir, "Martus.dmg")
    production_zipfile_dir = File.join(hudson_job_dir, "artifacts")
    production_zip_name = "/MartusClient-#{version}-#{timestamp}-MacLinux.zip"
    production_zipfile = File.join(production_zipfile_dir, production_zip_name)

    tmpdir = File.join(_('dist', 'mactree')) #was Dir.mktmpdir
    puts "Using temp dir: #{tmpdir}"
    FileUtils::rm_r(tmpdir)
    FileUtils::mkdir_p(tmpdir)

    dmg_contents_dir = File.join(tmpdir, "dmgcontents")
    raw_production_zip_contents_dir = File.join(tmpdir, "production")
    FileUtils::mkdir_p(dmg_contents_dir)
    FileUtils::mkdir_p(raw_production_zip_contents_dir)

    unzip_file(production_zipfile, raw_production_zip_contents_dir)
    production_zip_contents_dir = File.join(raw_production_zip_contents_dir, "MartusClient-#{version}")
#puts production_zip_contents_dir
#puts "press enter"
#$stdin.gets

	other_files_dir = File.join(dmg_contents_dir, "Documents")
	FileUtils::mkdir_p(other_files_dir)
	docs = Dir["#{production_zip_contents_dir}/*.txt"]
	FileUtils::cp(docs, other_files_dir)
	
	extensions_dir = File.join(dmg_contents_dir, "Extensions")
	FileUtils::mkdir_p(extensions_dir)
	libext_dir = File.join(production_zip_contents_dir, "LibExt")
puts "Moving #{File.join(libext_dir, 'bc-jce.jar')}, #{extensions_dir}"
	FileUtils::mv(File.join(libext_dir, 'bc-jce.jar'), extensions_dir)

	FileUtils::cp([_('BuildFiles', 'Documents', 'README.mac')], "#{other_files_dir}")
	FileUtils::mv(File.join(other_files_dir, 'README.mac'), dmg_contents_dir)
	exit 1
	
    buildfile_option = "-buildfile martus-client-mac-dmg.ant.xml"
    properties = ""
    properties << " -Dmac.app.name=Martus"
    properties << " -Dshort.app.name=Martus"
    properties << " -Dversion.full=#{version}"
    properties << " -Dversion.timestamp=#{timestamp}"
    properties << " -Dmain.class=org.martus.client.swingui.Martus"

    properties << " -Dinstaller.mac=BuildFiles/Mac/" #parent of JavaApplicationStub
    properties << " -Dapp.dir=#{production_zip_contents_dir}"
    properties << " -Dvm.options=-Xbootclasspath/p:/Library/Java/Extensions/bc-jce.jar"

    properties << " -Ddist.mactree=#{dmg_contents_dir}" #can be temp
    properties << " -Ddmg.dest.dir=#{_('dist')}"
    properties << " -Drawdmgfile=#{dmg_file}"
    properties << " -Ddmgmount=#{dmg_mount_point}"
    properties << " -Ddmg.size.megs=40"

    ant = "/opt/java/tools/ant/bin/ant #{buildfile_option} macdmgfile #{properties}"
puts ant
    `#{ant}`
    if $CHILD_STATUS != 0
        raise "Failed in dmg ant script #{$CHILD_STATUS}"
    end
    
end
