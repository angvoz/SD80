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

import org.eclipse.cdt.debug.edc.internal.symbols.Variable;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.core.runtime.IPath;

public class DwarfVariable extends Variable {

	public DwarfVariable(String name, IScope scope, ILocationProvider locationProvider, 
			IType type, boolean isDeclared, IPath definingFile) {
		super(name, scope, null, locationProvider, isDeclared, definingFile);

		this.type = type;
	}
}
