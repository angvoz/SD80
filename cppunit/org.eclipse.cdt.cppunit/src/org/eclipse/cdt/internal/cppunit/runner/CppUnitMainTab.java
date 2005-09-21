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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.swt.widgets.Composite;

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
		try {
			IBinary [] executables=cproject.getBinaryContainer().getBinaries();
			Vector v=new Vector();
			for(int i=0;i<executables.length;i++)
			{
//TODO Act Here !!
				if((executables[i].getPath().removeFileExtension().toString().endsWith("Test"))||
						(executables[i].getPath().removeFileExtension().toString().endsWith("Tests"))) {
//				if(executables[i].getElementName().endsWith("Test")) {
					v.add(executables[i]);
				}
			}
			return(IBinary[])v.toArray(new IBinary[0]);
		} catch(CModelException e) {
			return new IBinary[0];
		}
	}
}
