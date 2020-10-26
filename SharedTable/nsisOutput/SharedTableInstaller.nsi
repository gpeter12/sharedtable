;NSIS Modern User Interface
;Basic Example Script
;Written by Joost Verburg

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  ;Name and file
  Name "Shared Table"
  OutFile "InstallSharedTable.exe"
  Unicode True

  ;Default installation folder
  InstallDir "$PROGRAMFILES64\SharedTable"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\SharedTable" "instdir"

  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin
  
  
Function .onInit

         Exec $INSTDIR\Uninstall.exe

FunctionEnd

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  ;!insertmacro MUI_PAGE_LICENSE "${NSISDIR}\Docs\Modern UI\License.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "Hungarian"

;--------------------------------
;Installer Sections

Section "Programfájlok" pf

	SectionIn RO
	SetOutPath "$INSTDIR"
  

	File /r ".\..\build\image\*"

	;Store installation folder
	WriteRegStr HKCU "Software\SharedTable" "instdir" $INSTDIR

	;Create uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Start menü parancsikon" smp
  
	SectionIn RO
	CreateDirectory "$SMPROGRAMS\SharedTable"
	CreateShortcut "$SMPROGRAMS\SharedTable\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
	CreateShortcut "$SMPROGRAMS\SharedTable\SharedTable.lnk" "$INSTDIR\bin\javaw.exe" " -Dfile.encoding=UTF-8 -m sharedtable/com.sharedtable.controller.MainViewController" "$INSTDIR\bin\javaw.exe" 0
  
  
SectionEnd


Section "Asztali parancsikon" ap

  CreateShortcut "$DESKTOP\SharedTable.lnk" "$INSTDIR\bin\javaw.exe" "-Dfile.encoding=UTF-8 -m sharedtable/com.sharedtable.controller.MainViewController" "$INSTDIR\bin\javaw.exe" 0
  
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  ;LangString DESC_SecDummy ${LANG_ENGLISH} "A test section."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${pf} "A program futtatásához nélkülözhetelen fájlok"
	!insertmacro MUI_DESCRIPTION_TEXT ${smp} "Parancsikon létrehozása a startmenüben"
	!insertmacro MUI_DESCRIPTION_TEXT ${ap} "Parancsikon létrehozása az asztalon"
  !insertmacro MUI_FUNCTION_DESCRIPTION_END


; Optional section (can be disabled by the user)

;--------------------------------
;Uninstaller Section

Section "Uninstall"

	RMDir /r "$APPDATA\SharedTable"
	RMDir /r "$LOCALAPPDATA\SharedTable"

	RMDir /r "$INSTDIR"

	RMDir "$INSTDIR"

	Delete "$INSTDIR\Uninstall.exe"

	Delete "$SMPROGRAMS\SharedTable\Uninstall.lnk"
	Delete "$SMPROGRAMS\SharedTable\SharedTable.lnk"
	Delete "$DESKTOP\SharedTable.lnk"
	RMDir "$SMPROGRAMS\SharedTable"

	DeleteRegKey /ifempty HKCU "Software\SharedTable"

	

SectionEnd