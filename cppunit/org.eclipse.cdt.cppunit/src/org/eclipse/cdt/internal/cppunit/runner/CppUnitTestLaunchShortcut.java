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

import org.eclipse.cdt.launch.internal.CApplicationLaunchShortcut;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.cdt.internal.cppunit.runner.CppUnitLaunchConfiguration;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.IPath;
import java.util.*;
/*
 */
public class CppUnitTestLaunchShortcut extends CApplicationLaunchShortcut //mplements ILaunchShortcut
{
	/*
	 * Set to Protected instead of private this method in CApplicationLaunchShortcut
	 * Allows to redefine it
	 */
	protected ILaunchConfigurationType getCLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(CppUnitLaunchConfiguration.ID_LAUNCH_CPPUNIT_TEST);
	}
	/**
	 * Prompts the user to select a  binary
	 * 
	 * @return the selected binary or <code>null</code> if none.
	 */
	protected IBinary chooseBinary(List binList, String mode)
	{
		// Filter the List on the Test*.exe
		Vector newList=new Vector();
		for(int i=0;i<binList.size();i++)
		{
			IBinary b=(IBinary)binList.get(i);
// getNeededSharedLibs return an empty array for PEParsers...			
//			String [] libs=b.getNeededSharedLibs();
//			for(int j=0;j<libs.length;j++) 
//			{
//				System.out.println("Needed Shared Lib: "+libs[j]);
//			}
			
			IPath path=b.getPath().removeFileExtension();
			if((path.toString().endsWith("Test"))||(path.toString().endsWith("Tests")))
				newList.add(b);
		}
		return(super.chooseBinary(newList,mode));
	}
}
