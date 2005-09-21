/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.wizards;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author mlescuye
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * A wizard for creating test cases.
 */
public class NewTestCaseCreationWizard extends CppUnitWizard {

	private NewTestCaseCreationWizardPage fPage;
	private NewTestCaseCreationWizardPage2 fPage2;

	public NewTestCaseCreationWizard() {
		super();
		setWindowTitle(WizardMessages.getString("Wizard.title.new")); //$NON-NLS-1$
		initDialogSettings();
	}

	protected void initializeDefaultPageImageDescriptor() {
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(CppUnitPlugin.makeIconFileURL("wizban/newtest_wiz.gif")); //$NON-NLS-1$
			setDefaultPageImageDescriptor(id);
	} catch (MalformedURLException e) {
			// Should not happen.  Ignore.
		}
	}


	/*
	 * @see Wizard#createPages
	 */	
	public void addPages()
	{
		super.addPages();
		fPage= new NewTestCaseCreationWizardPage();
		fPage2= new NewTestCaseCreationWizardPage2(fPage);
		addPage(fPage);
		fPage.init(getSelection(),fPage2);
		addPage(fPage2);
	}	
	
	public boolean performFinish()
	{
		CoreModel coreModel=CCorePlugin.getDefault().getCoreModel();
		String mainSrc=CppUnitPlugin.getPluginDirectory()+"lib/RemoteTestRunner.cpp";

		// Retrieve parameters from the page 1
		ICProject testedProject=fPage.getCProject();
		ITranslationUnit testedFile= fPage.getFileToTest();
		IPath testFile= fPage.getTestFile();
		
		generateTestDriver(testedProject,testFile);
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(testedProject.getProject());
		if(info != null) {			
			// Managed Build Project
			copyCppUnitMain(testedProject,mainSrc);

			IManagedProject managedProject=info.getManagedProject();
			IConfiguration selected=info.getSelectedConfiguration();
			IConfiguration original=null;
			if(selected==null) {
				selected=info.getDefaultConfiguration();
			}
			if(selected.getName().endsWith("Test")) {
				// The current config is a test one
				// Need a new one, to build a new executable, based on this one's parent
				original=selected.getParent();
			} else {
				original=selected;
			}
			// Create a New config based on original.
			String id=original.getId()+testFile.removeFileExtension().lastSegment();
			IConfiguration newConfig=managedProject.getConfiguration(id);
			if(newConfig==null) {
				newConfig=managedProject.createConfigurationClone(original,id);
			}
			addCppUnit(newConfig,testFile.removeFileExtension().lastSegment());
			info.setSelectedConfiguration(newConfig);
			info.setDefaultConfiguration(newConfig);
			ManagedBuildManager.saveBuildInfo(testedProject.getProject(),true);			
		} else {
			// Std make project
			generateMake(testedProject,mainSrc,testedFile,testFile);
			String testClassRoot=testFile.removeFileExtension().toString();
			try {
				IMakeTargetManager targetManager=MakeCorePlugin.getDefault().getTargetManager();
				String[] id = targetManager.getTargetBuilders(testedProject.getProject());
				String targetBuildID = id[0];
				IMakeTarget target=targetManager.findTarget(testedProject.getProject(),testClassRoot);
				if(target!=null) {
					targetManager.removeTarget(target);
				}
				target = targetManager.createTarget(testedProject.getProject(),testClassRoot,targetBuildID);
				target.setBuildTarget(testClassRoot);
				targetManager.addTarget(testedProject.getProject(),target);
			} catch (CoreException e) {
				CppUnitLog.error("Cannot create build target "+testClassRoot,e);
			}
		}
		
		/*
		 * Refresh and Open the editor
		 */
		try
		{
			testedProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,null);
		}
		catch(CoreException e)
		{
			CppUnitLog.error("Error refreshing project",e);
		}
		IResource res=testedProject.getProject().findMember(testFile);
		if(res!=null)
		{
				selectAndReveal(res);
				openResource(res);
		}
		
		fPage.saveWidgetValues();
		fPage2.saveWidgetValues();

		try {
			fPage.createTaskMarkers();
		} catch(CoreException e) {
			CppUnitLog.error("Error creating tasks markers",e);
		}
		return true;
	}

	String [] checkOverload(ICElement [] element)
	{
		int len=element.length;
		String [] ret=new String[element.length];
		for(int i=0;i<len;i++)
		{
			ret[i]=buildCompilableName(element[i].getElementName());
		}
		for(int i=0;i<len;i++)
		{
			int index=1;
			for(int j=i;j<len;j++)
			{
				if((i!=j)&&(ret[i].equals(ret[j])))
				{
					// Overload !
					ret[j]+="_"+(new Integer(index)).toString();
					index++;
				}
			}
		}
		return ret;
	}
	public String buildCompilableName(String i)
	{
		HashMap h=new HashMap();
		h.put("operator+","PlusOperator");
		h.put("operator-","MinusOperator");
		h.put("operator*","MultOperator");
		h.put("operator/","DivOperator");
		h.put("operator%","ModuloOperator");
		h.put("operator^","BitWiseXorOperator");
		h.put("operator&","BitWiseAndOperator");
		h.put("operator|","BitWiseOrOperator");
		h.put("operator~","BitWiseComplementOperator");
		h.put("operator!","LogicalNotOperator");
		h.put("operator=","AssignOperator");
		h.put("operator<","LowerThanOperator");
		h.put("operator>","GreaterThanOperator");
		h.put("operator+=","InPlaceAddOperator");
		h.put("operator-=","InPlaceSubOperator");
		h.put("operator*=","InPlaceMulOperator");
		h.put("operator/=","InPlaceDivOperator");
		h.put("operator%=","InPlaceModOperator");
		h.put("operator^=","InPlaceBitWiseXorOperator");
		h.put("operator&=","InPlaceBitWiseAndOperator");
		h.put("operator|=","InPlaceBitWiseOrOperator");
		h.put("operator<<","LeftShiftOperator");
		h.put("operator>>","RightShiftOperator");
		h.put("operator<<=","InPlaceLeftShiftOperator");
		h.put("operator>>=","InPlaceRightShiftOperator");
		h.put("operator==","EqualOperator");
		h.put("operator!=","NotEqualOperator");
		h.put("operator<=","LowerOrEqualOperator");
		h.put("operator>=","GreaterOrEqualOperator");
		h.put("operator&&","LogicalAndOperator");
		h.put("operator||","LogicalOrOperator");
		h.put("operator++","PostIncOperator");
		h.put("operator--","PostDecOperator");
		h.put("operator,","CommaOperator");
		h.put("operator->*","PointerMemberAccessOperator");
		h.put("operator->","MemberAccessOperator");
		h.put("operator()","FunctionCallOperator");
		h.put("operator[]","IndexOperator");
		h.put("operatornew","NewOperator");
		h.put("operatornew[]","NewArrayOperator");
		h.put("operatordelete","DeleteOperator");
		h.put("operatordelete[]","DeleteArrayOperator");

		// Remove white spaces
		String n="";
		for(int j=0;j<i.length();j++)
		{
			char c=i.charAt(j);
			if((c!=' ')&&(c!='\t'))
			{
				n+=c;
			}
		}
		i=n;
		// Is the element an operator ?
		String ret=i.replace(':','_');
		int index=i.lastIndexOf("operator");
		if(index!=-1)
		{
			// It is an operator
			int len=i.length();
			String operatorRep=i.substring(index,len);
			String rootName=i.substring(0,index);
			String operatorName=(String)h.get(operatorRep);
			if(operatorName!=null)
			{
				ret=rootName.replace(':','_')+operatorName;
			}
		}
		h=null;
		return ret.replace('~','D');
	}
	private void generateTestDriver(ICProject cProject,IPath testDriverFile) {
		String testClassName=testDriverFile.removeFileExtension().lastSegment();
		String testClassRoot=testDriverFile.removeFileExtension().toString();

		StringBuffer buf=new StringBuffer();
		buf.append("/** \n");
		buf.append(" * Generated CppUnit test driver template.\n");
		buf.append(" * To build it, add the following line at the end of\n");
		buf.append(" * your existing Makefile:\n");	
		buf.append(" *    include "+testClassRoot+".mk\n");
		buf.append(" * Build the "+testClassRoot+" target from the Make Target view\n");
		buf.append(" */\n\n");
		buf.append("#ifdef TEST_"+testClassName+"\n");
		buf.append("#include \"cppunit/extensions/HelperMacros.h\"\n");		
		buf.append("\nclass "+testClassName+" : public CppUnit::TestFixture {\n");
		buf.append("private:\n");
		buf.append("public:\n");
		/* Constructor */
//		buf.append("\t"+testClassName+"()\n\t{\n\t}\n");
		buf.append("\tvoid setUp()\n\t{\n");
		buf.append("\t}\n");
		buf.append("\tvoid tearDown()\n\t{\n");
		buf.append("\t}\n");

		IFunctionDeclaration [] checkedCode=fPage2.getCheckedFunctions();
		String [] testMethodNames=checkOverload(checkedCode);
		for(int i=0;i<checkedCode.length;i++) {
			String testMethod=testMethodNames[i];
			buf.append("\tvoid test"+testMethod+"()\n\t{\n");
			buf.append("\t\tCPPUNIT_ASSERT(true);\n");
			buf.append("\t}\n");
		}
		buf.append("\tCPPUNIT_TEST_SUITE("+testClassName+");\n");
		for(int i=0;i<testMethodNames.length;i++)  {
			String testMethod=testMethodNames[i];
			buf.append("\tCPPUNIT_TEST(test"+testMethod+");\n");
		}
		buf.append("\tCPPUNIT_TEST_SUITE_END();\n");
		buf.append("};\n");
		buf.append("CPPUNIT_TEST_SUITE_REGISTRATION("+testClassName+");\n");
		buf.append("#endif\n");

		/*
		 * Generate the test driver file
		 */
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath p0=root.getLocation();
		IPath path=p0.append(cProject.getPath()).append(testDriverFile);
		FileOutputStream file=null;
		try {
			file=new FileOutputStream(path.toString());
			file.write(buf.toString().getBytes());
		}
		catch (FileNotFoundException e) {
			CppUnitLog.error("Error creating the test driver file",e);
		}
		catch (IOException e) {
			CppUnitLog.error("Error creating the test driver file",e);
		} finally {
			if(file!=null) {
				try {
					file.close();
				} catch(Exception e) {
					CppUnitLog.error("Error closing file",e);
				}
			}
		}
	}
	
	private void generateMake(ICProject cProject,String mainSource,ITranslationUnit testedFile,IPath testFile) {
		StringBuffer buf=new StringBuffer();
		String libFileName=fPage.getCppUnitLibLocation();
		String includeMacros=fPage.getCppUnitIncludeLocation();
		String testClassName=testFile.removeFileExtension().lastSegment();
		String testClassRoot=testFile.removeFileExtension().toString();

		buf.append("CPPUNIT_LIBS="+libFileName+"\n");
		buf.append("CPPUNIT_INCLUDE=-I"+includeMacros+"\n");
		buf.append("CPPUNIT_MAIN="+mainSource+"\n");
		buf.append("SOURCES_UNDER_TEST="+testedFile.getPath().makeRelative().removeFirstSegments(1)+"\n");
		buf.append(testClassRoot+": "+testFile.toString()+" "+"$(SOURCES_UNDER_TEST)\n");
		buf.append("\t$(CXX) -DCPPUNIT_MAIN=main -DTEST_"+testClassName+" "+testFile.toString()+" $(CPPUNIT_MAIN) $(CXXFLAGS) $(CPPUNIT_INCLUDE) $(SOURCES_UNDER_TEST) -o $@ $(CPPUNIT_LIBS)\n");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		FileOutputStream file=null;
		IPath p0=root.getLocation();
		try {
			IPath path=p0.append(cProject.getPath()).append(testFile.removeFileExtension().addFileExtension("mk"));
			file=new FileOutputStream(path.toString());
			file.write(buf.toString().getBytes());
		} catch (FileNotFoundException e) {
			CppUnitLog.error("Error creating the piece of make",e);
		} catch (IOException e) {
			CppUnitLog.error("Error creating the piece of make",e);
		} finally {
			if(file!=null) {
				try {
					file.close();
				} catch(Exception e) {
					CppUnitLog.error("Error closing file",e);
				}
			}
		}
	}
	
	private void addCppUnit(IConfiguration buildConfig,String testClassFile) {
		buildConfig.setArtifactName(testClassFile);
		buildConfig.setName(testClassFile);
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
						String [] preproDirective=new String [2];
						preproDirective[0]="CPPUNIT_MAIN=main";
						preproDirective[1]="TEST_"+testClassFile;
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
	public void copyCppUnitMain(ICProject cproject,String location) {
		IPath path=new Path(location);
		String fileName=path.lastSegment();
		IResource r=cproject.getProject().findMember(fileName);
		FileInputStream  fis=null;
		FileOutputStream fos=null;
		byte [] buf=new byte[10240];
		if(r==null) {
			try {
				fis=new  FileInputStream(location);
				fos=new FileOutputStream(cproject.getProject().getLocation().toString()+"/"+fileName);
				int avail=fis.read(buf);
				while(avail>0) {
					fos.write(buf,0,avail);
					avail=fis.read(buf);
				}
			} catch(Exception e) {
				CppUnitLog.error("Writing file",e);
			} finally {
				buf=null;
				try { if(fis!=null) { fis.close();}
				} catch (Exception e) {CppUnitLog.error("Error closing input file",e);}
				try { if(fos!=null) { fos.close();}
				} catch (Exception e) {CppUnitLog.error("Error closing output file",e);}
			}
		}
	}
}
