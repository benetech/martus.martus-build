name = 'martus-client-unsigned'

def extract_sig_file_to_crypto(jar_artifact, base_filename)
  result = FileUtils.mkdir_p crypto_dir
  puts "Created #{crypto_dir} result=#{result} exists=#{File.exists?(crypto_dir)}"

  sf_file = File.join(main_target_dir, "META-INF/#{base_filename}.SF")
  FileUtils.rm_f sf_file
  unzip_one_entry(jar_artifact, "META-INF/#{base_filename}.SF", main_target_dir)

  sig_file = sig_file(base_filename)
  FileUtils.rm_f sig_file
  FileUtils.move(sf_file, sig_file)
  puts "Moved #{sf_file} (#{File.exists?(sf_file)}) to #{sig_file}"
  return sig_file
end

def main_source_dir
  return _('source', 'main', 'java')
end

def main_target_dir
  return _('target', 'main', 'classes')
end

def test_source_dir
  return _('source', 'test', 'java')
end

def test_target_dir
  return _('target', 'test', 'classes')
end

def crypto_dir
  return _(main_target_dir, 'org', 'martus', 'common', 'crypto')
end

def sig_file(base_filename)
  return File.join(crypto_dir, "#{base_filename}.SIG")
end

def bcjce_sig_file
  return sig_file("SSMTSJAR")
end

def bcprov_sig_file
  return sig_file("BCKEY")
end

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

  file bcjce_sig_file => project('martus-thirdparty') do
    extract_sig_file_to_crypto(artifact(BCJCE_SPEC), "SSMTSJAR")
  end
  
  file bcprov_sig_file => project('martus-thirdparty') do
    extract_sig_file_to_crypto(artifact(BCPROV_SPEC), "BCKEY")
  end
		
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
