// MartusSetup.h : main header file for the MARTUSSETUP application
//

#if !defined(AFX_MARTUSSETUP_H__000BABC6_9674_48C5_B21E_2F5CF7B5DCC5__INCLUDED_)
#define AFX_MARTUSSETUP_H__000BABC6_9674_48C5_B21E_2F5CF7B5DCC5__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

/////////////////////////////////////////////////////////////////////////////
// CMartusSetupApp:
// See MartusSetup.cpp for the implementation of this class
//

class CMartusSetupApp : public CWinApp
{
public:
	CMartusSetupApp();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CMartusSetupApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CMartusSetupApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_MARTUSSETUP_H__000BABC6_9674_48C5_B21E_2F5CF7B5DCC5__INCLUDED_)
