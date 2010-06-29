/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.debug.edc.tests;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.core.runtime.IPath;
import org.junit.Assert;

/**
 * Base class for DWARF tests.
 */
public abstract class BaseDwarfTestCase extends Assert {

	protected static final String prefix = "resources/SymbolFiles/";
	
	protected static IPath getFile(String string) {
		return EDCTestPlugin.getDefault().getPluginFilePath(prefix + string);
	}

	/**
	 * Looser search of CU by filename. 
	 * @param reader
	 * @param file
	 * @return list (maybe empty) of compilation units with the given filename
	 */
	protected List<ICompileUnitScope> getCompileUnitsFor(IEDCSymbolReader reader, String file) {
		List<ICompileUnitScope> scopes = new ArrayList<ICompileUnitScope>();
		for (IScope scope : reader.getModuleScope().getChildren()) {
			if (scope instanceof ICompileUnitScope) {
				ICompileUnitScope cu = (ICompileUnitScope) scope;
				if (cu.getFilePath().lastSegment().equals(file))
					scopes.add(cu);
			}
		}
		return scopes;
	}

}
