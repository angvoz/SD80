/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.cppunit.wizards;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;

/**
 * A wizard for creating test suites.
 */
public class NewTestSuiteCreationWizard extends CppUnitWizard {

	private NewTestSuiteCreationWizardPage fPage;
	
	public NewTestSuiteCreationWizard() {
		super();
		setWindowTitle(WizardMessages.getString("Wizard.title.new")); //$NON-NLS-1$
		initDialogSettings();
	}

	/*
	 * @see Wizard#createPages
	 */	
	public void addPages() {
		super.addPages();
		fPage= new NewTestSuiteCreationWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}	

	/*
	 * @see Wizard#performFinish
	 */		
	public boolean performFinish() 
	{
		String mainSrc=CppUnitPlugin.getPluginDirectory()+"lib/RemoteTestRunner.cpp";
		String suiteName=fPage.getTypeName();
		ICProject testedProject=fPage.getCProject();
		String testClassesString=new String("");
		ITranslationUnit [] testClasses=fPage.getCheckedTestClasses();
		for(int i=0;i<testClasses.length;i++)
		{
			testClassesString+=testClasses[i].getPath().removeFirstSegments(1).makeRelative().toString()+" ";
		}
		/*
		 * Generate a piece of make
		 */
		StringBuffer buf=new StringBuffer();
		String libFileName=fPage.getCppUnitLibLocation();
		String includeMacros=fPage.getCppUnitIncludeLocation();

		buf.append("CPPUNIT_LIBS="+libFileName+"\n");
		buf.append("CPPUNIT_INCLUDE=-I"+includeMacros+"\n");
		buf.append("CPPUNIT_MAIN="+mainSrc+"\n");
		buf.append("SOURCES_UNDER_TEST=\n");
		buf.append(suiteName+": "+testClassesString+" "+"$(SOURCES_UNDER_TEST)\n");
		buf.append("\t$(CXX) "+testClassesString+" $(CPPUNIT_MAIN) $(CXXFLAGS) $(CPPUNIT_INCLUDE) $(SOURCES_UNDER_TEST) -o $@ $(CPPUNIT_LIBS)\n");
		try
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath p0=root.getLocation();
			IPath path=p0.append(testedProject.getPath()).append(suiteName+".mk");
			FileOutputStream file=new FileOutputStream(path.toString());
			file.write(buf.toString().getBytes());
			file.close();
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("File Not Found Exception "+e);
		}
		catch (IOException e) 
		{
			System.out.println("IO Exception "+e);
		}
		/*
		 * Refresh
		 */
		try
		{
			testedProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,null);
		}
		catch(CoreException e)
		{
			System.out.println("Exception: "+e);
		}

		IResource res=testedProject.getProject().findMember(new Path(suiteName+".mk"));
		if(res!=null) selectAndReveal(res);
		new CppUnitMakeTarget(testedProject.getProject(),res,suiteName);

		
//		IPackageFragment pack= fPage.getPackageFragment();
//		String filename= fPage.getTypeName() + ".java"; //$NON-NLS-1$
//		ICompilationUnit cu= pack.getCompilationUnit(filename);
//		if (cu.exists()) {
//			IEditorPart cu_ep= EditorUtility.isOpenInEditor(cu);
//			if (cu_ep != null && cu_ep.isDirty()) {
//				boolean saveUnsavedChanges= 
//					MessageDialog.openQuestion(fPage.getShell(), 
//						WizardMessages.getString("NewTestSuiteWiz.unsavedchangesDialog.title"), //$NON-NLS-1$
//						WizardMessages.getFormattedString("NewTestSuiteWiz.unsavedchangesDialog.message", //$NON-NLS-1$
//						filename));  
//				if (saveUnsavedChanges) {
//					ProgressMonitorDialog progressDialog= new ProgressMonitorDialog(fPage.getShell());
//					try {
//						progressDialog.run(false, false, getRunnableSave(cu_ep));
//					} catch (Exception e) {
//						JUnitPlugin.log(e);
//					}
//				}
//			}
//			IType suiteType= cu.getType(fPage.getTypeName());
//			IMethod suiteMethod= suiteType.getMethod("suite", new String[] {}); //$NON-NLS-1$
//			if (suiteMethod.exists()) {
//				try {
//				ISourceRange range= suiteMethod.getSourceRange();
//				IBuffer buf= cu.getBuffer();
//				String originalContent= buf.getText(range.getOffset(), range.getLength());
//				int start= originalContent.indexOf(NewTestSuiteCreationWizardPage.START_MARKER);
//				if (start > -1) {
//					int end= originalContent.indexOf(NewTestSuiteCreationWizardPage.END_MARKER, start);
//					if (end < 0) {
//						fPage.cannotUpdateSuiteError();
//						return false;
//					}
//				} else {
//					fPage.cannotUpdateSuiteError();
//					return false;
//				}
//				} catch (JavaModelException e) {
//					JUnitPlugin.log(e);
//					return false;
//				}
//			}
//		}
		
		if (finishPage(fPage.getRunnable())) {
			if (!fPage.hasUpdatedExistingClass())
				System.out.println("I Have to Post It !!");
			fPage.saveWidgetValues();				
			return true;
		}

		return false;		
	}

//	protected void postCreatingType() {
//		System.out.println("PostCreatingType");
//		IType newClass= fPage.getCreatedType();
//		if (newClass == null)
//			return;
//		ICompilationUnit cu= newClass.getCompilationUnit();
//		if (cu.isWorkingCopy()) {
//			cu= (ICompilationUnit) cu.getOriginalElement();
//			//added here
//		}
//		IResource resource= cu.getResource();
//		if (resource != null) {
//			selectAndReveal(resource);
//			openResource(resource);
//		}
//	}

	public NewTestSuiteCreationWizardPage getPage() {
		return fPage;
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(CppUnitPlugin.makeIconFileURL("wizban/newtest_wiz.gif")); //$NON-NLS-1$
			setDefaultPageImageDescriptor(id);
	} catch (MalformedURLException e) {
			// Should not happen.  Ignore.
		}
	}

	public IRunnableWithProgress getRunnableSave(final IEditorPart cu_ep) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (monitor == null) {
						monitor= new NullProgressMonitor();
					}
					cu_ep.doSave(monitor);
			}
		};
	}
}
