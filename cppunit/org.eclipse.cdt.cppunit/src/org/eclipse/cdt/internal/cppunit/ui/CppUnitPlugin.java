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

package org.eclipse.cdt.internal.cppunit.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractSet;
import java.util.HashSet;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.cppunit.runner.CppUnitLaunchConfiguration;
import org.eclipse.cdt.internal.cppunit.util.CppUnitLog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The plug-in runtime class for the CppUnit plug-in.
 */
public class CppUnitPlugin extends AbstractUIPlugin implements ILaunchListener {	
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static CppUnitPlugin fgPlugin= null;
	
	public static final String PLUGIN_ID = "org.eclipse.cdt.cppunit" ; //$NON-NLS-1$

	private static URL fgIconBaseURL;
	
	/**
	 * Use to track new launches. We need to do this
	 * so that we only attach a TestRunner once to a launch.
	 * Once a test runner is connected it is removed from the set.
	 */
	private AbstractSet fTrackedLaunches= new HashSet(20);
	
	public CppUnitPlugin() {
		super();
		fgPlugin= this;
	}
	public void start(BundleContext context) throws Exception {
		super.start(context);
		String pathSuffix= "icons/full/"; //$NON-NLS-1$
		try {
			URL uu=getBundle().getEntry("/");
			fgIconBaseURL= new URL(uu,pathSuffix);
		} catch (MalformedURLException e) {
			CppUnitLog.error("Error Starting CppUnit plugin",e);
			// do nothing
		}
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);
		CppUnitPreferencePage.initializeDefaults(getPreferenceStore());
	}
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
	}
	
	public static CppUnitPlugin getDefault() {
		return fgPlugin;
	}
	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow workBenchWindow= getActiveWorkbenchWindow();
		if (workBenchWindow == null) 
			return null;
		return workBenchWindow.getShell();
	}
	
	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (fgPlugin == null) 
			return null;
		IWorkbench workBench= fgPlugin.getWorkbench();
		if (workBench == null) 
			return null;
		return workBench.getActiveWorkbenchWindow();
	}	
	
	public IWorkbenchPage getActivePage() {
		return getActiveWorkbenchWindow().getActivePage();
	}

	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e));  //$NON-NLS-1$
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static URL makeIconFileURL(String name) throws MalformedURLException {
		if (CppUnitPlugin.fgIconBaseURL == null)
			throw new MalformedURLException();
		return new URL(CppUnitPlugin.fgIconBaseURL, name);
	}

	static ImageDescriptor getImageDescriptor(String relativePath) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(relativePath));
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/*
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		fTrackedLaunches.remove(launch);
	}

	/*
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		fTrackedLaunches.add(launch);
	}

	public void connectTestRunner(ILaunch launch,ICElement program, int port) {
		IWorkbench workbench= getWorkbench();
		if (workbench == null)
			return;
			
		IWorkbenchWindow window= getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page= window.getActivePage();
		TestRunnerViewPart testRunner= null;
		if (page != null) {
			try { // show the result view if it isn't shown yet
				testRunner= (TestRunnerViewPart)page.findView(TestRunnerViewPart.NAME);
				// TODO: have force the creation of view part contents 
				// otherwise the UI will not be updated
				if(testRunner == null || !testRunner.isCreated()) {
					IWorkbenchPart activePart= page.getActivePart();
					testRunner= (TestRunnerViewPart)page.showView(TestRunnerViewPart.NAME);
					//restore focus stolen by the creation of the result view
					page.activate(activePart);
				} 
			} catch (PartInitException pie) {
				log(pie);
			}
		}
		if (testRunner != null)
			testRunner.startTestRunListening( program, port, launch);	
	}

	/*
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(final ILaunch launch)
	{
		String portStr=null;
		ICElement programElement=null;
		int port=-1;
		if (!fTrackedLaunches.contains(launch)) {
//			System.out.println("Returning, cause not in the array");
			return;
		}
			
		ILaunchConfiguration config= launch.getLaunchConfiguration();

		if(config!=null)
		{
			// Test whether the launch defines the CppUnit Attribute
			portStr=launch.getAttribute(CppUnitLaunchConfiguration.PORT_ATTR);
			if (portStr != null)
			{
				port= Integer.parseInt(portStr);
			}
			try
			{
				ICProject project=CppUnitLaunchConfiguration.getCProject(config);
				if(project!=null)
				{
					String programName=CppUnitLaunchConfiguration.getProgramName(config);
					if(programName!=null)
					{
						IFile programFile = ((IProject)project.getResource()).getFile(programName);
						if (programFile != null && programFile.exists())
						{
							programElement=project.findElement(programFile.getLocation());
						}
					}
				}
			}
			catch(CoreException e){}
		}

		if((portStr!=null)&&(programElement!=null))
		{
			fTrackedLaunches.remove(launch);
			final int finalPort=port;
			final ICElement pg=programElement;
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					connectTestRunner(launch,pg,finalPort);
				}
			});		
		}
	}
	
	public static Display getDisplay() {
		Shell shell= getActiveWorkbenchShell();
		if (shell != null) {
			return shell.getDisplay();
		}
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;
	}
	public static String getPluginDirectory() {
		try {
			URL u=getDefault().getBundle().getEntry("/");
			String location=Platform.resolve(u).getPath();
			if(location.startsWith("/") && Platform.getOS().equals(Platform.OS_WIN32)) {
				location=location.substring(1);
			}
			return location;
		} catch(IOException e) {
			CppUnitLog.error("Error getting CppUnit plugin location",e);
			return "";
		}
	}

}
