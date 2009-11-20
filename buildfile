define "martus"

repositories.remote << 'http://www.ibiblio.org/maven2/'

if !system("cvs -d:extssh:kevins@cvs.benetech.org/var/local/cvs co martus-bc-jce")
	raise "Unable to check out"
end
if $? != 0
	raise "Error checking out"
end
