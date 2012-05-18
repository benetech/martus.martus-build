README for Martus and Macintosh

See https://www.martus.org for information about Martus software.

Requirements to run Martus on a Macintosh
-----------------------------------------

    * Mac OS X (10.6.4 or later is recommended)

    * One of the following Java Runtime Environment (JRE) versions: (Best): Version 1.6.0_30 or later (Next best): Version 1.5.0_22(rev-b07) or a later 1.5 version. 
NOTE: Java 1.5 and Java 1.6 earlier than the specific revisions mentioned above will not work with Martus Language Pack (MLP) files and may cause other issues in the Martus user interface.  You can check which version of Java is running on your machine by opening a Terminal window and typing “java -version” (without the quotes) and hitting enter/return.

    * 75 MB hard disk space available (100 MB or more recommended)

     * 1GB RAM 
     
    * Internet connection, to back up data to the Martus server. If you do not have an internet connection, see section "9g. Enabling other accounts to send your bulletins to a server." in the Martus User Guide.

    * To send bulletins to a Martus Server, your Martus software must not be blocked by a firewall. If you have a software or hardware firewall, it must allow your computer to contact ports 987 and 988, or ports 80 and 443, on the Internet. If these ports are blocked when you try to select the server, you will see a message that the server is not responding.

    * CD drive (for CD installation only)

    * Screen resolution of 800x600 or greater

Note:  To display your system specifications in Mac, go to the Apple menu > About this Mac > More info… > System report… > Hardware (this is the Hardware Overview).

Note: If you have multiple versions of Java installed, you will need to launch the Java Preferences dialogue to change the version of Java to be used. To locate it, hit Command-Space to bring up the "Spotlight" search, and type Java Preferences. Once it is open, you will see two sets of configurations, one for Java applet plugins and another for Java applications. If you wish to change the version of Java Martus uses by default, select the new version using the dialogue instructions, then close out of the Java Preferences screen.  For additional assistance, please email help@martus.org. 


Installing Martus on Mac OS X
-----------------------------

Note: These instructions may vary depending on which version of Mac OS X you are running. If these instructions are not applicable to what you see on your computer, please email us at help@martus.org.

BEFORE YOU INSTALL:

If an earlier, non-DMG, version of Martus has been installed on this computer, you must first delete all of the Martus-related jar files from the /Library/Java/Extensions folder. This is very important, and Martus may not work correctly if you skip this step. You may be prompted to enter the admin username/password to delete these files.

The Martus-related jars are:

    * InfiniteMonkey.jar
    * bc-jce.jar
    * bcprov-jdk14-135.jar
    * icu4j_3_2_calendar.jar
    * js.jar
    * junit.jar
    * layouts.jar
    * persiancalendar.jar
    * velocity-1.4-rc1.jar
    * velocity-dep-1.4-rc1.jar
    * xmlrpc-1.2-b1.jar

After deleting these files, the Extensions folder may be empty.

We also recommend that you delete any shortcuts you may have created to the old Martus version, to avoid confusion.

Installing Martus using a Mac DMG file (new or upgrade):

   1. Obtain the Martus DMG file from the Martus website or CD/ISO, and double-click it as you would for any DMG file.

   2. If the Martus folder was not automatically opened, double-click on the Martus it to view the contents.

   3. While you can copy the Martus application (Martus.app) to your Applications folder, please note that if you do so, it will not be removed when you uninstall Martus, which may be a security concern.

   4. The MartusDocumentation folder contains files with helpful information about Martus, including User Guides, Quick Start Guides, and README files that describe the features in each version, all in various languages. We also suggest that you copy this folder to your computer where you can have easy access to it.

   5. If you are using a Martus Language Pack (e.g. Martus-en.mlp), please copy the mlp file to your Martus data folder. For instructions on how to access your Martus data folder, see the section below named "Viewing the Martus Data Folder".

   6. If needed, Burmese, Khmer, and Bangla/Bengali fonts are provided in the Fonts folder. If you need any of these, you can install it by double-clicking on the .ttf file and choosing "Install". If you have issues running or entering text in Burmese, Khmer, or Bangla/Bengali, please see FAQs 40 - 42 in the Martus User Guide, or email help@martus.org.



Running Martus on Mac OS X (from a DMG)
---------------------------------------

Double-click the Martus icon/application (Martus.app file).

To display additional information as Martus runs that will be helpful in diagnosing any problems in Mac OSX, you can run the "Console" application. Launch it in one of two ways:

   1. Inside your Applications folder, find the Utilities folder, and double-click on Console.

   2. Hit Command-Space to bring up the search, enter "console", and when it finds the Console app, launch it.

As long as console is running, any console output from Martus will appear there. You only have to run console once, and then you can run (and stop) Martus as many times as you want. You can copy and paste the text from the Console app into an email to help@martus.org. 

Note: To see only Martus info in the console and exclude any console info from other programs, type “.Martus” (without the quotes) in the search box at the top right of the console screen.

Note: If you need to run Martus with additional options than the default installation (e.g. increased memory, shorter timeout), see FAQs 12 and 39 in the Martus User Guide (available at https://www.martus.org/downloads or in the Documents folder of your Martus installation).


Viewing the Martus Data Folder
-------------------------------

The Martus data folder is named .Martus and is located in your Home folder. This folder contains your account and bulletin info, as well as other files created while using Martus, such as report/search templates, HQ information .mpi files, etc. The Martus data folder is also where you would place any language pack files.

Normally the Martus data folder is hidden, so in order to be able to see it with Finder, please follow these instructions:

   1. Open a Terminal window, under Applications > Utilities (or by hitting Command-Space to bring up the search, enter "terminal", and when it finds the Terminal app, launch it).

   2. Type the following (exactly) and then press return/enter: 
defaults write com.apple.Finder AppleShowAllFiles YES

   3. Restart the Finder by holding the Option key, and click and hold the Finder icon. When the context menu shows, select Relaunch.  (Alternately, you can type the following in the Terminal:
  
   killall Finder 
 
and hit Enter.) 

   4. When the Finder restarts, you should now see the .Martus folder within your home directory, and can copy files to/from there as needed.



Uninstalling Martus on Mac OS X (DMG)
-------------------------------------

To uninstall Martus in Mac OS without deleting your Martus bulletins or account data, simply delete the Martus application (Martus.app file).

Note:  Please note that if you copied the Martus application (Martus.app) to your Applications folder or your Desktop (or elsewhere on your computer), it will not be removed when you uninstall Martus, which may be a security concern.

If you wish to delete *ALL* of your Martus bulletins and account data, delete the .Martus folder in your home folder. The .Martus folder is normally hidden; to see it in Finder, you must configure Finder to show hidden files. 

