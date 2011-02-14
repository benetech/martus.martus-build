name = 'martus-client-mac-dmg'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
	project.version = '1'

	build do
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
	    if(File.exists?(tmpdir)
		    FileUtils::rm_r(tmpdir)
		end
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
	
		FileUtils::cp([_('BuildFiles', 'Documents', 'Mac-install-README.txt')], dmg_contents_dir)
		
		docs_dir = File.join(dmg_contents_dir, "MartusDocumentation")
		FileUtils::mkdir_p(docs_dir)
		readmes = Dir[File.join(production_zip_contents_dir, "*.txt")]
		FileUtils::cp(readmes, docs_dir)
		pdfs = Dir[File.join(production_zip_contents_dir, "Martus/Docs/*.pdf")]
		FileUtils::cp(pdfs, docs_dir)
		
		licenses_dir = File.join(production_zip_contents_dir, "Martus/Docs/Licenses")
		FileUtils::cp_r(licenses_dir, docs_dir)
		
		FileUtils::cp_r(_('BuildFiles', 'Fonts'), dmg_contents_dir)
		dmg_fonts_dir = File.join(dmg_contents_dir, "Fonts")
		dmg_fonts_cvs_dir = File.join(dmg_fonts_dir, "CVS")
		if(File.exists?(dmg_fonts_cvs_dir))
			FileUtils::rm_r(dmg_fonts_cvs_dir)
		end
	
	    buildfile_option = "-buildfile martus-client-mac-dmg.ant.xml"
	    properties = ""
	    properties << " -Dmac.app.name=Martus"
	    properties << " -Dshort.app.name=Martus"
	    properties << " -Dversion.full=#{version}"
	    properties << " -Dversion.timestamp=#{timestamp}"
	    properties << " -Dmain.class=org.martus.client.swingui.Martus"
	
	    properties << " -Dinstaller.mac=BuildFiles/Mac/" #parent of JavaApplicationStub
	    properties << " -Dapp.dir=#{production_zip_contents_dir}"
	    properties << " -Dvm.options=-Xbootclasspath/p:Contents/Resources/Java/LibExt/bc-jce.jar"
	
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
    
end
