/**********************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems Ltd - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 ***********************************************************************/

package org.eclipse.cdt.core.cdescriptor.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CDescriptorTests extends TestCase {

	static String projectId = CTestPlugin.PLUGIN_ID + ".TestProject";
	static IProject fProject;
	static CDescriptorListener listener = new CDescriptorListener();
	static CDescriptorEvent fLastEvent;

	/**
	 * Constructor for CDescriptorTest.
	 * 
	 * @param name
	 */
	public CDescriptorTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CDescriptorTests.class.getName());

		suite.addTest(new CDescriptorTests("testDescriptorCreation"));
		suite.addTest(new CDescriptorTests("testDescriptorOwner"));
		suite.addTest(new CDescriptorTests("testExtensionCreation"));
		suite.addTest(new CDescriptorTests("testExtensionGet"));
		suite.addTest(new CDescriptorTests("testExtensionData"));
		suite.addTest(new CDescriptorTests("testExtensionRemove"));
		suite.addTest(new CDescriptorTests("testProjectDataCreate"));
		suite.addTest(new CDescriptorTests("testProjectDataDelete"));
		suite.addTest(new CDescriptorTests("testAccumulatingBlankLinesInProjectData"));
		suite.addTest(new CDescriptorTests("testConcurrentDescriptorCreation"));
		suite.addTest(new CDescriptorTests("testConcurrentDescriptorCreation2"));
		suite.addTest(new CDescriptorTests("testDeadlockDuringProjectCreation"));
		
		TestSetup wrapper = new TestSetup(suite) {

			@Override
			protected void setUp() throws Exception {
				oneTimeSetUp();
			}

			@Override
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}

		};
		return wrapper;
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}

	static public class CDescriptorListener implements ICDescriptorListener {

		public void descriptorChanged(CDescriptorEvent event) {
			fLastEvent = event;
		}
	}

	static void oneTimeSetUp() throws Exception {
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = CTestPlugin.getWorkspace().getRoot();
				IProject project = root.getProject("testDescriptorProject");
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				CCorePlugin.getDefault().getCDescriptorManager().addDescriptorListener(listener);
				if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
					addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
				}
				fProject = project;
			}
		}, null);
	}

	static void oneTimeTearDown() throws Exception {
		fProject.delete(true, true, null);
	}

	public void testDescriptorCreation() throws Exception {
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				CCorePlugin.getDefault().mapCProjectOwner(fProject, projectId, false);
			}
		}, null);
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_ADDED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;

		Assert.assertEquals(fProject, desc.getProject());
		Assert.assertEquals("*", desc.getPlatform());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185930
	public void testConcurrentDescriptorCreation() throws Exception {
		fProject.close(null);
		fProject.open(null);
		Thread t= new Thread() {
			@Override
			public void run() {
				try {
					CCorePlugin.getDefault().getCProjectDescription(fProject, true);
				} catch (CoreException exc) {
				}
			}
		};
		t.start();
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		t.join();
		
		Element data = desc.getProjectData("testElement0");
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();
		fLastEvent = null;
 	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185930
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193503
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196118
	public void testConcurrentDescriptorCreation2() throws Exception {
		for (int i=0; i<20; ++i) {
			PDOMManager pdomMgr= (PDOMManager)CCorePlugin.getIndexManager();
			pdomMgr.shutdown();
			fProject.close(null);
			fProject.open(null);
			pdomMgr.startup().schedule();
			ICDescriptor desc= CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			NodeList childNodes= desc.getProjectData("testElement").getChildNodes();
			int lengthBefore= childNodes.getLength();
			final Throwable[] exception= new Throwable[10];
			Thread[] threads= new Thread[10];
			for (int j = 0; j < 10; j++) {
				final int index= j;
				Thread t= new Thread() {
					@Override
					public void run() {
						try {
							ICDescriptorOperation operation= new ICDescriptorOperation() {
								public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									try {
										Thread.sleep(10);
									} catch (InterruptedException exc) {
									}
									Element data = descriptor.getProjectData("testElement");
									data.appendChild(data.getOwnerDocument().createElement("test"));
									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									descriptor.saveProjectData();
								}};
								CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(fProject, operation, null);
						} catch (Throwable exc) {
							exception[index]= exc;
							exc.printStackTrace();
						}
					}
				};
				t.start();
				threads[j] = t;
				Thread.sleep(10);
			}
			for (int j = 0; j < threads.length; j++) {
				if (threads[j] != null) {
					threads[j].join();
				}
				assertNull(exception[j]);
			}
			desc= CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			childNodes= desc.getProjectData("testElement").getChildNodes();
			int lengthAfter= childNodes.getLength();
			assertEquals(threads.length, lengthAfter - lengthBefore);

			fLastEvent = null;
		}
	}

	public void testDeadlockDuringProjectCreation() throws Exception {
		for (int i=0; i < 10; ++i) {
			oneTimeTearDown();
			oneTimeSetUp();
			Thread t= new Thread() {
				@Override
				public void run() {
					try {
						ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
						Element data = desc.getProjectData("testElement0");
						data.appendChild(data.getOwnerDocument().createElement("test"));
						desc.saveProjectData();
					} catch (CoreException exc) {
					}
				}
			};
			t.start();

			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			Element data = desc.getProjectData("testElement0");
			data.appendChild(data.getOwnerDocument().createElement("test"));
			desc.saveProjectData();
			t.join();
			
			fLastEvent = null;
		}
 	}

	public void testDescriptorOwner() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICOwnerInfo owner = desc.getProjectOwner();
		Assert.assertEquals(projectId, owner.getID());
		Assert.assertEquals("*", owner.getPlatform());
		Assert.assertEquals("C/C++ Test Project", owner.getName());
	}

	public void testDescriptorConversion() {

	}

	public void testExtensionCreation() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef = desc.create("org.eclipse.cdt.testextension", "org.eclipse.cdt.testextensionID");

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), CDescriptorEvent.EXTENSION_CHANGED);
		fLastEvent = null;

		Assert.assertEquals("org.eclipse.cdt.testextension", extRef.getExtension());
		Assert.assertEquals("org.eclipse.cdt.testextensionID", extRef.getID());
	}

	public void testExtensionGet() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");

		Assert.assertEquals("org.eclipse.cdt.testextension", extRef[0].getExtension());
		Assert.assertEquals("org.eclipse.cdt.testextensionID", extRef[0].getID());
	}

	public void testExtensionData() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");
		extRef[0].setExtensionData("testKey", "testValue");

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;

		Assert.assertEquals("testValue", extRef[0].getExtensionData("testKey"));
		extRef[0].setExtensionData("testKey", null);
		Assert.assertEquals(null, extRef[0].getExtensionData("testKey"));
	}

	public void testExtensionRemove() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");
		desc.remove(extRef[0]);

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), CDescriptorEvent.EXTENSION_CHANGED);
		fLastEvent = null;

	}

	public void testProjectDataCreate() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		Element data = desc.getProjectData("testElement");
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	public void testProjectDataDelete() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		Element data = desc.getProjectData("testElement");
		NodeList list = data.getElementsByTagName("test");
		Assert.assertEquals(1, list.getLength());
		data.removeChild(data.getFirstChild());
		desc.saveProjectData();

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	public void testAccumulatingBlankLinesInProjectData() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		Element data = desc.getProjectData("testElement");
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();

		fProject.close(null);
		fProject.open(null);

		String dotCProject1 = readDotCProjectFile(fProject);
		long mtime1 = fProject.getFile(".cproject").getLocalTimeStamp();
		
		desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		data = desc.getProjectData("testElement");
		Node child = data.getFirstChild();
		while (child != null) {
			data.removeChild(child);
			child = data.getFirstChild();
		}
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();

		String dotCProject2 = readDotCProjectFile(fProject);
		long mtime2 = fProject.getFile(".cproject").getLocalTimeStamp();
		assertEquals("Difference in .cproject file", dotCProject1, dotCProject2);
		assertTrue(".cproject file has been written", mtime1 == mtime2);

		// do it a second time - just to be sure
		fProject.close(null);
		fProject.open(null);

		desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		data = desc.getProjectData("testElement");
		child = data.getFirstChild();
		while (child != null) {
			data.removeChild(child);
			child = data.getFirstChild();
		}
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();

		String dotCProject3 = readDotCProjectFile(fProject);
		long mtime3 = fProject.getFile(".cproject").getLocalTimeStamp();
		assertEquals("Difference in .cproject file", dotCProject2, dotCProject3);
		assertTrue(".cproject file has been written", mtime2 == mtime3);
	}

	/**
	 * Read .cproject file.
	 * 
	 * @param project
	 * @return content of .cproject file
	 * @throws CoreException 
	 * @throws IOException 
	 */
	private static String readDotCProjectFile(IProject project) throws CoreException, IOException {
		IFile cProjectFile = project.getFile(".cproject");
		InputStream in = cProjectFile.getContents();
		try {
			Reader reader = new InputStreamReader(in, "UTF-8");
			StringBuilder sb = new StringBuilder();
			char[] b = new char[4096];
			int n;
			while ((n = reader.read(b)) > 0) {
				sb.append(b, 0, n);
			}
			return sb.toString();
		} finally {
			in.close();
		}
	}

}