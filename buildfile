repositories.remote << 'http://www.ibiblio.org/maven2/'

define "martus" do
	define "martus-thirdparty" do
		clean do
			cvs_checkout("martus-thirdparty")
		end
	end

	define "martus-utils"
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


