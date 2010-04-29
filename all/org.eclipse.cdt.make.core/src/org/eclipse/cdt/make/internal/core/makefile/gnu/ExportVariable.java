/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.internal.core.makefile.Directive;


public class ExportVariable extends VariableDefinition {

	public ExportVariable(Directive parent, String name, StringBuffer value, int type) {
		super(parent, name, value, type);
	}

	@Override
	public boolean isExport() {
		return true;
	}
}
