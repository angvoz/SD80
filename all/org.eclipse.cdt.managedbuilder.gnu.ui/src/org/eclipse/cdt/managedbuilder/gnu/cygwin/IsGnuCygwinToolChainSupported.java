/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.runtime.PluginVersionIdentifier;

/*
 * This class inplements the IManagedIsToolChainSupported for the Gnu Cygwin tool-chain
 * The class is NOT used currently, because currently the gnu cygwin tool-chain
 * is intended to be used not only with Cygwin, but with MinGW also, and there is no 
 * correct way of determining whether the appropriate packages are installed for MinGW.
 * 
 * For the future MBS/CDT versions we might create the separate tool-chain/configuration/project-type
 * for the MinGW and define a set of converters using the tool-chain converter mechanism that MBS will provide,
 * that would convert the CygWin to the MinGW projects/tool-chains, and vice a versa.
 * 
 */
public class IsGnuCygwinToolChainSupported implements
		IManagedIsToolChainSupported {
	
	static final String[] CHECKED_NAMES = {"gcc", "binutils", "make"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	static boolean suppChecked = false;
	static boolean toolchainIsSupported = false; 

	/*
	 * returns support status
	 */
	public boolean isSupported(IToolChain toolChain,
			PluginVersionIdentifier version, String instance) {
		
		if (suppChecked) return toolchainIsSupported;

		String etcCygwin = CygwinPathResolver.getEtcPath();
		if (etcCygwin != null) {
			File file = new File(etcCygwin + "/setup/installed.db"); //$NON-NLS-1$
			try {
				BufferedReader data = new BufferedReader(new FileReader(file));

				// all required package names should be found 
				boolean[] found = new boolean[CHECKED_NAMES.length];
				String s;			
				while ((s = data.readLine()) != null ) {
					for (int j = 0; j < CHECKED_NAMES.length; j++) {
						if (s.startsWith(CHECKED_NAMES[j])) {found[j] = true;}
					}
				}	
				toolchainIsSupported = true;
				for (int j = 0; j < CHECKED_NAMES.length; j++) {
					toolchainIsSupported &= found[j]; 
				}
				data.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		
		suppChecked = true;

		return toolchainIsSupported;
	}	
}
