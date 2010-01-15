define "martus-thirdparty" do
	def bouncycastle_artifact
		bouncycastle_jar_artifact_id = BCPROV_SPEC
		bouncycastle_jar_file = file(_(name, "libext/BouncyCastle/bin/bcprov-jdk14-135.jar"))
		return artifact(bouncycastle_jar_artifact_id).from(bouncycastle_jar_file)
	end

	def bouncycastle_license_artifact
		bouncycastle_jar_artifact_id = BCPROV_LICENSE_SPEC
		bouncycastle_jar_file = file(_(name, "libext/BouncyCastle/license/LICENSE.html"))
		return artifact(bouncycastle_jar_artifact_id).from(bouncycastle_jar_file)
	end

	def infinitemonkey_jar_artifact
		infinite_monkey_jar_artifact_id = INFINITEMONKEY_JAR_SPEC
		infinite_monkey_jar_file = file(_(name, "common/InfiniteMonkey/bin/InfiniteMonkey.jar"))
		return artifact(infinite_monkey_jar_artifact_id).from(infinite_monkey_jar_file)
	end
	
	def infinitemonkey_dll_artifact
		infinite_monkey_dll_artifact_id = INFINITEMONKEY_DLL_SPEC
		infinite_monkey_dll_file = file(_(name, "common/InfiniteMonkey/bin/infinitemonkey.dll"))
		return artifact(infinite_monkey_dll_artifact_id).from(infinite_monkey_dll_file)
	end
	
	def persiancalendar_artifact
		persian_calendar_jar_artifact_id = PERSIANCALENDAR_SPEC
		persian_calendar_jar_file = file(_(name, "common/PersianCalendar/bin/persiancalendar.jar"))
		return artifact(persian_calendar_jar_artifact_id).from(persian_calendar_jar_file)
	end
	
	def layouts_artifact
		layouts_jar_artifact_id = LAYOUTS_SPEC
		layouts_jar_file = file(_(name, "client/jhlabs/bin/layouts.jar"))
		return artifact(layouts_jar_artifact_id).from(layouts_jar_file)
	end
	
	def rhino_artifact
		js_jar_artifact_id = RHINO_SPEC
		js_jar_file = file(_(name, "client/RhinoJavaScript/bin/js.jar"))
		return artifact(js_jar_artifact_id).from(js_jar_file)
	
	end

	install do
		puts "Installing martus-thirdparty"
	end
	
	install bouncycastle_artifact
	install bouncycastle_license_artifact
	install infinitemonkey_jar_artifact
	install infinitemonkey_dll_artifact
	install persiancalendar_artifact
	install layouts_artifact
	install rhino_artifact

end
