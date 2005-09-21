/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.cppunit.wizards;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;
import org.eclipse.cdt.internal.cppunit.util.CppUnitLog;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;

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
		String [] testClassesNames=new String[testClasses.length];
		for(int i=0;i<testClasses.length;i++) {
			testClassesString+=testClasses[i].getPath().removeFirstSegments(1).makeRelative().toString()+" ";
			testClassesNames[i]=testClasses[i].getPath().removeFileExtension().lastSegment();
		}
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(testedProject.getProject());
		if(info != null) {			
			// Managed Project
			IManagedProject managedProject=info.getManagedProject();
			IConfiguration selected=info.getSelectedConfiguration();
			IConfiguration original=null;
			if(selected==null) {
				selected=info.getDefaultConfiguration();
			}
			if(selected.getName().endsWith("Tests")) {
				// The current config is a test one
				// Need a new one, to build a new executable, based on this one's parent
				original=selected.getParent();
			} else {
				original=selected;
			}
			// Create a New config based on original.
			String id=original.getId()+suiteName;
			IConfiguration newConfig=managedProject.getConfiguration(id);
			if(newConfig==null) {
				newConfig=managedProject.createConfigurationClone(original,id);
			}
			addCppUnit(newConfig,suiteName,testClassesNames);
			info.setSelectedConfiguration(newConfig);
			info.setDefaultConfiguration(newConfig);
			ManagedBuildManager.saveBuildInfo(testedProject.getProject(),true);			

		} else {
			//Std make project
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
			String defines="-DCPPUNIT_MAIN=main ";
			for(int y=0;y<testClassesNames.length;y++) {
				defines+="-DTEST_"+testClassesNames[y]+" ";
			}
			buf.append("\t$(CXX) "+defines+" "+testClassesString+" $(CPPUNIT_MAIN) $(CXXFLAGS) $(CPPUNIT_INCLUDE) $(SOURCES_UNDER_TEST) -o $@ $(CPPUNIT_LIBS)\n");
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
//			new CppUnitMakeTarget(testedProject.getProject(),res,suiteName);

			try {
				IMakeTargetManager targetManager=MakeCorePlugin.getDefault().getTargetManager();
				String[] id = targetManager.getTargetBuilders(testedProject.getProject());
				String targetBuildID = id[0];
				
				IMakeTarget target=targetManager.findTarget(testedProject.getProject(),suiteName);
				if(target!=null)
				{
					targetManager.removeTarget(target);
				}
				target = targetManager.createTarget(testedProject.getProject(),suiteName,targetBuildID);
				target.setBuildTarget(suiteName);
				targetManager.addTarget(testedProject.getProject(),target);
			} catch (CoreException e) {
				System.out.println("Cannot create build target "+suiteName+": "+e);
			}
		}
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
	
	private void addCppUnit(IConfiguration buildConfig,String suiteName,String [] testClassFile) {
		buildConfig.setArtifactName(suiteName);
		buildConfig.setName(suiteName);
		try {
			ITool [] tools=buildConfig.getTools();
			for(int i=0;i<tools.length;i++) {
				IOption [] options=tools[i].getOptions();
				for(int j=0;j<options.length;j++) {
					if(options[j].getValueType()==IOption.INCLUDE_PATH) {
						String [] listIncludes=new String [1];
						listIncludes[0]=fPage.getCppUnitIncludeLocation();
						buildConfig.setOption(tools[i],options[j],listIncludes);
					}
					else if(options[j].getValueType()==IOption.PREPROCESSOR_SYMBOLS) {
						String [] preproDirective=new String [1+testClassFile.length];
						preproDirective[0]="CPPUNIT_MAIN=main";
						for(int y=0;y<testClassFile.length;y++) {
							preproDirective[y+1]="TEST_"+testClassFile[y];
						}
						buildConfig.setOption(tools[i],options[j],preproDirective);
					}
					else if(options[j].getValueType()==IOption.OBJECTS) {
						String [] newObject=new String [1];
						newObject[0]=fPage.getCppUnitLibLocation();
						buildConfig.setOption(tools[i],options[j],newObject);
					}
					else if((Platform.getOS().equals(Platform.OS_WIN32))&&
							(options[j].getValueType()==IOption.LIBRARIES)) {
						String [] libList=new String[1];
						libList[0]="wsock32";
						buildConfig.setOption(tools[i],options[j],libList);
					}
				}
			}
		} catch(BuildException e) {
			CppUnitLog.error("Error configuring the IConfiguration with CppUnit",e);
		}
	}

}
