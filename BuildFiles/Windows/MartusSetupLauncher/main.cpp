// *****************************************************************
//
//  Filesplit - file splitter and merger
//  Version 2.0.100
//  Copyright (C) 2003 Chris Pike
//  Contact:       www.pikeus.freeserve.co.uk
//  Modified:      2004.04.27
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of
// the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. 
// *****************************************************************

#include <iostream>
#include <string>
#include "chunkhandler.h"
#include "config.h"
using namespace std;

void helpInfo() {
    cout << "use: filesplit [-options]" << endl << endl;
    cout << "Where options include:" << endl << endl;
    cout << "  --help                                    print out this message" << endl;
    cout << "  --version                                 print out the build" << endl;
    cout << " -s <source> <size in k> [<destination>]    split file into specified size" << endl;
    cout << " -m <source> [<destination>]                merge file" << endl;
}

int main(int argc, char *argv[]) {
    ChunkHandler *filesplit = new ChunkHandler; // declare ChunkHandler object
    Updater *upd = new Updater; // declare Update object

    filesplit->setUpdater(upd); // set filesplit to use this Update object

    if(argc==1) { // filesplit called with no arguments
        helpInfo();
        // implement GUI mode here
    }
    else if(argc<3) { // filesplit called with single argument
        if(!strcmp(argv[1], "microsoft")) {
            cout << "Ugg!  How dare you mention that vile name!" << endl;
            exit(1);
        }
        else if(!strcmp(argv[1], "--version")) {
            cout << "Filesplit - file splitter/merger" << endl;
            cout << "Version " << VERSION << " - [09.03.2003]" << endl;
            exit(1);
        }
        else if(!strcmp(argv[1], "--help")) {
            helpInfo();
            exit(1);
        }

        else { // automerge
            unsigned int i;
            string failedpath;
            string fpath;
            string fname;
            string sloc = argv[1];
            string extn = sloc.substr(sloc.length()-4, sloc.length());

            if(extn == ".cnk") {
                // check for path separator character (guaranteed to exist)
                i = sloc.find_last_of("\\"); // win path separator

                if (i == sloc.npos) { // not Windows, *nix ... somebody has some sense
                    fpath = filesplit->extractPath(sloc) + "/";
                    fname = filesplit->extractFile(sloc);
                }
                else {
                    fpath = filesplit->extractPath(sloc) + "\\";
                    fname = filesplit->extractFile(sloc);
                }

                filesplit->setSourceLocation(fpath+fname);
                filesplit->setDestinationPath(fpath);
                if(!filesplit->merge()) {
                    // create FAILED.txt file
                    // note: doesn't matter if it fails
                    FILE *out;
                    failedpath = fpath + "FAILED.txt";
                    out = fopen(failedpath.c_str(), "w");
                    fputs(filesplit->getErrorMessage().c_str(), out);
                }
            }
            else {
                helpInfo();
                exit(1);
            }
        }
    }
    else { // filesplit called with more than one argument
        if(!strcmp(argv[1], "-m")) { // merge
            if(argc>4) {
                helpInfo();
                exit(1);
            }

            cout << "Merging:" << endl << endl;
            filesplit->setSourceLocation(argv[2]);

            if(argc==4) {
                filesplit->setDestinationPath(argv[3]);
            }

            if(!filesplit->merge()) {
                cout << filesplit->getErrorMessage() << endl;
            }
            else {
                cout << "* DONE *" << endl;
            }
        }
        else if(!strcmp(argv[1], "-s")) { // split
            if(argc>5) {
                helpInfo();
                exit(1);
            }

            cout << "Splitting:" << endl << endl;
            filesplit->setSourceLocation(argv[2]);

            if(argc<4) {
                filesplit->setChunkSize(1400);
            }
            else {
                filesplit->setChunkSize(atoi(argv[3]));
            }

            if(argc==5) {
                filesplit->setDestinationPath(argv[4]);
            }

            if(!filesplit->split()) {
                cout << filesplit->getErrorMessage() << endl;
            }
            else {
                cout << "* DONE *" << endl;
            }
        }
        else { // if somebody inputs something weird
            cout << "I don't believe I've come across that command before..." << endl;
            exit(1);
        }
    }

    return 0;
}


