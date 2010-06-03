/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.FunctionScope;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.core.runtime.IPath;

public class DwarfFunctionScope extends FunctionScope implements IFunctionScope {
	protected int declFileNum;
	
	public DwarfFunctionScope(String name, IScope parent, IAddress lowAddress, IAddress highAddress,
			ILocationProvider frameBaseLocationProvider) {
		super(name, parent, lowAddress, highAddress, frameBaseLocationProvider);
	}
	
	/**
	 * Set the declaration file number entry from the line number table
	 * @param declFileNum
	 */
	public void setDeclFileNum(int declFileNum) {
		this.declFileNum = declFileNum;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.FunctionScope#getDeclFile()
	 */
	@Override
	public IPath getDeclFile() {
		IPath file = super.getDeclFile();
		if (file == null && declFileNum != 0) {
			// ask the parent
			IScope cu = getParent();
			while (cu != null) {
				if (cu instanceof DwarfCompileUnit) {
					return ((DwarfCompileUnit) cu).getFileEntry(declFileNum);
				}
				cu = cu.getParent();
			}
		}
		return file;
	}
}
