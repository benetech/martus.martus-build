def define_martus_thirdparty

	define "martus-thirdparty" do
		task :checkout do
			cvs_checkout("martus-thirdparty")
		end
		
		def install_libext_artifacts
			bouncycastle_jar_artifact_id = "bouncycastle:bcprov-jdk14:jar:135"
			bouncycastle_jar_file = file(_("libext/BouncyCastle/bin/bcprov-jdk14-135.jar"))
			install artifact(bouncycastle_jar_artifact_id).from(bouncycastle_jar_file)
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

		install_libext_artifacts
		install_common_artifacts
		install_client_artifacts

	end

end
