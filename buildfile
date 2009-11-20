define "martus"

repositories.remote << 'http://www.ibiblio.org/maven2/'

def cvs_checkout(project)
	if !system("cvs -d:extssh:kevins@cvs.benetech.org/var/local/cvs co #{project}")
		raise "Unable to check out #{project}"
	end
	if $? != 0
		raise "Error checking out #{project}"
	end
end

cvs_checkout("martus-bc-jce")

