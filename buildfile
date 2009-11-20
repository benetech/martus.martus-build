repositories.remote << 'http://www.ibiblio.org/maven2/'

define "martus" do
	define "martus-thirdparty" do
		clean do
			cvs_checkout("martus-thirdparty")
		end

		def install_common_artifacts
			infinite_monkey_jar_artifact_id = "infinitemonkey:infinitemonkey:jar:1.0"
			infinite_monkey_jar_file = file(_("common/InfiniteMonkey/bin/InfiniteMonkey.jar"))
			install artifact(infinite_monkey_jar_artifact_id).from(infinite_monkey_jar_file)
		
			infinite_monkey_dll_artifact_id = "infinitemonkey:infinitemonkey:dll:1.0"
			infinite_monkey_dll_file = file(_("common/InfiniteMonkey/bin/infinitemonkey.dll"))
			install artifact(infinite_monkey_dll_artifact_id).from(infinite_monkey_dll_file)
		
			persian_calendar_jar_artifact_id = "com.ghasemkiani:persiancalendar:jar:2.1"
			persian_calendar_jar_file = file(_("common/PersianCalendar/bin/persiancalendar.jar"))
			install artifact(persian_calendar_jar_artifact_id).from(persian_calendar_jar_file)
		end
		
		def install_client_artifacts
			layouts_jar_artifact_id = "com.jhlabs:layouts:jar:2006-08-10"
			layouts_jar_file = file(_("client/jhlabs/bin/layouts.jar"))
			install artifact(layouts_jar_artifact_id).from(layouts_jar_file)
		
			js_jar_artifact_id = "org.mozilla.rhino:js:jar:2006-03-08"
			js_jar_file = file(_("client/RhinoJavaScript/bin/js.jar"))
			install artifact(js_jar_artifact_id).from(js_jar_file)
		
		end

		install_common_artifacts
		install_client_artifacts

	end

	martus_utils_layout = Layout.new
	martus_utils_layout[:source, :main, :java] = 'source'
	define "martus-utils", :layout=>martus_utils_layout do
	  project.version = '1'
	
	  compile.options.target = '1.5'
	  compile.with(
	  	'junit:junit:jar:3.8.2',
	  	'persiancalendar:persiancalendar:jar:2.1',
	  	'com.ibm.icu:icu4j:jar:3.4.4'
	  )
	
	  package :jar
	end

	define "martus-bc-jce"

	clean do
		cvs_checkout("martus-utils")
		cvs_checkout("martus-bc-jce")
	end

end


def cvs_checkout(project)
	if !system("cvs -d:extssh:kevins@cvs.benetech.org/var/local/cvs co #{project}")
		raise "Unable to check out #{project}"
	end
	if $? != 0
		raise "Error checking out #{project}"
	end
end
