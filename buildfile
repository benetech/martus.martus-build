require 'buildfile-martus-thirdparty'
require 'buildfile-martus-utils'
require 'buildfile-martus-bc-jce'

repositories.remote << 'http://www.ibiblio.org/maven2/'

define "martus" do
	define_martus_thirdparty
	define_martus_utils
	define_martus_bc_jce
	
	task :checkout => sub_project_checkouts(self) do
		puts "checking out"
		
	end

end

def sub_project_checkouts(project)
	checkout_tasks = []
	project.projects.each do | sub_project |
		name = sub_project.name
		name[project.name] = ''
		task = task("#{name}:checkout")
		checkout_tasks << task
	end
	return checkout_tasks
end

def create_layout_with_source_as_source
	layout = Layout.new
	layout[:source, :main, :java] = 'source'
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
