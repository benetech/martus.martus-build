name = 'martus-client-nsis-zip'

define name, :layout=>create_layout_with_source_as_source('.') do
  project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER'] || 'MISSING_PROJECT_VERSION'
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER


  combined_license_file = _('temp', 'combined-license.txt')
  file combined_license_file do
    FileUtils.mkdir_p File.dirname(combined_license_file)
    martus_license = File.readlines(_('martus-build', 'BuildFiles', 'Documents', 'license.txt'))
    gpl = File.readlines(_('martus-build', 'BuildFiles', 'Documents', 'gpl.txt'))
    File.open(combined_license_file, "w") do | out |
      out.write(martus_license)
      out.write("\n\n\t**********************************\n\n")
      out.write(gpl)
    end
  end

  package(:zip).tap do | zip |
    attic_dir = "/var/lib/hudson/martus-client/builds/#{input_build_number}/"
    signed_jar = "#{attic_dir}/martus-client-signed-#{input_build_number}.jar"
    source_zip = "#{attic_dir}/martus-client-sources-#{input_build_number}.zip"
  
    zip.include(signed_jar, :as=>"martus.jar")
    zip.include(source_zip, :path=>'BuildFiles/SourceFiles')
  
    zip.include(_('martus-build', 'BuildFiles', '*.txt'), :path=>'BuildFiles')
  
    include_artifacts(zip, third_party_client_source, 'SourceFiles') 
    
    zip.include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
    zip.include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
    zip.include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
    #TODO: Need to include MartusWin32SetupLauncher?
    zip.include(_('martus-build', 'BuildFiles', 'ProgramFiles'), :path=>'BuildFiles')
    zip.include(_('martus-build', 'BuildFiles', 'SampleDir'), :path=>'BuildFiles')
    #TODO: Need to include MartusSetupLauncher?
  
    include_artifact(zip, artifact(BCJCE_SPEC), 'BuildFiles/Jars', 'bc-jce.jar')
    include_artifacts(zip, third_party_client_jars, 'BuildFiles/Jars') 
    include_artifacts(zip, [_('martus-build', 'BuildFiles', 'JavaRedistributables', 'Win32', 'jre6')], 'BuildFiles/jre6')
    zip.include(_('martus-build', 'BuildFiles', 'Fonts', '*.ttf'), :path=>'BuildFiles/jre6/jre6/lib/fonts/fallback')
    include_artifacts(zip, [_('martus-build', 'BuildFiles', 'Documents')], 'BuildFiles')
    include_artifacts(zip, third_party_client_licenses, 'BuildFiles/Documents/Licenses')
  
    zip.include(combined_license_file, :path=>'BuildFiles')
    
    zip.include(_('martus-build', 'BuildFiles', 'Windows', 'Win32_NSIS'))
    
    zip.include(_('martus-build', 'BuildFiles', 'Fonts'))
  end
end
