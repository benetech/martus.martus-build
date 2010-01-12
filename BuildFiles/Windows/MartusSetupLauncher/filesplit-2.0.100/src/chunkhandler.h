//*****************************************************************
//  Copyright:     (c)2003 - Chris Pike
//  Contact:       http://www.pikeus.freeserve.co.uk
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

#ifndef CHUNKHANDLER_H
#define CHUNKHANDLER_H

#include "updater.h"

#include <string>
#include <iostream>

class ChunkHandler
{
public:
    // setUpdater() receives an Updater object by reference and assigns it to ChunkHandler's
    // Updater object ('chunkUpdater'). This allows for interaction between a ChunkHandler object
    // and a client application that passes the Updater object.
    void setUpdater( Updater *upd ) { chunkUpdater = upd; updateEnabled = true; }
    bool split(); // split chunks - returns "false" if failed/ "true" if successful
    bool merge(); // merge chunks - returns "flase" if failed/ "true" if successful

    // stop() sets flag to terminate operation.  'set' functions (below) are only active when
    // stopped is set to 'true'.  'stopped' is automatically set to 'false' when
    // a file is being split/merged
    void stop() { stopped = true; }

    void setErrorMessage( const std::string errmsg ); // sets error message
    void setSourceLocation( const std::string sloc ); // set source file location
    void setDestinationPath( const std::string dpath ); // set destination path
    void setChunkSize( const long cnksize ); // set size of chunks
    void setMaxChunks( const int maxCnks ); // set max size of chunks to generate

    bool operationStopped() const { return stopped; } // returns whether operation is stopped
    std::string getErrorMessage() const { return errorMessage; } // returns error message
    std::string getSourceLocation() const { return sourceLocation; } // return source file location
    std::string getDestinationPath() const { return destinationPath; } // return path where file will be split/merged to
    long getChunkSize() const { return chunkSize; } // return the size of chunks to be split into
    int getMaxChunks() const { return maxChunks; } // return maximum number of chunks allowed to generate

    std::string extractPath( const std::string &sloc ) const; // take full path and file name and return just path
    std::string extractFile( const std::string &sloc ) const; // take full path and file name and return just file name
    ChunkHandler()
    {
        stopped = true;
        chunkSize = 1440; // floppy disk size
        maxChunks = 999; // don't want to risk any accidents
        maxChunkWidth = 3; // obviously 999 is 3 characters wide
        updateEnabled = false; // user-defined updating not set
    }
    ~ChunkHandler() {}

private:
    Updater *chunkUpdater; // holds pointer to Updater object
    bool updateEnabled; // holds whether updating has been enabled
    bool stopped; // flag to set operation to be cancelled
    std::string errorMessage; // holds name of error
    std::string sourceLocation; // holds full path and filename of file to split/merge
    std::string destinationPath; // holds destination path of file to be split/merged
    long chunkSize; // holds size of chunks to split into
    int maxChunks; // holds maximum number of chunks allowed to generate
    int maxChunkWidth; // holds width of max chunk value (for chunk name)
    int nameToMaxChunkWidth( const std::string &sloc ) const; // returns max chunk width, -1 signifies invalid chunk name format
    std::string nameToChunk( const std::string &fname, const int &fnum ) const; // convert filename to chunk name
    int readIntoStream( const std::string &str, std::FILE *outfile, const int fnum ) const; // used for merging
    int writeIntoStream( const std::string &str, std::FILE *infile ) const; // used for splitting
    ChunkHandler( const ChunkHandler & ); // prevent copy construction
    // update() is called by split and merge when a chunk is incremented.
    // It calls the Updater object's update() function, which can be
    // defined by the client programmer to perform update routines.
    void update( const int &cnknum, const std::string &cnkname ) const { chunkUpdater->update( cnknum, cnkname ); }
};

#endif // CHUNKHANDLER_H
