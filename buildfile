repositories.remote << 'http://www.ibiblio.org/maven2/'

def create_layout_with_source_as_source
	layout = Layout.new
	layout[:source, :main, :java] = 'source'
	layout[:source, :test, :java] = 'source'
	return layout
end

def cvs_checkout(project)
	if !system("cvs -d:extssh:kevins@cvs.benetech.org/var/local/cvs co #{project}")
		raise "Unable to check out #{project}"
	end
	if $? != 0
		raise "Error checking out #{project}"
	end
end

task nil do
end

task :checkout => [
		'martus-thirdparty:checkout',
		'martus-bc-jce:checkout',
		'martus-utils:checkout',
		'martus-swing:checkout',
		nil
	] do
		puts "checkout"
end

require 'buildfile-martus-thirdparty'
require 'buildfile-martus-bc-jce'
require 'buildfile-martus-utils'
require 'buildfile-martus-swing'

