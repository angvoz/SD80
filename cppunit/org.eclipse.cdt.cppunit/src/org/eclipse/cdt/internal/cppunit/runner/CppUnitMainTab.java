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

import java.util.*;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.cdt.launch.ui.CMainTab;

public class CppUnitMainTab extends CMainTab
{

	public void createControl(Composite parent)
	{
		super.createControl(parent);
		fProgLabel.setText("CppUnit Test:");
	}
	/**
	 * Iterate through and suck up all of the executable files that
	 * we can find.
	 */
	protected IBinary[] getBinaryFiles(ICProject cproject) {
		IBinary [] executables=cproject.getBinaryContainer().getBinaries();
		Vector v=new Vector();
		for(int i=0;i<executables.length;i++)
		{
			if((executables[i].getPath().removeFileExtension().toString().endsWith("Test"))||
			   (executables[i].getPath().removeFileExtension().toString().endsWith("Tests")))
//			if(executables[i].getElementName().endsWith("Test"))
			{
				v.add(executables[i]);
			}
		}
		if(v.size()>0)
		{
			IBinary [] res=new IBinary[v.size()];
			Enumeration	e=v.elements();
			for(int i=0;i<v.size();i++)
			{
				res[i]=(IBinary)e.nextElement();
			}
			return res;
		}
		else
		{
			return null;
		}
	}

}
