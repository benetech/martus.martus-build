repositories.remote << 'http://www.ibiblio.org/maven2/'
repositories.remote << 'http://ftp.cica.es/mirrors/maven2'


def create_layout_with_source_as_source(base)
	layout = Layout.new
	layout[:source, :main, :java] = "#{base}/source"
	layout[:source, :test, :java] = "#{base}/source"
	layout[:target] = "#{base}/target"
	layout[:target, :main, :classes] = "#{base}/target/main/classes"
	layout[:target, :test, :classes] = "#{base}/target/test/classes"
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

task :checkout do
	cvs_checkout 'martus-thirdparty'
	cvs_checkout 'martus-bc-jce'
	cvs_checkout 'martus-logi'
	cvs_checkout 'martus-hrdag'
	cvs_checkout 'martus-utils'
	cvs_checkout 'martus-swing'
	cvs_checkout 'martus-common'
	cvs_checkout 'martus-jar-verifier'
	cvs_checkout 'martus-clientside'
	cvs_checkout 'martus-client'
end

define 'martus' do
	compile.with [
		project('martus-common'),
	]
end
	

require 'buildfile-martus-thirdparty'
require 'buildfile-martus-bc-jce'
require 'buildfile-martus-logi'
require 'buildfile-martus-hrdag'
require 'buildfile-martus-utils'
require 'buildfile-martus-swing'
require 'buildfile-martus-common'
require 'buildfile-martus-jar-verifier'
require 'buildfile-martus-clientside'
require 'buildfile-martus-client'

