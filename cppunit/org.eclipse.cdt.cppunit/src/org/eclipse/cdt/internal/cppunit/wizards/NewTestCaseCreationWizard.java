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

import java.net.MalformedURLException;
import java.util.*;
import java.io.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;

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
		String mainSrc=CppUnitPlugin.getPluginDirectory()+"lib/RemoteTestRunner.cpp";
		// Retrieve parameters from the page 1
		ICProject testedProject=fPage.getCProject();
		ITranslationUnit testedFile= fPage.getFileToTest();
		IPath testFile= fPage.getTestFile();
		String testClassName=testFile.removeFileExtension().lastSegment();
		String testClassRoot=testFile.removeFileExtension().toString();

		StringBuffer buf=new StringBuffer();
//		IFile f2=f.getFile(); // Get Source file to test ??
		buf.append("/** \n");
		buf.append(" * Generated CppUnit test driver template.\n");
		buf.append(" * To build it, add the following line at the end of\n");		buf.append(" * your existing Makefile:\n");	
		buf.append(" *    include "+testClassRoot+".mk\n");
		buf.append(" * Build the "+testClassRoot+" target from the Make Target view\n");
		buf.append(" */\n\n");
		buf.append("#include \"cppunit/extensions/HelperMacros.h\"\n");		
//		buf.append("\n#include \""+f2.getLocation().toOSString()+"\"\n");
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
		for(int i=0;i<checkedCode.length;i++) 
		{
			String testMethod=testMethodNames[i];
			buf.append("\tvoid test"+testMethod+"()\n\t{\n");

//String retType=checkedCode[i].getReturnType();
//if((retType.indexOf("void")==-1)&&(retType!=""))
//{
//	buf.append("\t\t"+retType+" retValue;\n");
//	String [] parameters=checkedCode[i].getParameterTypes();
//	for(int j=0;i<checkedCode[i].getNumberOfParameters();j++)
//	{
//		buf.append("\t\t"+parameters[j]+" p"+(new Integer(j)).toString()+";\n");
//	}
//	buf.append("\t\tretValue="+checkedCode[i].getSignature()+";\n");
//}

			buf.append("\t\tCPPUNIT_ASSERT(true);\n");
			buf.append("\t}\n");
		}
		buf.append("\tCPPUNIT_TEST_SUITE("+testClassName+");\n");
		for(int i=0;i<testMethodNames.length;i++) 
		{
			String testMethod=testMethodNames[i];
			buf.append("\tCPPUNIT_TEST(test"+testMethod+");\n");
		}
		buf.append("\tCPPUNIT_TEST_SUITE_END();\n");
		buf.append("};\n");
		buf.append("CPPUNIT_TEST_SUITE_REGISTRATION("+testClassName+");\n");

		/*
		 * Generate the test driver file
		 */
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath p0=root.getLocation();
		IPath path=p0.append(testedProject.getPath()).append(testFile);
		try
		{
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
		 * Generate a piece of make
		 */
		buf=new StringBuffer();
		String libFileName=fPage.getCppUnitLibLocation();
		String includeMacros=fPage.getCppUnitIncludeLocation();

		buf.append("CPPUNIT_LIBS="+libFileName+"\n");
		buf.append("CPPUNIT_INCLUDE=-I"+includeMacros+"\n");
		buf.append("CPPUNIT_MAIN="+mainSrc+"\n");
		buf.append("SOURCES_UNDER_TEST="+testedFile.getPath().makeRelative().removeFirstSegments(1)+"\n");
		buf.append(testClassRoot+": "+testFile.toString()+" "+"$(SOURCES_UNDER_TEST)\n");
		buf.append("\t$(CXX) "+testFile.toString()+" $(CPPUNIT_MAIN) $(CXXFLAGS) $(CPPUNIT_INCLUDE) $(SOURCES_UNDER_TEST) -o $@ $(CPPUNIT_LIBS)\n");
		try
		{
			path=p0.append(testedProject.getPath()).append(testFile.removeFileExtension().addFileExtension("mk"));
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
		 * Refresh and Open the editor
		 */
		try
		{
			testedProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,null);
		}
		catch(CoreException e)
		{
			System.out.println("Exception: "+e);
		}
		IResource res=testedProject.getProject().findMember(testFile.removeFileExtension().addFileExtension("mk"));
		if(res!=null) selectAndReveal(res);
		new CppUnitMakeTarget(testedProject.getProject(),res,testClassRoot);

		res=testedProject.getProject().findMember(testFile);
		if(res!=null)
		{
				selectAndReveal(res);
				openResource(res);
		}
		
		fPage.saveWidgetValues();
		fPage2.saveWidgetValues();

		try {
			fPage.createTaskMarkers();
		}
		catch(CoreException e) {}

		// Retrieve Parameters from page 2 (the function list to test)
		
//		if (finishPage(fPage.getRunnable())) {
//			IType newClass= fPage.getCreatedType();
//
//			IStructure newClass=fPage.getCreatedType();
//			ICFile cu=newClass.getCompilationUnit();
////			ICompilationUnit cu= newClass.getCompilationUnit();				
//
//			if (cu.isWorkingCopy()) {
//				cu= (ICompilationUnit)cu.getOriginalElement();
//			}	
//			IResource resource= cu.getResource();
//			if (resource != null) {
//				selectAndReveal(resource);
//				openResource(resource);
//			}
//			fPage.saveWidgetValues();
//			fPage2.saveWidgetValues();
//			
//			return true;
//		}
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

}
