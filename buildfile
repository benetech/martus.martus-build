require 'buildfile-martus-thirdparty'

repositories.remote << 'http://www.ibiblio.org/maven2/'

define "martus" do
	define_martus_thirdparty
	
	define "martus-utils", :layout=>create_layout_with_source_as_source do
		project.version = '1'
	
		task :checkout do
			cvs_checkout("martus-utils")
		end

		compile.options.target = '1.5'
		compile.with(
	  		'junit:junit:jar:3.8.2',
	  		'persiancalendar:persiancalendar:jar:2.1',
	  		'com.ibm.icu:icu4j:jar:3.4.4'
		)
	  
		build do
			puts "Building martus-utils"
	  		task('martus:martus-thirdparty:install')
	  	end
	  
		package :jar
	end

	define "martus-bc-jce", :layout=>create_layout_with_source_as_source do
		project.group = 'org.martus'
		project.version = '1'
		jar_file = _('target/martus-bc-jce.jar')
		
		task :checkout do
			cvs_checkout("martus-bc-jce")
		end

		compile.options.target = '1.5'
		compile.with(
			'bouncycastle:bcprov-jdk14:jar:135'
		)
	
		build do
			puts "Building martus-bc-jce"
			task('martus:martus-thirdparty:install')
		end
	
		package :jar, :file=>jar_file

		# isn't there an easier way to ask the project for its artifact?
		jar_artifact_id = "#{project.group}:martus-bc-jce:jar:#{project.version}"
	  	install artifact(jar_artifact_id).from(jar_file)
	
	end

	sub_project_checkouts =	[
		task('martus-thirdparty:checkout'),
		task('martus-bc-jce:checkout'),
		task('martus-utils:checkout')
		]
	task :checkout => sub_project_checkouts do
		puts "checking out"
		
	end

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
