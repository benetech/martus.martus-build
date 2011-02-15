README for Martus and Macintosh

See http://www.martus.org for information about Martus software.


Requirements to run Martus on a Macintosh
-----------------------------------------
- Mac OS X (10.6.4 or later is recommended)
- One of the following Java Runtime Environment (JRE) versions:
  (Best): Version 1.6.0_22 or later
  (Next best): Version 1.5.0_22(rev-b07) or a later 1.5 version. 
  (Acceptable): Version 1.4.2
  NOTE: Java 1.5 and Java 1.6 earlier than the specific revisions
  mentioned above will not work with Martus Language Pack (MLP) files.
- 75 MB hard disk space available (100 MB or more recommended)
- Internet connection, to back up data to the Martus server. 
  If you do not have an internet connection, see section 
  "9g. Enabling other accounts to send your bulletins to a server."
  in the Martus User Guide.
- To send bulletins to a Martus Server, your Martus software 
  must not be blocked by a firewall. If you have a software or 
  hardware firewall, it must allow your computer to contact 
  ports 987 and 988, or ports 80 and 443, on the Internet. 
  If these ports are blocked when you try to select the server, 
  you will see a message that the server is not responding.
- CD drive (for CD installation only)
- Screen resolution of 800x600 or greater


Installing Martus on Mac OS X
-----------------------------
Note: These instructions may vary depending on which version of 
Mac OS X you are running. If these instructions are not applicable 
to what you see on your computer, please email us at help@martus.org.

BEFORE YOU INSTALL:

If an earlier version of Martus has been installed on this computer, 
you must first delete all of the Martus-related jar files from the 
/Library/Java/Extensions folder. This is very important, and Martus 
may not work correctly if you skip this step. You may be prompted to 
enter the admin username/password to delete these files.

The Martus-related jars are:
- InfiniteMonkey.jar
- bc-jce.jar
- bcprov-jdk14-135.jar
- icu4j_3_2_calendar.jar
- js.jar
- junit.jar
- layouts.jar
- persiancalendar.jar
- velocity-1.4-rc1.jar
- velocity-dep-1.4-rc1.jar
- xmlrpc-1.2-b1.jar

After deleting these files, the Extensions folder may be empty.

We also recommend that you delete any shortcuts you may have created 
to the old Martus version, to avoid confusion.


Installing Martus (new or upgrade):
1. Download the Martus DMG file and double-click it as you would for 
   any DMG file.
2. Double-click on the Martus icon to view the contents.
3. We recommend that you copy the Martus application (Martus.app) 
   to your Applications folder. 
4. The MartusDocumentation folder contains files with helpful 
   information about Martus, including User Guides, Quick Start Guides, 
   and README files that describe the features in each version, all in 
   various languages. We also suggest that you copy this folder to your 
   computer where you can have easy access to it. 
5. If you are using a Martus Language Pack (e.g. Martus-en.mlp), please 
   copy the mlp file to your Martus data folder. For instructions on 
   how to access your Martus data folder, see the section below named 
   "Viewing the Martus Data Folder".
6. Burmese, Khmer, and Bangla/Bengali fonts are provided in the Fonts 
   folder. If you need any of these, you can install it by 
   double-clicking on the .ttf file and choosing "Install". 
   If you have issues running or entering text in Burmese, Khmer, or 
   Bangla/Bengali, please see FAQs 40 - 42 in the Martus User Guide, 
   or email help@martus.org.
   

Running Martus on Mac OS X
--------------------------
Double-click the Martus application.

To display additional information as Martus runs that will be 
helpful in diagnosing any problems in Mac OSX, you can run the 
"Console" application. Launch it in one of two ways: 
  
1. Inside your Applications folder, find the Utilities folder, 
   and double-click on Console. 
2. Command-Space to bring up the search, enter "console", and 
   when it finds the Console app, launch it. 
  
As long as console is running, any console output from Martus 
will appear there. You only have to run console once, and then 
you can run (and stop) Martus as many times as you want. 


Viewing the Martus Data Folder
-------------------------------
The Martus data folder is named .Martus and is located in your Home 
folder. This folder contains your account and bulletin info, as well 
as other files created while using Martus, such as report/search 
templates, HQ information files, etc. The Martus data folder is also 
where you would place any language pack files. 

Normally the Martus data folder is hidden, so in order to be able to 
see it with Finder, please follow these instructions:
 
1. Open a Terminal window, under Applications > Utilities 
   (or by hitting Command-Space to bring up the search, enter 
   "terminal", and when it finds the Terminal app, launch it).  
2. Type the following (exactly) and then press return/enter:
   defaults write com.apple.Finder AppleShowAllFiles YES
3. Restart the Finder by holding the Option key, and click and hold 
   the Finder icon. When the context menu shows, select Relaunch.
4. You should now see the .Martus folder within your home directory, 
   and can copy files to/from there as needed.
  

Uninstalling Martus on Mac OS X
-------------------------------
To uninstall Martus in Mac OS without deleting your Martus bulletins or 
account data, simply delete the Martus application.

If you wish to delete *ALL* of your Martus bulletins and account data, 
delete the .Martus folder in your home folder. The .Martus folder is 
normally hidden; to see it in Finder, you must configure Finder to 
show hidden files.
