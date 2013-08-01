July 31, 2013
By: Charles LaPierre (and updated by Kevin Smith)

These instructions cover 3 cases:
- Windows development machine
- Linux development machine
- Linux official build machine

1. Under Linux, you must first install wine.

2. Install NSIS version 3.0 or greater.
the latest as of 07/31/2013 is: nsis-3.0a1-setup.exe 
from http://nsis.sourceforge.net/Download 

The previous stable build of 2.46 does not handle unicode completely, 
so it is advisable to install NSIS 3.0 or greater.  

We believe 2.46 can read unicode UTF8 files but your OS 
may have to be in that language to display correctly.  
With NSIS 3.0 you can see all the install languages in their native language 
on the same computer without having to switch your OS to that language. 

3. Copy customized language files into the NSIS directory
The customized files are located in:
  martus-build\BuildFiles\Windows\Win32_NSIS\NSIS\Contrib\Language files

Copy all of them to your installed NSIS\Contrib\Language files\ directory.  
Under Linux, this may be something like:
  ~/.wine/drive_c/Program Files (x86)/NSIS/Contrib/Language Files/

The changes here allow for you to see the foreign language 
as well as the English equivalent in the "Installer Language Drop down".

Note: If/when you install a newer version of NSIS and these files are modified 
      by NSIS itself, use their newer copy and modify the LANGFILE line in each 
      language file used by Martus:
        Arabic, Farsi, French, Khmer, Russian, Spanish, and Thai.
        
	  The change would be, for example:
	  !insertmacro LANGFILE "Russian" = "Русский" "Russkij"
	  change to 
	  !insertmacro LANGFILE "Russian" = "Русский, Russian" "Russkij"
	  
	  This way you see "Русский, Russian" in the language selection dropdown. 
	  Now there may be a way show the foreign language and English equivalent 
	  without adjusting this line since it does have "Russian" 
	  as part of the definition. 
	  But I was unable to figure out a way to do this, so this is workaround.
	  