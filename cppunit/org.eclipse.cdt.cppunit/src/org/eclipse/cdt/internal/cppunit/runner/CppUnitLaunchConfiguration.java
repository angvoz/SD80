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

package org.eclipse.cdt.internal.cppunit.runner;

import java.util.Vector;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;
import org.eclipse.cdt.internal.cppunit.util.SocketUtil;
import org.eclipse.cdt.launch.internal.LocalRunLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class CppUnitLaunchConfiguration extends LocalRunLaunchDelegate
{
	public static final String PORT_ATTR= CppUnitPlugin.PLUGIN_ID+".PORT"; //$NON-NLS-1$
	public static final String ID_LAUNCH_CPPUNIT_TEST = "org.eclipse.cdt.cppunit.launchConfig"; //"org.eclipse.cdt.launch.localCLaunch";

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
	{
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// Setting the port as argument
		int port= SocketUtil.findUnusedLocalPort("", 5000, 15000);   //$NON-NLS-1$
		launch.setAttribute(PORT_ATTR, Integer.toString(port));
		
//		Properties iEnvironment=prepareEnvironment(config);

		String arguments[]=getProgramArgumentsArray(config);
		Vector v=new Vector();
		for(int i=0;i<arguments.length;i++)
		{
			if(arguments[i].startsWith("-port="))
			{
				// Skip It
			}
			else
			{
				v.add(arguments[i]);
			}
		}
		String newArgument="";
		for(int i=0;i<v.size();i++)
		{
			newArgument+=(String)v.elementAt(i);
		}

		monitor.beginTask("Launching Local CppUnit Test", IProgressMonitor.UNKNOWN);
		ILaunchConfigurationWorkingCopy wcopy=config.getWorkingCopy();
		wcopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "-port="+port+" "+newArgument);
//		wcopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, iEnvironment);
		wcopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_INHERIT, true);
		wcopy.doSave();
		super.launch(config,mode,launch,monitor);
	}
	
//	Properties prepareEnvironment(ILaunchConfiguration config)
//	{
//		Properties iEnvironment=getEnvironmentProperty(config);
//		String libPathVariable=EnvironmentLibPath.getLibPathVariable();
//		if(libPathVariable==null) return iEnvironment;
//		Properties defaultEnv=getDefaultEnvironment();
//		LocatePluginUtil x=new LocatePluginUtil(CppUnitPlugin.getDefault());
////		String libFileName=x.findLibrary("cppunit");
//		String libFileName=null;
//		if(libFileName==null) return iEnvironment;
//		String newValue=(new Path(libFileName)).removeLastSegments(1).toOSString();
//		if(newValue==null) return iEnvironment;
//
//		String value=null;
//		// Retrieve the value from the default environment
//		value=defaultEnv.getProperty(libPathVariable);
//		if(value!=null)
//		{
//			// If value already contains newValue, then return
//			int index=value.indexOf(newValue);
//			if(index!=-1)
//			{
//				// Do nothing
//				return iEnvironment;
//			}
//		}
//		// Retrieve the value from the launch configuration
//		value=iEnvironment.getProperty(libPathVariable);
//		if(value!=null)
//		{
//			// If value already contains newValue, then return
//			int index=value.indexOf(newValue);
//			if(index!=-1)
//			{
//				// Do nothing
//				return iEnvironment;
//			}
//		}
//		// Set libPathName to newValue
//		newValue+=System.getProperty("path.separator")+"${"+libPathVariable+"}";
////		System.out.println("Setting "+libPathVariable+" to "+newValue);
//		iEnvironment.setProperty(libPathVariable,newValue);
//		return iEnvironment;
//	}			
}
