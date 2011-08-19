// MartusSetupDlg.h : header file
//

#if !defined(AFX_MARTUSSETUPDLG_H__43EA4A37_6563_4740_BBB6_6B04CD815D83__INCLUDED_)
#define AFX_MARTUSSETUPDLG_H__43EA4A37_6563_4740_BBB6_6B04CD815D83__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

/////////////////////////////////////////////////////////////////////////////
// CMartusSetupDlg dialog

class CMartusSetupDlg : public CDialog
{
// Construction
public:
	CMartusSetupDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	//{{AFX_DATA(CMartusSetupDlg)
	enum { IDD = IDD_MARTUSSETUP_DIALOG };
		// NOTE: the ClassWizard will add data members here
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CMartusSetupDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	//{{AFX_MSG(CMartusSetupDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_MARTUSSETUPDLG_H__43EA4A37_6563_4740_BBB6_6B04CD815D83__INCLUDED_)
