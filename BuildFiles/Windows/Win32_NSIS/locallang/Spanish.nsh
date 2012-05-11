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

!define LANG "SPANISH" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "Idioma de la instalaci�n"
!insertmacro LANG_STRING LangDialog_Text "Por favor escoja el idioma del programa de instalaci�n."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} ha sido instalado en su sistema.\r\n \r\nVisite el sitio web https://www.martus.org/downloads/ para verificar si hay nuevos Paquetes de Idiomas (Language Packs) para ${PRODUCT_NAME} disponibles. \r\n \r\nUn 'Paquete de idioma' le permite instalar traducciones o documentaci�n nuevas y actualizadas en cualquier momento enseguida que est� disponible una nueva versi�n de Martus. Los 'Paquetes de idioma' pueden contener actualizaciones a las traducciones de la interfaz gr�fica del cliente Martus, el Manual de Usuario, la Gu�a r�pida/ Tarjeta de referencia, el archivo L�AME, y ayuda dentro del programa.\r\n \r\nPresione Terminar para cerrar este asistente."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "�Necesita enlaces a Martus en el men� Inicio?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "�Necesita un enlace a Martus sobre su escritorio?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "La instalaci�n va a colocar un enlace a Martus en la carpeta del programa en $INSTDIR. Use este enlace, o una copia de el, para iniciar Martus."

!insertmacro LANG_STRING MartusShortcutDescription_Text "Sistema de Boletines Martus de Derechos Humanos"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "Manual de usuario"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_es.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Gu�a r�pida"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_es.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "Desinstalar"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Archivo de bolet�n de Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "Se desinstal� $(^Name) de su ordenador."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "Es necesario tener permiso de administraci�n para poder instalar el software $(^Name) en su ordenador."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Ocurri� un error inesperado. Verifique que tenga permiso de administraci�n para poder instalar el software $(^Name) en su ordenador. De no ser asi la instalaci�n de $(^Name) podr� tener problemas."

!insertmacro LANG_STRING UninstallProgramRunning_Text "Por favor verifique de que no est� usando $(^Name) o el programa de desinstalaci�n no podr� remover aquellos archivos que esten en uso."

!insertmacro LANG_STRING NewerVersionInstalled_Text "Tiene una versi�n m�s reciente ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} instalado en su ordenador. Si quiere instalar la copia anterior va a tener que desinstalar la copia actual y volver a reiniciar la instalaci�n de la copia anterior. Note que va a perder funcionalidad si decide continuar e instala una copia anterior y puede que no pueda ver o editar boletines creados con la versi�n m�s reciente. Para mantener la versi�n actual oprima Aceptar para salir del programa de instalaci�n. Si es que decide instalar la versi�n anterior, oprima Aceptar para salir del programa de instalaci�n, desinstale la versi�n actual y vuelva a intentar este programa de instalaci�n."
!insertmacro LANG_STRING SameVersionInstalled_Text "La versi�n actual ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} ya est� instalada. �Desea volver a instalarla?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "Tiene una versi�n anterior ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} instalado en su ordenador.  El programa de instalaci�n lo actualizar� a la versi�n ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "Tiene una versi�n anterior de ${PRODUCT_NAME} en su ordenador. Intentaremos ejecutar el programa de desinstalaci�n. Una vez este haya completado el programa de instalaci�n de la versi�n actual continuar�. Si es que a�n no ha hecho una copia de seguridad de su par de claves le sugerimos salga del programa de instalaci�n, haga una copia y vuelva a ejecutar este programa de instalaci�n. �Desea continuar con la instalaci�n?"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "La versi�n de Martus que tiene instalada no puede ser actualizada por este programa de instalaci�n. Solo puede ser actualizada por el programa de instalaci�n de Martus que contiene el entorno Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "No se pudo remover la versi�n anterior de Martus en su ordenador. El programa de instalaci�n terminar�, por favor remueva Martus de su ordenador usando la funci�n para agregar y quitar programas en su panel de control y luego vuelva a ejecutar este programa de instalaci�n. Antes de desinstalar Martus le sugerimos haga una copia de seguridad de su par de claves si es que a�n no lo ha hecho."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "Este programa de instalaci�n es para actualizar una copia existente de Martus. Por favor descargue e instale el programa de instalaci�n de Martus que contiene el entorno Java."
