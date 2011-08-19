=====================================
   F I L E S P L I T  -  V 2.0.100

 Pikeus - www.pikeus.freeserve.co.uk
=====================================

Filesplit is a command line application that splits a file into chunks 
of whatever size you specify. It can also merge the chunks back into a 
single file again. 

A maximum number of 999 chunks can be generated (if anybody has a good
reason why this should be increased, please let me know).

Data is split in binary form so that it works with text files or 
graphics, executable programs, anything. 

To split a file into chunks of 1MB you need to specify the size in K 
as 1000.


INSTALLATION:
=============
LINUX: Place filesplit into "/bin" and you can run filesplit from 
whatever directory you're in. Note: you'll need to set executable 
permissions for filesplit.

WINDOWS: Place filesplit into "C:\Windows\Command" and you can run 
filesplit from whatever directory you're in.

Note: If you don't wish to add filesplit to your Command/bin 
directory, place filesplit into the directory containing the file you 
wish to split, and run filesplit from there. Note, when running the 
program in this way on Linux you'll need to type "./filesplit" rather 
than just "filesplit".


SPLITTING A FILE
================
To split a file, use the following arguments:

filesplit -s <source> <size in k> [<destination>]

<source> is the name of the file you wish to split,
<size in k> is the maximum size you wish each chunk to be,
[<destination>] is where you want the chunks to go.

The destination argument is optional, if you don't specify this then 
the chunks will be created in the same directory as the source file.

--

Example...

If you have a file named "file.exe" which is 80k, and you 
wanted to split it into 10k chunks, you'd do the following...

filesplit -s file.exe 10

The output is as follows...

Splitting:

file.exe_001.cnk
file.exe_002.cnk
file.exe_003.cnk
file.exe_004.cnk
file.exe_005.cnk
file.exe_006.cnk
file.exe_007.cnk
file.exe_008.cnk
* DONE *

Optionally, you could specify the directory to save the chunks into as:

Windows...
filesplit -s file.exe 10 "C:\mychunks"\

or 

Linux...
filesplit -s file.exe /home/whatever/chunkstore/ 

NOTE:  the path separator must be added to the end of the directory you
wish to save the chunks to.  This is necessary because filesplit can't
assume whether to automatically add a "/" for Linux or a "\" for
Windows.


MERGING A FILE:
===============
To merge a file, use the following arguments:

-m <source> [<destination>]

<source> is the name of the file you wish to merge.  This can be the
  name of any chunk file (e.g. "whatever.exe_003.cnk") or just the
  name of the file without the ".cnk" extension (e.g. "whatever.exe").
[<destination>] is where you want the chunks to go.

The destination argument is optional, if you don't specify this then the 
merged file will be restored to the same directory as the chunk files.

--

Example...

If you have a load of chunks starting with the filename "file.exe",
having the .cnk extension (eg "file.exe_001.cnk"), and you wish to
merge them back into a file called "file.exe", you'd do the following...

filesplit -m file.exe

The output is as follows...

Merging:

file.exe_001.cnk
file.exe_002.cnk
file.exe_003.cnk
file.exe_004.cnk
file.exe_005.cnk
file.exe_006.cnk
file.exe_007.cnk
file.exe_008.cnk
* DONE *

Optionally, you could specify the directory to save the restored file
into:

Windows...
filesplit -m file.exe_001.cnk "C:\Program Files\merged"\

or 

Linux...
filesplit -m file.exe /home/whatever/merged/ 

NOTE:  the path separator must be added to the end of the directory you
wish to save the chunks to.  This is necessary because filesplit can't
assume whether to automatically add a "/" for Linux or a "\" for
Windows.


AUTOMERGE
=========

If you associate ".cnk" files with filesplit (so that they're opened
with filesplit when you open them), then filesplit will automatically
restore the file into the same directory as the chunks.

If automerge fails for some reason it will generate a text file named
FAILED.txt, containing a message such as:

Error: Couldn't open source file: file.exe_001.cnk

RELEASE NOTES
=============
* filesplit V2.0.100 is compatible with previous versions of filesplit.

* Updated SPLIT process:
  1) New argument [<destination>] to specify where chunks should
     arrive.  
  2) If no chunk size is specified it defaults to 1440k 
     (floppy disk size).

* Updated MERGE process:
  1) New argument [<destination>] to specify where chunks should
     arrive.
  2) If filesplit is called with a single command line argument
     that's a chunk file (e.g. "filesplit largefile.exe_001.cnk"),
     then it will automatically merge it (regardless of which 
     chunk you pass as the argument).  
     This means that if you associate the extension ".cnk" with
     filesplit in your operating system, whenever you open a chunk 
     file it will automatically merge it.  
     Obviously, if you use filesplit to split up log files for easy
     reading, you'd associate ".cnk" files with your favorite text
     editor.

     If filesplit automerge process fails, it will generate a 
     file named FAILED.txt stating the error.
