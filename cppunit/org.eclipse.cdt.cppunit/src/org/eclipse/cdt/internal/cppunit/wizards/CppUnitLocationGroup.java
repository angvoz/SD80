/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 12 mars 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */
package org.eclipse.cdt.internal.cppunit.wizards;

import java.io.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.cppunit.util.SWTUtil;
import org.eclipse.cdt.internal.cppunit.util.CppUnitStatus;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.boot.BootLoader;

public class CppUnitLocationGroup
{
	public final static String PAGE_NAME= "CppUnitLocations"; //$NON-NLS-1$
	public final static String CPPUNIT_INCLUDE_LOCATION= PAGE_NAME + ".include_location"; //$NON-NLS-1$
	public final static String CPPUNIT_LIB_LOCATION= PAGE_NAME + ".lib_location"; //$NON-NLS-1$

	private Label fCppUnitIncludeLabel;
	private Text fCppUnitIncludeText;
	private Button fCppUnitIncludeButton;
	static private String initIncString;

	private Label fCppUnitLibLabel;
	private Text fCppUnitLibText;
	private Button fCppUnitLibButton;
	static private String initLibString;

	private WizardPage fParent;

	static 
	{
		initLibString=null;
		initIncString=null;
	}
	public CppUnitLocationGroup(WizardPage parent)
	{
		fParent=parent;
	}
	public void init(IStructuredSelection o)
	{
		String defaultInc="";
		String defaultLib="";
		if(BootLoader.getOS().equals(BootLoader.OS_WIN32))
		{
			defaultInc="c:/cygwin/usr/local/include/cppunit/TestCase.h";
			defaultLib="c:/cygwin/usr/local/lib/libcppunit.a";
		}
		if(!(BootLoader.getOS().equals(BootLoader.OS_WIN32))
			&&!(BootLoader.getOS().equals(BootLoader.OS_UNKNOWN)))
		{
			defaultInc="/usr/local/include/cppunit/TestCase.h";
			defaultLib="/usr/local/lib/libcppunit.a";
		}
		if(initIncString==null)
		{
			initIncString=defaultInc;
		}
		if(initLibString==null)
		{
			initLibString=defaultLib;
		}
	}
	protected void handleFieldChanged(String fieldName)
	{
		if(fParent instanceof NewTestCaseCreationWizardPage)
		{
			NewTestCaseCreationWizardPage page=(NewTestCaseCreationWizardPage)fParent;
			page.handleFieldChanged(fieldName);
		}
		if(fParent instanceof NewTestSuiteCreationWizardPage)
		{
			NewTestSuiteCreationWizardPage page=(NewTestSuiteCreationWizardPage)fParent;
			page.handleFieldChanged(fieldName);
		}
	}
	protected void createCppUnitLocation(Composite composite,int nColums)
	{
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= 1;
		/*
		 * include
		 */
		fCppUnitIncludeLabel=new Label(composite,SWT.LEFT|SWT.WRAP);
		fCppUnitIncludeLabel.setFont(composite.getFont());
		fCppUnitIncludeLabel.setText(WizardMessages.getString("NewTestSuiteWizPage.cppunitincludelocation.label")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalSpan= 1;
		fCppUnitIncludeLabel.setLayoutData(gd);

		fCppUnitIncludeText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fCppUnitIncludeText.setFont(composite.getFont());
		fCppUnitIncludeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleFieldChanged(CPPUNIT_INCLUDE_LOCATION);
			}
		});
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColums - 2;
		fCppUnitIncludeText.setLayoutData(gd);
		
		fCppUnitIncludeButton= new Button(composite, SWT.PUSH);
		fCppUnitIncludeButton.setText(WizardMessages.getString("NewTestSuiteWizPage.cppunitincludelocation.browse")); //$NON-NLS-1$
		fCppUnitIncludeButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				String inc=chooseIncludeDirectory();
				if(inc!=null)
				{
					Path p=new Path(inc);
					fCppUnitIncludeText.setText(p.toString());
					handleFieldChanged(CPPUNIT_INCLUDE_LOCATION);
				}
			}
			public void widgetSelected(SelectionEvent e)
			{
				String inc=chooseIncludeDirectory();
				if(inc!=null)
				{
					Path p=new Path(inc);
					fCppUnitIncludeText.setText(p.toString());
					handleFieldChanged(CPPUNIT_INCLUDE_LOCATION);
				}
			}
		});	
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= 1;
		gd.heightHint = SWTUtil.getButtonHeigthHint(fCppUnitIncludeButton);
		gd.widthHint = SWTUtil.getButtonWidthHint(fCppUnitIncludeButton);		
		fCppUnitIncludeButton.setLayoutData(gd);

		fCppUnitIncludeLabel.setEnabled(true);
		fCppUnitIncludeButton.setEnabled(true);
		fCppUnitIncludeText.setEnabled(true);
		fCppUnitIncludeText.setText(initIncString);

		/*
		 * libs
		 */
		fCppUnitLibLabel=new Label(composite,SWT.LEFT|SWT.WRAP);
		fCppUnitLibLabel.setFont(composite.getFont());
		fCppUnitLibLabel.setText(WizardMessages.getString("NewTestSuiteWizPage.cppunitliblocation.label")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalSpan= 1;
		fCppUnitLibLabel.setLayoutData(gd);

		fCppUnitLibText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fCppUnitLibText.setFont(composite.getFont());
		fCppUnitLibText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleFieldChanged(CPPUNIT_LIB_LOCATION);
			}
		});
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColums - 2;
		fCppUnitLibText.setLayoutData(gd);
		
		fCppUnitLibButton= new Button(composite, SWT.PUSH);
		fCppUnitLibButton.setText(WizardMessages.getString("NewTestSuiteWizPage.cppunitliblocation.browse")); //$NON-NLS-1$
		fCppUnitLibButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				String lib=chooseLibLocation();
				if(lib!=null)
				{
					Path p=new Path(lib);
					fCppUnitLibText.setText(p.toString());
					handleFieldChanged(CPPUNIT_LIB_LOCATION);
				}
			}
			public void widgetSelected(SelectionEvent e)
			{
				String lib=chooseLibLocation();
				if(lib!=null)
				{
					Path p=new Path(lib);
					fCppUnitLibText.setText(p.toString());
					handleFieldChanged(CPPUNIT_LIB_LOCATION);
				}
			}
		});	
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= 1;
		gd.heightHint = SWTUtil.getButtonHeigthHint(fCppUnitLibButton);
		gd.widthHint = SWTUtil.getButtonWidthHint(fCppUnitLibButton);		
		fCppUnitLibButton.setLayoutData(gd);

		fCppUnitLibLabel.setEnabled(true);
		fCppUnitLibButton.setEnabled(true);
		fCppUnitLibText.setEnabled(true);
		fCppUnitLibText.setText(initLibString);

		handleFieldChanged(CPPUNIT_INCLUDE_LOCATION);
		handleFieldChanged(CPPUNIT_LIB_LOCATION);

	}
	private String chooseIncludeDirectory() {
		FileDialog dialog= new FileDialog(fParent.getShell(),SWT.OPEN);
		String filterExtension[] = {"*.h","*.*"};
		dialog.setFilterExtensions(filterExtension);
		dialog.setText(WizardMessages.getString("NewTestSuiteWizPage.cppunitincludelocation.chooselocation.title"));
		return(dialog.open());
	}
	private String chooseLibLocation() {
		FileDialog dialog= new FileDialog(fParent.getShell(),SWT.OPEN);
		String libName=System.mapLibraryName("cppunit");
		String ext=(new Path(libName)).getFileExtension();
		String filterExtension[] = {"*.a","*."+ext,"*.*"};
		dialog.setFilterExtensions(filterExtension);
		dialog.setText(WizardMessages.getString("NewTestSuiteWizPage.cppunitliblocation.chooselocation.title"));
		return(dialog.open());
	}
	protected IStatus cppUnitIncludeLocationChanged()
	{
		CppUnitStatus status= new CppUnitStatus();
		String inc=fCppUnitIncludeText.getText().trim();
		if (inc.length() == 0) {
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.cppunitincludelocation.name_empty")); //$NON-NLS-1$
			return status;
		}
		initIncString=inc;
		File f=new File(inc);
		if(f.exists()==false)
		{
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.cppunitincludelocation.d_not_exist")); //$NON-NLS-1$
			return status;
		}
		Path path=new Path(inc);
		String lastSegment=path.lastSegment();
		if((lastSegment==null)||((lastSegment.equals("TestCase.h"))==false))
		{
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.cppunitincludelocation.is_not_correct")); //$NON-NLS-1$
			return status;
		}
		return status;		
	}
	protected IStatus cppUnitLibLocationChanged()
	{
		CppUnitStatus status= new CppUnitStatus();
		String lib=fCppUnitLibText.getText().trim();
		if (lib.length() == 0) {
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.cppunitliblocation.name_empty")); //$NON-NLS-1$
			return status;
		}
		initLibString=lib;
		File f=new File(lib);
		if(f.exists()==false)
		{
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.cppunitliblocation.d_not_exist")); //$NON-NLS-1$
			return status;
		}
//  ToBeDone:  Parse the found binary.
//  Check if it contains something specific to CppUnit

		Path path=new Path(lib);
		String libName=System.mapLibraryName("cppunit");
		Path p=new Path(libName);
		String expectedExt=p.getFileExtension();
		String obtainedExt=path.getFileExtension();
		if(((obtainedExt.equals(expectedExt))==false)&&
		   ((obtainedExt.equals("a"))==false))
		{
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.cppunitliblocation.is_not_a_lib")); //$NON-NLS-1$
			return status;
		}
		return status;		
	}
	public String getCppUnitIncludeLocation()
	{
		Path path=new Path(fCppUnitIncludeText.getText().trim());
		IPath p=path.removeLastSegments(2);
		return(p.toString());
	}
	public String getCppUnitLibLocation()
	{
		return(fCppUnitLibText.getText().trim());
	}
	public Text getIncludeTextWidget()
	{
		return(fCppUnitIncludeText);
	}
	public Text getLibTextWidget()
	{
		return(fCppUnitLibText);
	}
}
