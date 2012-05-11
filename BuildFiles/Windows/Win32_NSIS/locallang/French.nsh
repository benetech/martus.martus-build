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

!define LANG "FRENCH" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "valeur de la cha�ne"
!insertmacro LANG_STRING LangDialog_Title "Langue d'Installation. "
!insertmacro LANG_STRING LangDialog_Text "Veuillez s�lectionner la langue d'installation."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} a �t� install� sur votre ordinateur.\r\n \r\n Veuillez vous rendre sur https://www.martus.org/downloads pour t�l�charger les packs de langues Martus mis � jour. \r\n \r\nUn 'Pack Langues' vous permet d'installer � tout moment les nouvelles mises � jour de traductions ou de documentation � la suite d'une nouvelle publication de Martus. Les Packs Langues peuvent contenir les mises � jour de la traduction de l'Interface Utilisateur Client Martus, du Guide de l'Utilisateur, du Guide de D�marrage Rapide, du fichier README (LISEZMOI) et de l'aide incluse dans le programme.\r\n \r\nCliquez sur Fermer pour quitter le programme d'installation."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "Voulez-vous installer un raccourci Martus dans votre menu d�marrer Windows ?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "Voulez-vous installer un raccourci Martus sur votre bureau ?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "Un raccourci Martus a �t� install� dans le dossier programme $INSTDIR. Utilisez ce raccourci, ou une copie, pour ouvrir Martus. "

!insertmacro LANG_STRING MartusShortcutDescription_Text "Syst�me de Communiqu�s Martus pour les Droits Humains "

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "Guide de l'Utilisateur "
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_fr.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "D�marrage Rapide "
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_fr.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "D�sinstaller "

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Archives de Communiqu�s Martus "

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) a �t� supprim� de votre ordinateur. "

!insertmacro LANG_STRING NeedAdminPrivileges_Text "Il vous faut un privil�ge administratif sur cet ordinateur pour pouvoir installer $(^Name) "
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Erreur inconnue pendant la recherche de privil�ges administratifs. Assurez-vous d'avoir les privil�ges administratifs sur cet ordinateur, sinon l'installation de $(^Name) risque d'�chouer "

!insertmacro LANG_STRING UninstallProgramRunning_Text "Veuillez vous assurer d'avoir quitt� $(^Name) sinon le programme de d�sinstallation ne pourra pas supprimer les fichiers utilis�s. "

!insertmacro LANG_STRING NewerVersionInstalled_Text "Une version plus r�cente  ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} est d�j� install�e. Il vous faut d'abord d�sinstaller la version existante avant de pouvoir installer cette ancienne version. Si vous confirmez, vous perdrez cependant certaines fonctionnalit�s et risquez de ne pas pouvoir lire les communiqu�s cr��s avec la version r�cente. Pour conserver la version r�cente, tapez sur OK pour quitter cette installation. Si vous souhaitez quand m�me passer � l'ancienne version malgr� la perte de fonctionnalit�, quittez cette installation, d�sinstallez la version r�cente, puis r�installez l'ancienne version. "
!insertmacro LANG_STRING SameVersionInstalled_Text "La version actuelle ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} est d�j� install�e. Voulez-vous la r�installer ? "
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "Une version plus ancienne ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} est install�e. Le programme d'installation va la remplacer par la version ${PRODUCT_EXTENDED_VERSION}. "
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "Il existe une installation ant�rieure de ${PRODUCT_NAME} sur votre ordinateur. Nous allons tenter de lancer le programme d'installation pour celle-ci, et ensuite l'installation actuelle reprendra. Si vous n'avez pas effectu� une sauvegarde de cl� dans votre version actuelle de Martus, nous vous conseillons de quitter cette installation et d'effectuer une sauvegarde avant de d�sinstaller. Ensuite, vous pourrez relancer ce programme d'installation. "
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "La version de Martus que vous avez install�e ne peut �tre mise � jour qu'avec le programme d'installation complet qui contient Java. "
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "Nous n'avons pas pu supprimer de votre ordinateur l'ancienne version de Martus. Le programme d'installation va maintenant se fermer, veuillez supprimer votre copie de Martus � l'aide de Ajout/Suppression de Programmes dans le Panneau de Configuration, puis relancez ce programme d'installation. Si vous n'avez pas effectu� une sauvegarde de cl� dans votre version actuelle de Martus, nous vous conseillons de le faire avant la d�sinstallation. "
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "C'est une version de mise � niveau de Martus. T�l�chargez et veuillez installez le plein installateur de version qui porte Java."



