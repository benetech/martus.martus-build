name = 'martus-api'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
	project.version =$BUILD_NUMBER
	
	jarname = _('target', "martus-api-#{project.version}.jar")
	package(:zip, :file=>jarname).tap do | p |
		p.merge(project('martus-common').package).include('org/common/*.java')
		p.merge(project('martus-common').package).include('org/common/bulletin/*.java')
		p.merge(project('martus-common').package).include('org/common/bulletinstore/*.java')
		p.merge(project('martus-common').package).include('org/common/crypto/*.java')
		p.merge(project('martus-common').package).include('org/common/database/*.java')
		p.merge(project('martus-common').package).include('org/common/field/*.java')
		p.merge(project('martus-common').package).include('org/common/fieldspec/*.java')
		p.merge(project('martus-common').package).include('org/common/network/*.java')
		p.merge(project('martus-common').package).include('org/common/network/mirroring/*.java')
		p.merge(project('martus-common').package).include('org/common/packet/*.java')
		p.merge(project('martus-common').package).include('org/common/utilities/*.java')
		p.merge(project('martus-common').package).include('org/common/xmlrpc/*.java')
		p.merge(project('martus-utils').package).include('org/utils/*.java')
		p.merge(project('martus-utils').package).include('org/utils/inputstreamwithseek/*.java')
		p.merge(project('martus-utils').package).include('org/utils/language/*.java')
		p.merge(project('martus-utils').package).include('org/utils/xml/*.java')
		p.merge(project('martus-clientside').package).include('org/martus/clientside/ClientPortOverride.java')
		p.merge(project('martus-clientside').package).include('org/martus/clientside/ClientSideNetwork*.java')
	end

end
