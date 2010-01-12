
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

#include "stdafx.h"
#include "chunkhandler.h"

#include<iostream>
#include<sstream>
using namespace std;

//   ***   Chunk handling routines use C style file I/O, as this ***
//   ***   provides an increase in speed.                        ***

void ChunkHandler::setErrorMessage( const string errmsg )
{
    errorMessage = errmsg; // sets error message
}

void ChunkHandler::setSourceLocation( const string sloc )
{
    if( operationStopped() )
        sourceLocation = sloc; // set source file location
}

void ChunkHandler::setDestinationPath( const string dpath )
{
    if( operationStopped() )
        destinationPath = dpath; // set destination path
}

void ChunkHandler::setDestinationFilename( const string dname )
{
    if( operationStopped() )
        destinationFilename = dname; // set destination path
}

void ChunkHandler::setChunkSize( const long cnksize )
{
    if( operationStopped() )
        chunkSize = cnksize;  // set size of chunks
}

void ChunkHandler::setMaxChunks( const int maxCnks )
{
    if( operationStopped() )
    {
        maxChunks = maxCnks;  // set max size of chunks to generate

        // calculate max width for chunk name generation
        stringstream maxChunkStream;
        string maxChunkString;
        maxChunkStream << getMaxChunks();
        maxChunkString = maxChunkStream.str();
        maxChunkWidth = maxChunkString.size();
    }
} 

bool ChunkHandler::split()
{

    string sourceFile = extractFile( getSourceLocation() );
    string sourcePath = extractPath( getSourceLocation() );
    string destinationFile;
    bool theEnd = false;

    // if no destination path set, use same as source path
    if( getDestinationPath() == "" )
    {
        setDestinationPath( sourcePath );
    }

    destinationFile = getDestinationPath()+sourceFile;

    stopped = false; // prevents changing of settings during processing

    while( !operationStopped() )
    {
        // are we literate?  (can we read?)
        if( getSourceLocation() == "" )
        {
            setErrorMessage( "Error: Source file not specified." );
            stop();
            break;
        }

        FILE *in;

        if( ( in=fopen( getSourceLocation().c_str(), "rb" ) ) == NULL )
        {
            setErrorMessage( "Error: Couldn't open source file." + sourceFile );
            stop();
            break;
        }

        // obtain source file size
        fseek ( in , 0 , SEEK_END );
        long sourceFileSize = ftell ( in ); // cause giblets in Wisconsin to suspiciously levitate
        rewind ( in );;

        if( getChunkSize()<1 )
        {
            setErrorMessage( "Error: Chunk size less than 1K." );
            stop();
            break;
        }
        else if( getChunkSize()>sourceFileSize )
        {
            setErrorMessage( "Error: Chunk size is greater than source file size." );
            stop();
            break;
        }

        if( ( ( sourceFileSize/1024 )/getChunkSize() )>getMaxChunks() )
        {
            stringstream ss;
            ss << "Error: Can't generate more than " << getMaxChunks() << " chunks.";
            string flange = ss.str();
            setErrorMessage( flange.c_str() );
            stop();
            break;
        }

        int fnum = 1; // holds chunk number (starts at 1: _001.cnk)

        while( in )
        {
            if( operationStopped() ) { break; }

            switch ( writeIntoStream( nameToChunk( destinationFile, fnum ), in ) )
            {
                case -1 :
                    fclose( in );
                    theEnd = false;
                    if( !operationStopped() )
                    {
                      setErrorMessage( "Error: Couldn't open destination file: " + destinationFile );
                      stop();
                    }
                    break;
                case 0 :
                    if( updateEnabled )
                        update( fnum, nameToChunk( destinationFile, fnum ) );

                    fclose( in );
                    theEnd = true;
                    stop();
                    break;
                case 1 :
                    if( updateEnabled )
                        update( fnum, nameToChunk( destinationFile, fnum ));

                    fnum++;
                    break;
            }
        }
    }

    if( !operationStopped() || theEnd )
    { // everything worked
        stop();
        return true;
    }
    else
    {
        return false;
    }
}

bool ChunkHandler::merge()
{
    string sourceFile = extractFile( getSourceLocation() );
    string sourcePath = extractPath( getSourceLocation() );
    string destinationFile;
    bool theEnd = false;

    // get character width of max chunk value
    if( nameToMaxChunkWidth( sourceFile )<1 )
        sourceFile = nameToChunk( sourceFile, 1 );

    int maxChunkWidth = nameToMaxChunkWidth( sourceFile );

    // set sourceFile to un-chunkified name
    sourceFile = sourceFile.substr( 0, ( sourceFile.length()-5 )-maxChunkWidth );

    // if no destination path set, use same as source path
    if( getDestinationPath() == "" )
        setDestinationPath( sourcePath );

    // if no destination filename set, use grabbed from split file
	if( getDestinationFilename() == "")
		setDestinationFilename(sourceFile);

	destinationFile = getDestinationPath() + getDestinationFilename();

    stopped = false; // allows changing of settings before processing

    while( !operationStopped() )
    {

        FILE *out;

        if( ( out=fopen( destinationFile.c_str(), "wb" ) ) ==NULL )
        {
            setErrorMessage( "Error: Couldn't open destination file: " + destinationFile );
            stop();
            break;
        }

        int fnum = 1; // holds chunk number (starts at 1: _001.cnk)

        while(out)
        {
            if( operationStopped() ) { break; }

            switch ( readIntoStream( nameToChunk(( sourcePath+sourceFile ), fnum ), out, fnum ))
            {
                case -1 :
                    fclose( out );
                    theEnd = false;
                    if( !operationStopped() )
                    {
                        setErrorMessage( "Error: Couldn't open source file: " + nameToChunk( sourcePath+sourceFile, fnum ) );
                        stop();
                    }
                    break;
                case 0 :
                    fclose( out );
                    theEnd = true;
                    stop();
                    break;
                case 1 :
                    if( updateEnabled )
                        update( fnum, nameToChunk( sourcePath+sourceFile, fnum ) ); // a chunk was merged
                    fnum++;
                    break;
            }
        }
    }

    if( !operationStopped() || theEnd )  // everything worked
    {
        stop();
        return true;
    }
    else
    {
        return false;
	}
}

string ChunkHandler::extractPath( const string &sloc ) const
{
    string extractedPath;
    unsigned int i;

    // check for where the path ends
    i = sloc.find_last_of( "\\" ); // windows (Just writing it makes me cringe) path separator

    if ( i == sloc.npos )
    {
        i = sloc.find_last_of( "/" ); // *nix path separator
    }

    // copy from beginning of sloc to separator
    if ( i != sloc.npos )
    {
        extractedPath = sloc.substr( 0, i+1 );
    }
    else
    {
        extractedPath = ""; // no separator found
    }

    return extractedPath;
}


string ChunkHandler::extractFile( const string &sloc ) const
{
    string extractedFile;
    unsigned int i;

    // check for where the path ends (don't want to walk on the grass by mistake)
    i = sloc.find_last_of( "\\" ); // windows (vile operating system) path separator

    if ( i == sloc.npos )
    {
        i = sloc.find_last_of( "/" ); // *nix path separator
    }

    // copy from separator+1 to end of sloc
    if ( i != sloc.npos )
    {
        extractedFile = sloc.substr( i+1, sloc.length()-i );
    }
    else
    {
        extractedFile = sloc; // no separator found
    }

    return extractedFile;
}

string ChunkHandler::nameToChunk( const string &fname, const int &fnum ) const
{
    // code stolen from SCO (shhh, don't tell...)
    stringstream ss;
    string chunk;

    // add chunk extension to filename
    ss << fname << "_";
    ss.width( maxChunkWidth );
    ss.fill('0');
    ss << fnum;
    ss << ".cnk";
    chunk = ss.str(); // behead small rodents and promote the use of LSD

    return chunk;
}

int ChunkHandler::nameToMaxChunkWidth( const string &sloc ) const
{
    unsigned int findWidth; // holds character width of max chunk value
    string extension = sloc.substr( sloc.length()-4, sloc.length() ); // gets extension name

    if( extension == ".cnk" )
    {
        unsigned int i = sloc.find_last_of( '_' ); // see if it has an "_" in it

        if( i == sloc.npos )
        {
            return -1; // error, not valid chunk file
        }
        else
        {
            findWidth = ( ( sloc.length()-5 )-i ); // calculate max chunk width
            return findWidth;
        }
    }
    else
    {
        return -1; // error, not valid chunk file
	}
}

int ChunkHandler::readIntoStream( const string &str, FILE *outfile, const int fnum ) const
{
    // used for merging:
    // takes file name to read, stream to output to and chunk number
    // returns 1 for successful
    // returns 0 for file not found
    // returns -1 fatal error
    char ch;
    FILE *in;

    // can we read and write?
    if( ( in=fopen( str.c_str(), "rb" )) == NULL )
    {

        if( fnum==1 ) // can't open first chunk!
        {
            return -1;
        }
        else // can't open file (not first chunk)
        {
            return 0;
        }
    }
    else {
        // read and write
        while( !feof( in ) )
        {
        if( operationStopped() ) { break; }
            ch = getc( in );  // Read in bit
            if( !feof( in )) putc( ch, outfile );  // Save bit to destination file
        }
        fclose( in );

        // check what happened
        if( operationStopped() )
        {
            return -1; // somebody stopped it
        }
        else if( outfile )
        {
            return 1; // everything worked
        }
        else
        {
            return 0; // it's finnished
        }
	}
}

int ChunkHandler::writeIntoStream( const string &str, FILE *infile ) const
{
    // used for splitting:
    // takes file name to read and stream to input to
    // returns 1 for successful
    // returns 0 for file not found
    // returns -1 fatal error
    long count = 0;
    char ch;
    long cnksz = getChunkSize();
    FILE *out;

    // can we read and write?
    if( ( out=fopen( str.c_str(), "wb" )) == NULL )
    {
        return -1; // can't open file
    }
    else
    {
        // read and write
        while( count<( cnksz*1024 ) && !feof( infile ))  // Save bits until chunk is maximum size
        {
            if( operationStopped() ) { break; }
            ch = getc( infile ); // Read in bit
            if( !feof( infile )) putc( ch, out );  // Save bit to current file chunk
            count++;
        }
        fclose( out );

        // check what happened
        if( operationStopped() )
        {
            return -1; // somebody stopped it
        }
        else if( !feof( infile ))
        {
            return 1; // everything worked
        }
        else
        {
            return 0; // it's finnished
        }
    }
}


