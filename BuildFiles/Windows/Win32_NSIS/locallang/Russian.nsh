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

!define LANG "RUSSIAN" ; Required  
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"
!insertmacro LANG_STRING LangDialog_Title "���� ����������"
!insertmacro LANG_STRING LangDialog_Text "�������� ���� ���������� ��������� ���������."

!insertmacro LANG_STRING FinishDialog_Text "��������� ${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} ���������.\r\n \r\n ��������� ����������� �������������� ������ ������������ Martus � �����: http://www.martus.org/downloads. \r\n \r\nA 'Language Pack' allows you to install new and updated translations or documentation at any time following a full Martus release. Language Packs can contain updates to the Martus Client User Interface translation, the User Guide, Quick Start Guide, README file, and in-program help.\r\n \r\n������� ������ '������$' ��� ������ �� ��������� ���������."
 
; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "�������� ����� Martus � ������� ���� Windows (���� '����')? "
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "���������� �����  Martus �� ������� �����?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "����� Martus ���������� � ����� ��������  $INSTDIR. ����� ��������� Martus, ����������� ���� ����� ��� ��� �����."
 
!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus - ������� ��� �������� ������ �� ������ ��������"
 
!insertmacro LANG_STRING MartusUserGuideShortcut_Text "����������� ������������"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_ru.pdf"
 
!insertmacro LANG_STRING MartusQuickstartShortcut_Text "������� �������"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_ru.pdf"
 
!insertmacro LANG_STRING MartusUninstallShortcut_Text "������� ���������"
 
; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "����� ������ Martus"
 
; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "��������� $(^Name) ���� ������� ������� � ������ ����������"
 
!insertmacro LANG_STRING NeedAdminPrivileges_Text "��� ��������� ��������� $(^Name) ��� ����������� ����� �������������� �� ������ ����������."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "��������������� ������ ��� ��������� ���� ��������������. ���������, ������ �� �� ����� �������������� ��� ������� ����������. ���� ��� �� ���, ����������  $(^Name) ����� ����������."
 
!insertmacro LANG_STRING UninstallProgramRunning_Text "���������, ��� �� ����� ��  $(^Name). � ��������� ������ ��������� �������� �� ������ ������� �����, ������� ������������ � ������ ������."
 
!insertmacro LANG_STRING NewerVersionInstalled_Text "�� ���������� ��� ����������� ����� ����� ������ ($EXISTING_MARTUS_VERSION) ���������  ${PRODUCT_NAME}.  ������ ��� ������ ������������� ����� ������ ������, ������� ��������� ��������� ���������. �������������, ��� ��� �������� � ����� ������ ������ ����� �������������� ������������ ����� ��������.  ����� �������� �� ���������� ����� ����� ������ ���������, ������� OK ��� ������ �� ��������� ���������.  ���� �� ������� ���������� ����� ������ ������ � ����� ������������� ��������������� �������������, ������� �� ��������� ���������, ������� ����� ����� ������, � ����� ���������� ������ ������."
!insertmacro LANG_STRING SameVersionInstalled_Text "������� ������ ($EXISTING_MARTUS_VERSION) ������������ �������� ${PRODUCT_NAME} ��� �����������. ���������� ������?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "����������� ����� ������ ������ ($EXISTING_MARTUS_VERSION) ������������ �������� ${PRODUCT_NAME}. ��������� ��������� ������� �� �� ������ ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "�� ����� ���������� ��� ����������� ����������� ����������� ${PRODUCT_NAME}. ����� ������� ������� ��������� ��������� ��� �� ��������. �� ���������� �������� ������� ������� ��������� ����� ���������. ���� �� �� ��������� ��������� ����� ������ ����� ������� ������ Martus, �������� ��� �������� ������� ������� ��������� � ��������� ��������� ����� ������, � �����  ������� ��������� ��������� ���������. ����� ����� ���� ����� ������ ���������  ������ ��������� ���������."
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "�������� ������ Martus, ������������� �� ������ ����������, ����� ������ � ������� ������ ������ ��������� ���������, ������� �������� � ���� Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "�� ������� ������� � ������� ���������� ������� ������  Martus. ����� ����, ��� ��������� ��������� �������� ���� ������,  ������� ��������� ��������� ������������ �����������  Martus � ������� ������� '��������� � �������� ��������' ������ ���������� � ��������� ������ ��������� ��������� ������. ���� �� �� ��������� ��������� ����� ������ ����� ������� ������ Martus, �������� ��� ������� ���, ������ ��� ������� ���������."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "������ ����������� ������ ������������ ��� ���������� ������ Martus. ��������� � ���������� ������ ����������� ������ ������ ������, ���������� Java."
 
