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
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.core.runtime.IPath;

public abstract class CompileUnitScope extends Scope implements ICompileUnitScope {

	protected IPath filePath;

	protected Collection<ILineEntry> lineEntries;

	public CompileUnitScope(IPath filePath, IModuleScope parent, IAddress lowAddress, IAddress highAddress) {
		super(filePath != null ? filePath.lastSegment() : "", lowAddress, highAddress, parent); //$NON-NLS-1$

		this.filePath = filePath;
	}

	public IPath getFilePath() {
		return filePath;
	}

	public IFunctionScope getFunctionAtAddress(IAddress linkAddress) {
		IScope scope = getScopeAtAddress(linkAddress);
		while (scope != null && !(scope instanceof IFunctionScope)) {
			scope = scope.getParent();
		}

		return (IFunctionScope) scope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ICompileUnitScope#getFunctions()
	 */
	public Collection<IFunctionScope> getFunctions() {
		List<IFunctionScope> functions = new ArrayList<IFunctionScope>(children.size());
		for (IScope scope : getChildren()) {
			if (scope instanceof IFunctionScope)
				functions.add((IFunctionScope) scope);
		}
		return Collections.unmodifiableCollection(functions);
	}


	/**
	 * Parse the line table data - to be implemented by debug format specific
	 * sub classes.
	 * 
	 * @return the list of line table entries (may be empty)
	 */
	protected abstract Collection<ILineEntry> parseLineTable();


	public Collection<ILineEntry> getLineEntries() {
		if (lineEntries == null) {
			lineEntries = parseLineTable();
		}
		return Collections.unmodifiableCollection(lineEntries);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompileUnitScope ["); //$NON-NLS-1$
		builder.append("lowAddress="); //$NON-NLS-1$
		builder.append(lowAddress);
		builder.append(", highAddress="); //$NON-NLS-1$
		builder.append(highAddress);
		builder.append(", "); //$NON-NLS-1$
		if (filePath != null) {
			builder.append("path="); //$NON-NLS-1$
			builder.append(filePath.toOSString());
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}
