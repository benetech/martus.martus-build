name = 'martus-client-mac-dmg'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']

	build do
    hudson_job_dir = "/var/lib/hudson/jobs/martus-client-unsigned"
    dmg_mount_point = File.join(hudson_job_dir, "mounts/dmg")
    dmg_file = File.join(hudson_job_dir, "Martus.dmg")
    production_zipfile = project('martus-client-linux-zip').package.to_s

    mactree_dir = create_empty_mactree_directory

    raw_production_zip_contents_dir = File.join(mactree_dir, "production")
    FileUtils::mkdir_p(raw_production_zip_contents_dir)
    unzip_file(production_zipfile, raw_production_zip_contents_dir)
    production_zip_contents_dir = File.join(raw_production_zip_contents_dir, "MartusClient-#{project.version}")
	
    dmg_contents_dir = File.join(mactree_dir, "dmgcontents")
    FileUtils::mkdir_p(dmg_contents_dir)
    
    # COPY FILES FROM THE PRODUCTION ZIP
    # NOTE: The jars themselves are copied by jarbundler
    docs_dir = File.join(dmg_contents_dir, "MartusDocumentation")
    FileUtils::mkdir_p(docs_dir)
    readmes = Dir[File.join(production_zip_contents_dir, "*.txt")]
    FileUtils::cp(readmes, docs_dir)
    pdfs = Dir[File.join(production_zip_contents_dir, "Documents/*.pdf")]
    FileUtils::cp(pdfs, docs_dir)
    
    licenses_dir = File.join(production_zip_contents_dir, "ThirdParty/Licenses")
    FileUtils::cp_r(licenses_dir, docs_dir)
    
    # COPY MAC-SPECIFIC FILES NOT IN THE ZIP
    mac_readme = _("martus", 'BuildFiles', 'Documents', 'Mac-install-README.txt')
		FileUtils::cp([mac_readme], dmg_contents_dir)

		# NOTE: This does not appear to be working. We need to learn more 
		# about mac app icons before spending more time on it.
		mac_icon_file = _("martus", 'BuildFiles', 'ProgramFiles', 'Martus-Mac')

    # TODO: Shouldn't the fonts be in the zip??
    FileUtils::cp_r(_("martus", 'BuildFiles', 'Fonts'), dmg_contents_dir)
    dmg_fonts_dir = File.join(dmg_contents_dir, "Fonts")
    dmg_fonts_cvs_dir = File.join(dmg_fonts_dir, "CVS")
    if(File.exists?(dmg_fonts_cvs_dir))
      FileUtils::rm_r(dmg_fonts_cvs_dir)
    end

    buildfile_option = "-buildfile martus/martus-client-mac-dmg.ant.xml"
    properties = ""
    properties << " -Dmac.app.name=Martus"
    properties << " -Dshort.app.name=Martus"
    properties << " -Dversion.full=#{version}"
    properties << " -Dversion.build=#{input_build_number}"
    properties << " -Dmain.class=org.martus.client.swingui.Martus"
    properties << " -Dmac.icon.file=#{mac_icon_file}"

    properties << " -Dinstaller.mac=BuildFiles/Mac/" #parent of JavaApplicationStub
    properties << " -Dapp.dir=#{production_zip_contents_dir}"
    properties << " -Dvm.options=-Xbootclasspath/p:Contents/Resources/Java/LibExt/bc-jce.jar"

    properties << " -Ddist.mactree=#{dmg_contents_dir}" #can be temp
    properties << " -Ddmg.dest.dir=#{_('dist')}"
    properties << " -Drawdmgfile=#{dmg_file}"
    properties << " -Ddmgmount=#{dmg_mount_point}"
    properties << " -Ddmg.size.megs=40"
	
    ant = "ant #{buildfile_option} macdmgfile #{properties}"
    puts `#{ant}`
    if $CHILD_STATUS != 0
      raise "Failed in dmg ant script #{$CHILD_STATUS}"
    end
    
    destination_filename = "MartusClient-#{project.version}-#{input_build_number}.dmg"
    destination = _(:target, destination_filename)
    FileUtils.cp dmg_file, destination
    create_sha_files(destination)
	end

	def create_empty_mactree_directory
    mactree_dir = File.join(_('dist', 'mactree')) #was Dir.mktmpdir
    puts "Using temp dir: #{mactree_dir}"
    if(File.exists?(mactree_dir))
      FileUtils::rm_r(mactree_dir)
    end
    FileUtils::mkdir_p(mactree_dir)
    
    return mactree_dir
	end
end
