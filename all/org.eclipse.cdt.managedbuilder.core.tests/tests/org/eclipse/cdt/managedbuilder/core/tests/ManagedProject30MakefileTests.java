/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/

/**********************************************************************
 * These tests are for a 3.0 style tool integration.
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.projectconverter.UpdateManagedProjectManager;
import org.eclipse.cdt.managedbuilder.testplugin.CTestPlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class ManagedProject30MakefileTests extends TestCase {
	public static final String MBS_TEMP_DIR = "MBSTemp";

	static boolean pathVariableCreated = false;
	
	public ManagedProject30MakefileTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedProject30MakefileTests.class.getName());
		
		suite.addTest(new ManagedProject30MakefileTests("test30SingleFileExe"));
		suite.addTest(new ManagedProject30MakefileTests("test30TwoFileSO"));
		suite.addTest(new ManagedProject30MakefileTests("test30MultiResConfig"));
		suite.addTest(new ManagedProject30MakefileTests("test30LinkedLib"));
		//  TODO: testLinkedFolder fails intermittently saying that it cannot find
		//        the makefiles to compare.  This appears to be a test set issue,
		//        rather than an MBS functionality issue
		//suite.addTest(new ManagedProject30MakefileTests("test30LinkedFolder"));
		suite.addTest(new ManagedProject30MakefileTests("test30CopyandDeploy"));
		suite.addTest(new ManagedProject30MakefileTests("test30DeleteFile"));
		suite.addTest(new ManagedProject30MakefileTests("test30_1"));
		suite.addTest(new ManagedProject30MakefileTests("test30_2"));
		
		return suite;
	}

	private IProject[] createProject(String projName, IPath location, String projectTypeId, boolean containsZip){
		File testDir = CTestPlugin.getFileInPlugin(new Path("resources/test30Projects/" + projName));
		if(testDir == null) {
			fail("Test project directory " + testDir.getName() + " is missing.");
			return null;
		}

		ArrayList projectList = null;
		if (containsZip) {
			File projectZips[] = testDir.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					if(pathname.isDirectory())
						return false;
					return true;
				}
			});
			
			projectList = new ArrayList(projectZips.length);
			for(int i = 0; i < projectZips.length; i++){
				try{
					String projectName = projectZips[i].getName();
					if(!projectName.endsWith(".zip"))
						continue;
					
					projectName = projectName.substring(0,projectName.length()-".zip".length());
					if(projectName.length() == 0)
						continue;
					IProject project = ManagedBuildTestHelper.createProject(projectName, projectZips[i], location, projectTypeId);
					if(project != null)
						projectList.add(project);
				}
				catch(Exception e){
				}
			}
			if(projectList.size() == 0) {
				fail("No projects found in test project directory " + testDir.getName() + ".  The .zip file may be missing or corrupt.");
				return null;
			}
		} else {
			try{
				IProject project = ManagedBuildTestHelper.createProject(projName, null, location, projectTypeId);
				if(project != null)
					projectList = new ArrayList(1);
					projectList.add(project);
			} catch(Exception e){}
		}
		
		return (IProject[])projectList.toArray(new IProject[projectList.size()]);
	}
	
	private IProject[] createProjects(String projName, IPath location, String projectTypeId, boolean containsZip) {
		
		//  In case the projects need to be updated...
		IOverwriteQuery queryALL = new IOverwriteQuery(){
			public String queryOverwrite(String file) {
				return ALL;
			}};
		IOverwriteQuery queryNOALL = new IOverwriteQuery(){
			public String queryOverwrite(String file) {
				return NO_ALL;
			}};
		
		UpdateManagedProjectManager.setBackupFileOverwriteQuery(queryALL);
		UpdateManagedProjectManager.setUpdateProjectQuery(queryALL);
		
		IProject projects[] = createProject(projName, location, projectTypeId, containsZip);
		return projects;
	}
		
	private void buildProjects(IProject projects[], IPath[] files) {	
		if(projects == null || projects.length == 0)
			return;
				
		boolean succeeded = true;
		for(int i = 0; i < projects.length; i++){
			IProject curProject = projects[i];
			
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(curProject);
			
			//check whether the managed build info is converted
			boolean isCompatible = UpdateManagedProjectManager.isCompatibleProject(info);
			assertTrue(isCompatible);
			
			if(isCompatible){
				// Build the project in order to generate the makefiles 
				try{
					curProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,null);
				}
				catch(CoreException e){
					fail(e.getStatus().getMessage());
				}
				catch(OperationCanceledException e){
					fail("the project \"" + curProject.getName() + "\" build was cancelled, exception message: " + e.getMessage());
				}
				
				//compare the generated makefiles to their benchmarks
				if (files != null && files.length > 0) {
					if (i == 0) {
						String configName = info.getDefaultConfiguration().getName();
						IPath buildDir = Path.fromOSString(configName);
						succeeded = ManagedBuildTestHelper.compareBenchmarks(curProject, buildDir, files);
					}
				}
			}
		}
		
		if (succeeded) {	//  Otherwise leave the projects around for comparison
			for(int i = 0; i < projects.length; i++)
				ManagedBuildTestHelper.removeProject(projects[i].getName());
		}
	}

	private void createPathVariable(IPath tmpDir) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace = ResourcesPlugin.getWorkspace();
		IPathVariableManager pathMan = workspace.getPathVariableManager();
		String name = MBS_TEMP_DIR;
		try {
			if (pathMan.validateName(name).isOK() && pathMan.validateValue(tmpDir).isOK()) {
			    pathMan.setValue(name, tmpDir);
				assertTrue(pathMan.isDefined(name));
			} else {
				fail("could not create the path variable " + name);
			}
		} catch (Exception e) {fail("could not create the path variable " + name);}
	}
	
	private void createFileLink(IProject project, IPath tmpDir, String linkName, String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String name = MBS_TEMP_DIR;
		if (!pathVariableCreated) {
			createPathVariable(tmpDir);
			pathVariableCreated = true;
		}
			
		try {	
			// Now we can create a linked resource relative to the defined path variable: 
			IFile linkF1 = project.getFile(linkName);
			IPath location = new Path("MBSTemp/" + fileName);
			if (workspace.validateLinkLocation(linkF1, location).isOK()) {
			    linkF1.createLink(location, IResource.NONE, null);
			} else {
				fail("could not create the link to " + name);
			}
		} catch (Exception e) {fail("could not create the link to " + name);}		
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for a single file executable
	 */
	public void test30SingleFileExe(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("singleFileExe", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for a two file SO
	 */
	public void test30TwoFileSO(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("twoFileSO", null, null, true);
		buildProjects(projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration for multiple source files & a resource configuration
	 */
	public void test30MultiResConfig(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("source1/subdir.mk"),
				 Path.fromOSString("source2/subdir.mk"),
				 Path.fromOSString("source2/source21/subdir.mk")};
		IProject[] projects = createProjects("multiResConfig", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for linked files
	 */
	public void test30LinkedLib(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 //Path.fromOSString("subdir.mk")   // Can't compare this yet since it contains absolute paths!
				 Path.fromOSString("sources.mk")}; 
		IPath[] linkedFiles = {
				 Path.fromOSString("f1.c"), 
				 Path.fromOSString("f2.c"), 
				 Path.fromOSString("test_ar.h")};
		File srcDirFile = CTestPlugin.getFileInPlugin(new Path("resources/test30Projects/linkedLib/"));
		IPath srcDir = Path.fromOSString(srcDirFile.toString());
		IPath tmpSubDir = Path.fromOSString("CDTMBSTest");
		IPath tmpDir = ManagedBuildTestHelper.copyFilesToTempDir(srcDir, tmpSubDir, linkedFiles);
		try {
			IProject[] projects = createProjects("linkedLib", null, "cdt.managedbuild.target.gnu30.lib", true);
			//  There should be only one project.  Add our linked files to it.
			IProject project = projects[0];
			createFileLink(project, tmpDir, "f1.c", "f1.c");
			createFileLink(project, tmpDir, "f2link.c", "f2.c");
			createFileLink(project, tmpDir, "test_ar.h", "test_ar.h");
			//  Build the project
			buildProjects(projects, makefiles);
		} finally {ManagedBuildTestHelper.deleteTempDir(tmpSubDir, linkedFiles);}
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for a linked folder
	 */
	public void test30LinkedFolder(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 
		IPath[] linkedFiles = {
				 Path.fromOSString("f1.c"), 
				 Path.fromOSString("f2.c"), 
				 Path.fromOSString("test_ar.h"),
				 Path.fromOSString("Benchmarks/makefile"), 
				 Path.fromOSString("Benchmarks/objects.mk"), 
				 Path.fromOSString("Benchmarks/subdir.mk"),
				 Path.fromOSString("Benchmarks/sources.mk")}; 
		File srcDirFile = CTestPlugin.getFileInPlugin(new Path("resources/test30Projects/linkedFolder/"));
		IPath srcDir = Path.fromOSString(srcDirFile.toString());
		IPath tmpSubDir = Path.fromOSString("CDTMBSTest");
		IPath tmpDir = ManagedBuildTestHelper.copyFilesToTempDir(srcDir, tmpSubDir, linkedFiles);
		if (!pathVariableCreated) {
			createPathVariable(tmpDir);
			pathVariableCreated = true;
		}
		try {
			IPath location = Path.fromOSString(MBS_TEMP_DIR);
			IProject[] projects = createProjects("linkedFolder", location, "cdt.managedbuild.target.gnu30.lib", false);
			//  Build the project
			buildProjects(projects, makefiles);
		} finally {ManagedBuildTestHelper.deleteTempDir(tmpSubDir, linkedFiles);}
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration with pre and post process steps added to typical compile & link
	 */
	public void test30CopyandDeploy(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"), 
				 Path.fromOSString("Functions/subdir.mk")};
		IProject[] projects = createProjects("copyandDeploy", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration in the context of deleting a file, to see if the proper behavior
	 * occurs in the managedbuild system
	 */
	public void test30DeleteFile(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 		

		IProject[] projects = createProjects("deleteFile", null, null, true);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ArrayList resourceList = new ArrayList(1);
		IProject project = projects[0];
		IFile projfile = project.getFile("filetobedeleted.cxx");
		resourceList.add(projfile);
		IResource[] fileResource = (IResource[])resourceList.toArray(new IResource[resourceList.size()]);
		try {
		    workspace.delete(fileResource, false, null);
		}  catch (Exception e) {fail("could not delete file in project " + project.getName());}
		try {
			buildProjects(projects, makefiles);
		} finally {};
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration with pre and post process steps added to typical compile & link
	 */
	public void test30_1(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("test30_1", null, null, true);
		buildProjects(projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration with multiple input types use Eclipse content types
	 */
	public void test30_2(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("test30_2", null, null, true);
		buildProjects(projects, makefiles);
	}
}
