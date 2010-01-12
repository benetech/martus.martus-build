// MartusSetup.cpp : Defines the class behaviors for the application.
//

#include "stdafx.h"
#include  <io.h>
#include <strstream>
#include "MartusSetup.h"
#include "MartusSetupDlg.h"
#include <windows.h>
#include <string>
#include "chunkhandler.h"
using namespace std;


#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

//This is the name of the exe before it is split
#define MARTUS_SETUP_FILENAME_BEFORE	"MartusClient-3_3_2_exe"

//This is the name of the exe after it is joined
#define MARTUS_SETUP_FILENAME_AFTER		"MartusClient-3_3_2.exe"


/////////////////////////////////////////////////////////////////////////////
// CMartusSetupApp

BEGIN_MESSAGE_MAP(CMartusSetupApp, CWinApp)
	//{{AFX_MSG_MAP(CMartusSetupApp)
		// NOTE - the ClassWizard will add and remove mapping macros here.
		//    DO NOT EDIT what you see in these blocks of generated code!
	//}}AFX_MSG
	ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CMartusSetupApp construction

CMartusSetupApp::CMartusSetupApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}

/////////////////////////////////////////////////////////////////////////////
// The one and only CMartusSetupApp object

CMartusSetupApp theApp;

/////////////////////////////////////////////////////////////////////////////
// CMartusSetupApp initialization

BOOL CMartusSetupApp::InitInstance()
{
	AfxEnableControlContainer();

	// Standard initialization
	// If you are not using these features and wish to reduce the size
	//  of your final executable, you should remove from the following
	//  the specific initialization routines you do not need.

#ifdef _AFXDLL
	Enable3dControls();			// Call this when using MFC in a shared DLL
#else
	Enable3dControlsStatic();	// Call this when linking to MFC statically
#endif

	const DWORD BUFF_SIZE = 256;
	char Buffer[BUFF_SIZE];
	GetCurrentDirectory(BUFF_SIZE, Buffer); //Win 32 API


	ChunkHandler *filesplit = new ChunkHandler; // declare ChunkHandler object
	Updater *upd = new Updater; // declare Update object
	filesplit->setUpdater(upd); // set filesplit to use this Update object

	string filename = MARTUS_SETUP_FILENAME_BEFORE;
	filename.append("_001.cnk");
	filesplit->setSourceLocation(filename);
	filesplit->setDestinationFilename(MARTUS_SETUP_FILENAME_AFTER);

	// if first part doesn't exist but the output file exist, then run it.
	ostrstream filenamestr;
    filenamestr << filename << ends;

	bool setupFileExists = false;
	if( (_access( MARTUS_SETUP_FILENAME_AFTER, 0 )) == 0)
	{
		setupFileExists = true;
	}

	bool firstChunkExists = true;
	if( (_access( filenamestr.str(), 0 )) == -1)
	{
		firstChunkExists = false;
	}

	
	if( ! setupFileExists && firstChunkExists)
	{
		if(!filesplit->merge())
		{
			CString message = filesplit->getErrorMessage().c_str();

			// check if partial file was created
			if( (_access( MARTUS_SETUP_FILENAME_AFTER, 0 )) == 0 )
			{
				if( remove( MARTUS_SETUP_FILENAME_AFTER ) == -1 )
				{
					message += "\nA partial file ";
					message += MARTUS_SETUP_FILENAME_AFTER;
					message += " was created.";
				}
			}
			CMartusSetupDlg dlg;
			dlg.MessageBox( message, "Error", MB_OK | MB_ICONEXCLAMATION);

			return FALSE;
		}
	}

    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    ZeroMemory( &pi, sizeof(pi) );

    if( !CreateProcess( MARTUS_SETUP_FILENAME_AFTER,
        NULL,			  // Command line. 
        NULL,             // Process handle not inheritable. 
        NULL,             // Thread handle not inheritable. 
        FALSE,            // Set handle inheritance to FALSE. 
        0,                // No creation flags. 
        NULL,             // Use parent's environment block. 
        NULL,             // Use parent's starting directory. 
        &si,              // Pointer to STARTUPINFO structure.
        &pi )             // Pointer to PROCESS_INFORMATION structure.
    ) 
    {
		CString s_error;
		s_error.Format("%d", GetLastError());
		CString message = "Unable to start setup file:" + s_error + ": Missing files? =" + MARTUS_SETUP_FILENAME_BEFORE + "_001.cnk, _002.cnk etc...";
		CMartusSetupDlg dlg;
		dlg.MessageBox(message, "Error", MB_OK | MB_ICONEXCLAMATION);
    }

    // Wait until child process exits.
    WaitForSingleObject( pi.hProcess, INFINITE );

    // Close process and thread handles. 
    CloseHandle( pi.hProcess );
    CloseHandle( pi.hThread );

	// Since the dialog has been closed, return FALSE so that we exit the
	//  application, rather than start the application's message pump.
	return FALSE;
}
