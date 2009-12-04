def define_martus_thirdparty

	define "martus-thirdparty" do
		task :checkout do
			cvs_checkout("martus-thirdparty")
		end
		
		def bouncycastle_artifact
			bouncycastle_jar_artifact_id = "bouncycastle:bcprov-jdk14:jar:135"
			bouncycastle_jar_file = file(_("libext/BouncyCastle/bin/bcprov-jdk14-135.jar"))
			return artifact(bouncycastle_jar_artifact_id).from(bouncycastle_jar_file)
		end

		def infinitemonkey_jar_artifact
			infinite_monkey_jar_artifact_id = "infinitemonkey:infinitemonkey:jar:1.0"
			infinite_monkey_jar_file = file(_("common/InfiniteMonkey/bin/InfiniteMonkey.jar"))
			return artifact(infinite_monkey_jar_artifact_id).from(infinite_monkey_jar_file)
		end
		
		def infinitemonkey_dll_artifact
			infinite_monkey_dll_artifact_id = "infinitemonkey:infinitemonkey:dll:1.0"
			infinite_monkey_dll_file = file(_("common/InfiniteMonkey/bin/infinitemonkey.dll"))
			return artifact(infinite_monkey_dll_artifact_id).from(infinite_monkey_dll_file)
		end
		
		def persiancalendar_artifact
			persian_calendar_jar_artifact_id = "com.ghasemkiani:persiancalendar:jar:2.1"
			persian_calendar_jar_file = file(_("common/PersianCalendar/bin/persiancalendar.jar"))
			return artifact(persian_calendar_jar_artifact_id).from(persian_calendar_jar_file)
		end
		
		def layouts_artifact
			layouts_jar_artifact_id = "com.jhlabs:layouts:jar:2006-08-10"
			layouts_jar_file = file(_("client/jhlabs/bin/layouts.jar"))
			return artifact(layouts_jar_artifact_id).from(layouts_jar_file)
		end
		
		def rhino_artifact
			js_jar_artifact_id = "org.mozilla.rhino:js:jar:2006-03-08"
			js_jar_file = file(_("client/RhinoJavaScript/bin/js.jar"))
			return artifact(js_jar_artifact_id).from(js_jar_file)
		
		end

		install bouncycastle_artifact
		install infinitemonkey_jar_artifact
		install infinitemonkey_dll_artifact
		install persiancalendar_artifact
		install layouts_artifact
		install rhino_artifact

	end

end
