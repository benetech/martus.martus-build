July 31, 2013
By: Charles LaPierre

First install NSIS version 3.0 or greater on your development machine.
I installed nsis-3.0a1-setup.exe from http://nsis.sourceforge.net/Download which was the latest as of 07/31/2013.
I do not believe the previous stable build of 2.46 handles unicode completely so it is advisable to install NSIS 3.0 or greater.  
I believe 2.46 can read unicode UTF8 files but your OS may have to be in that language to display correctly.  With NSIS 3.0 
you can see all the install languages in their native language on the same computer without having to switch your OS to that language. 

Once installed copy these files 

D:\Benetech\martus-build\BuildFiles\Windows\Win32_NSIS\NSIS\Contrib\Language files\*.* 
o your installed NSIS\Contrib\Language files\ directory.

The changes here allow for you to see the foreign language as well as the English equivalent in the "Installer Language Drop down".
Note: If you get a newer version of any one of these language files when you install a newer version of NSIS
      it is advisable to use the newer copy and modify the following line.
	  EG:
	  !insertmacro LANGFILE "Russian" = "Русский" "Russkij"
	  change to 
	  !insertmacro LANGFILE "Russian" = "Русский, Russian" "Russkij"
	  This way you see "Русский, Russian" in the language selection dropdown. 
	  Now there may be a way show the foreign language and English equivalent without adjusting this line since it does have "Russian" as part of the definition
	  But I was unable to figure out a way to do this, so this is workaround.
	  
You will need to run MakeNSISW.exe to make the unicode version of the installers.
Good luck!
