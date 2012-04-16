;The Martus(tm) free, social justice documentation and
;monitoring software. Copyright (C) 2001-2006, Beneficent
;Technology, Inc. (Benetech).

;Martus is free software; you can redistribute it and/or
;modify it under the terms of the GNU General Public License
;as published by the Free Software Foundation; either
;version 2 of the License, or (at your option) any later
;version with the additions and exceptions described in the
;accompanying Martus license file entitled "license.txt".

;It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;IMPLIED, including warranties of fitness of purpose or
;merchantability.  See the accompanying Martus License and
;GPL license for more details on the required license terms
;for this software.

;You should have received a copy of the GNU General Public
;License along with this program; if not, write to the Free
;Software Foundation, Inc., 59 Temple Place - Suite 330,
;Boston, MA 02111-1307, USA.

!define LANG "THAI" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"
!insertmacro LANG_STRING LangDialog_Title "���ҵ�ǵԴ���"
!insertmacro LANG_STRING LangDialog_Text "�ô���͡���ҵ�ǵԴ���"

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} �١�Դ������º��������.\r\n \r\n Visit http://www.martus.org/downloads/ to see if any updated Martus Language Packs are available. \r\n \r\n�ش���� (Language Pack) ������س�Դ��駡�������ҷ����������ѻവ �����͡��÷ء���ҵ����蹢ͧ����� Martus �ش���� (Language Pack) �Ǻ���������������ǹ�����ҹ�ͧ������١���� �����͡����ҹ �����ͩ�Ѻ��� ��� README ��С�ê�������� �ͧ����� Martus ����ѻവ\r\n \r\n���꡻��� ������� �����͡�ҡ�����."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "�س��ͧ������ҧ�ҧ�Ѵ����� Start Menu �ͧ�س�������?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "�س��ͧ���ҧ�ҧ�Ѵ����躹�ʷͻ�ͧ�س�������"
!insertmacro LANG_STRING LaunchProgramInfo_Text "�ҧ�Ѵ Martus �����ҧ���������������� $INSTDIR ��ҧ�Ѵ��� ���ͷ����Ҷ֧ �����Դ Martus"

!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus �к���§ҹ��ҹ�Է������ª� (������)"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "��������ҹ (������)"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_th.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "��������ҹ��Ѻ��� (������)"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_th.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "�ʹ�͹��õԴ���"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "�ٻẺ�����§ҹ�ͧ Martus (������)"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) ��١ź�͡�ҡ����ͧ�ͧ�س����"

!insertmacro LANG_STRING NeedAdminPrivileges_Text "�س��ͧ���Ѻ�Է����繼���������ͧ���֧������ö�Դ�������� $(^Name)��"
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "�Դ�����Դ��Ҵ㹡�õ�Ǩ�ͺ�Է������������ͧ �ô���٨����Ҥس���Է����繼���������ͧ��� ���������ǡ�õԴ�������� $(^Name) �������ö������������"

!insertmacro LANG_STRING UninstallProgramRunning_Text "�ô��Ǩ�ͺ��Ҥس�͡�ҡ����� $(^Name) ���� ���������ǵ�Ƕʹ�͹���������öź�������ѧ��������"

!insertmacro LANG_STRING NewerVersionInstalled_Text "��� ($EXISTING_MARTUS_VERSION) �ͧ ${PRODUCT_NAME} ��������������Դ����������  �س��ͧ�ʹ�͹��õԴ�����蹷��Դ���������ǡ�͹���س������ö�Դ��������ҹ���� ���ҧ�á�����ҤسŴ�дѺ��� �س������˹�ҷ���÷ӧҹ�ҧ���ҧ ����������ö����§ҹ������ҧ����������������  �����ѡ������������ ����ŧ�����͡�ҡ��õԴ��駹��  ��Ҥس��ͧ���Ŵ�дѺ��蹷�駷�������˹�ҷ���÷ӧҹ�ҧ���ҧ �͡�ҡ��õԴ��駹�� �ʹ�͹��������͡��͹ ���Ǩ֧�Դ����������ա����"
!insertmacro LANG_STRING SameVersionInstalled_Text "��� ($EXISTING_MARTUS_VERSION) �ͧ ${PRODUCT_NAME} ����蹷����Դ����������� �س��ͧ��õԴ����ա�����������"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "��� ($EXISTING_MARTUS_VERSION) �ͧ ${PRODUCT_NAME} �������ҡ���.  ��ǵԴ��駨еԴ������ ${PRODUCT_EXTENDED_VERSION} ���"
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "��õԴ�������� ${PRODUCT_NAME} ������ѡ�����ҡ������ͧ�ͧ�س���� ��Ҩо������Դ��Ƕʹ�͹��õԴ����������Ͷʹ�͹���� ��ǵԴ��駻Ѩ�غѹ����������Թ��� ��Ҥس�ѧ��������ͧ�حᨢͧ Martus ��蹻Ѩ�غѹ ����й����س�͡�ҡ��õԴ��駹�� ��зӡ�����ͧ��͹���зӡ�öʹ�͹  �س����ö�Դ��ǵԴ��駹���ա������"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "Martus ��蹷��س�ӡ�õԴ��� ����ö�Ѿ�ô�������ǵԴ�������������� Java ��ҹ��"
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "�س�������öź Martus �������͡�ҡ����ͧ�ͧ�س�� ��ǵԴ��駨��͡����ǹ�� �ô�ʹ�͹ Martus ���� Add/Remove Programs � Control Panel ����Դ��ǵԴ��駹���ա����  ��Ҥس�ѧ�����ӡ�����ͧ�حᨢͧ Martus ������ ����й����س�ӡ�͹��öʹ�͹��õԴ���"
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "�������蹻�Ѻ��ا�ͧ����� Martus ��سҴ�ǹ���Ŵ��еԴ��駵�ǵԴ����������ó����ը����������"

