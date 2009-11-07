/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.Variable;

public class DwarfVariable extends Variable {

	private EDCDwarfReader reader;
	private long debugInfoTypeOffset;

	public DwarfVariable(String name, IScope scope, ILocationProvider locationProvider, EDCDwarfReader reader,
			long debugInfoTypeOffset) {
		super(name, scope, null, locationProvider);

		this.reader = reader;
		this.debugInfoTypeOffset = debugInfoTypeOffset;
	}

	@Override
	public IType getType() {
		if (type == null) {
			type = reader.typesByOffset.get(debugInfoTypeOffset);
		}

		return type;
	}

}
