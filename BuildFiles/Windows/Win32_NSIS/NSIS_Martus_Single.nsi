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

;SetCompressor bzip2

; Y = this installer delivers Java, N = it doesnt 
!define IS_JAVA_DELIVERED  "Y"
!define INSTALLER_EXE_NAME "MartusSetupSingle.exe"

!include "common\NSIS_Martus_Installer_Details.nsi"
!include "common\NSIS_Martus_Installer_Common_Defines.nsi"

;--------------------------------
; main file section
Section "MainSection" SEC01
    StrCpy $MARTUS_INSTALLATION_DIR $INSTDIR
    SetOutPath "$MARTUS_INSTALLATION_DIR"
    SetOverwrite ifnewer
    
    ; NOTE*** All the paths below are into the nsis.zip that gets created,
    ; not into the raw martus directory as it is in CVS

    ; copy java redistributable
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Copy redistributable Java...'
    File /r /x CVS "..\BuildFiles\jre6\*"
    
    ; -------------------------------------------
    ; uncomment below for single file distributions
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Copy documents...'
    File "..\BuildFiles\ProgramFiles\*.ico"
    File "..\BuildFiles\ProgramFiles\*.jar"
    File "..\BuildFiles\Documents\license.txt"
    File "..\BuildFiles\Documents\gpl.txt"
    File "..\BuildFiles\Documents\README.txt"
    File "..\BuildFiles\Documents\README_*.txt"
    SetOutPath "$MARTUS_INSTALLATION_DIR\Docs"
    File "..\BuildFiles\Documents\*.pdf"
    SetOutPath "$MARTUS_INSTALLATION_DIR\Docs\Licenses"
    File /r "..\BuildFiles\Documents\Licenses\*"

    ; remove previous bcprov jars
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Attempting to delete prev bcprov...'
    Delete /REBOOTOK "$MARTUS_INSTALLATION_DIR\lib\ext\bcprov*.jar"

    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Copy jars...'
    SetOutPath "$MARTUS_INSTALLATION_DIR\lib\ext"
    File "..\BuildFiles\Jars\*.jar"
    SetOutPath "$MARTUS_INSTALLATION_DIR\src\"
    File "..\BuildFiles\SourceFiles\*.*"
    SetOutPath $MARTUS_INSTALLATION_DIR
    ; ------------------------------------------------

    ; ------------------------------------------------------------------------
    ; create defaultui.txt file with language code (http://www.w3.org/WAI/ER/IG/ert/iso639.htm)
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Create defaultui.txt...'
    IfFileExists "$MARTUS_INSTALLATION_DIR\DefaultUI.txt" 0 write_default_ui
    Delete "$MARTUS_INSTALLATION_DIR\DefaultUI.txt"
write_default_ui:
     StrCpy $MARTUS_LANGUAGE_CODE "en"
    ${Select} $LANGUAGE
        ${Case} ${LANG_RUSSIAN}
            StrCpy $MARTUS_LANGUAGE_CODE "ru"
        ${Case} ${LANG_SPANISH}
            StrCpy $MARTUS_LANGUAGE_CODE "es"
        ${Case} ${LANG_FRENCH}
            StrCpy $MARTUS_LANGUAGE_CODE "fr"
        ${Case} ${LANG_THAI}
            StrCpy $MARTUS_LANGUAGE_CODE "th"
        ${Case} ${LANG_ARABIC}
            StrCpy $MARTUS_LANGUAGE_CODE "ar"
        ${Case} ${LANG_FARSI}
            StrCpy $MARTUS_LANGUAGE_CODE "fa"
;        ${Case} ${LANG_NEPALI}
;            StrCpy $MARTUS_LANGUAGE_CODE "ne"
;        ${Case} ${LANG_BENGALI}
;            StrCpy $MARTUS_LANGUAGE_CODE "bn"
;        ${Case} ${LANG_KHMER}
;            StrCpy $MARTUS_LANGUAGE_CODE "km"
        ${CaseElse}
            StrCpy $MARTUS_LANGUAGE_CODE "en"
    ${EndSelect}

    ClearErrors
    FileOpen $DefaultUIFile "$MARTUS_INSTALLATION_DIR\DefaultUI.txt" "w"
            IfErrors skip_file_creation
    ClearErrors

    FileWrite $DefaultUIFile $MARTUS_LANGUAGE_CODE
            IfErrors skip_file_creation
    ClearErrors

    FileClose $DefaultUIFile
            IfErrors skip_file_creation
    
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Defaultui.txt created!'
skip_file_creation:

    ; copy custom details file
    IfFileExists "$EXEDIR\DefaultDetails.txt" copy_default_details skip_default_details
copy_default_details:
    CopyFiles "$EXEDIR\DefaultDetails.txt" "$MARTUS_INSTALLATION_DIR"
skip_default_details:
    
    ; remove previous shortcuts if necessary
    IfFileExists "$SMPROGRAMS\Martus" remove_existing_shortcuts preserve_existing_shortcuts
remove_existing_shortcuts:
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Deleting previous start menu entries...'
    RMDir /r "$SMPROGRAMS\Martus"
preserve_existing_shortcuts:

    ; ask whether to install start menu shortcuts
    StrCpy $StartMenuShortcut "N"
    MessageBox MB_YESNO "$(StartMenuShortcutQuestion_Text)" /SD IDYES IDYES 0 IDNO lbl_no_startmenu_shortcut
    StrCpy $StartMenuShortcut "Y"
    CreateDirectory "$SMPROGRAMS\Martus"
    CreateShortCut "$SMPROGRAMS\Martus\Martus.lnk" "$INSTDIR\bin\javaw.exe" "-Xbootclasspath/p:$MARTUS_INSTALLATION_DIR\lib\ext\bc-jce.jar -jar $MARTUS_INSTALLATION_DIR\martus.jar" "$MARTUS_INSTALLATION_DIR\app.ico" 0 "" "" "$(MartusShortcutDescription_Text)"
    CreateShortCut "$SMPROGRAMS\Martus\$(MartusUserGuideShortcut_Text).lnk" "$MARTUS_INSTALLATION_DIR\Docs\$(MartusUserGuideShortcut_Filename)" "" "" "" "" "" "Martus $(MartusUserGuideShortcut_Text)"
    CreateShortCut "$SMPROGRAMS\Martus\$(MartusQuickstartShortcut_Text).lnk" "$MARTUS_INSTALLATION_DIR\Docs\$(MartusQuickstartShortcut_Filename)" "" "" "" "" "" "Martus $(MartusQuickstartShortcut_Text)"
    CreateShortCut "$SMPROGRAMS\Martus\$(MartusUninstallShortcut_Text) Martus.lnk" "$MARTUS_INSTALLATION_DIR\bin\uninst.exe" "" "" "" "" "" "$(MartusUninstallShortcut_Text) Martus"

lbl_no_startmenu_shortcut:
    CreateShortCut "$MARTUS_INSTALLATION_DIR\Martus.lnk" "$MARTUS_INSTALLATION_DIR\bin\javaw.exe" "-Xbootclasspath/p:$MARTUS_INSTALLATION_DIR\lib\ext\bc-jce.jar -jar $MARTUS_INSTALLATION_DIR\martus.jar" "$MARTUS_INSTALLATION_DIR\app.ico" 0 "" "" "$(MartusShortcutDescription_Text)"
    
    ; ask whether to install desktop shortcuts
    StrCpy $DesktopShortcut "N"
    MessageBox MB_YESNO "$(DesktopShortcutQuestion_Text)" /SD IDYES IDYES 0 IDNO lbl_no_desktopmenu_shortcut
    StrCpy $DesktopShortcut "Y"
    CreateShortCut "$DESKTOP\Martus.lnk" "$MARTUS_INSTALLATION_DIR\bin\javaw.exe" "-Xbootclasspath/p:$MARTUS_INSTALLATION_DIR\lib\ext\bc-jce.jar -jar $MARTUS_INSTALLATION_DIR\martus.jar" "$MARTUS_INSTALLATION_DIR\app.ico" 0 "" "" "$(MartusShortcutDescription_Text)"
    
lbl_no_desktopmenu_shortcut:
    StrCmp $DesktopShortcut "N" 0 no_launch_info_display
    StrCmp $StartMenuShortcut "N" 0 no_launch_info_display
    
    MessageBox MB_OK "$(LaunchProgramInfo_Text)" /SD IDOK
    
no_launch_info_display:

    ; create entry in registry
    WriteRegStr HKLM "Software\Benetech\${PRODUCT_NAME}\${PRODUCT_EXTENDED_VERSION}" "InstallationDir" "$MARTUS_INSTALLATION_DIR"
    WriteRegStr HKLM "Software\Benetech\${PRODUCT_NAME}" "CurrentVer" "${PRODUCT_EXTENDED_VERSION}"

    ; associate mba files with Martus
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Create registry .mba association...'
    WriteRegStr HKCR ".mba" "" "martusfile"
    WriteRegStr HKCR "martusfile" "" "$(MartusMBAFileDesc_Text)"
    WriteRegStr HKCR "martusfile\DefaultIcon" "" "$MARTUS_INSTALLATION_DIR\file.ico"
    
    ;If an upgrade, blow away previous version of registry
    StrCmp $INSTALLER_ACTION ${ACTION_UPGRADE_OLDER} 0 skip_removal_prev_registry_entry
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Deleting previous registry entry...'    
    DeleteRegKey HKLM "Software\Benetech\${PRODUCT_NAME}\$EXISTING_MARTUS_VERSION"

skip_removal_prev_registry_entry:
SectionEnd

!include "common\NSIS_Martus_Installer_Appended_Common_Functions.nsi"

