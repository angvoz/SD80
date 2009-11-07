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
package org.eclipse.cdt.debug.edc.internal.symbols;

public class InvalidVariableLocation implements IInvalidVariableLocation {

	private String message;

	public InvalidVariableLocation(String message) {
		if (message == null)
			this.message = ""; //$NON-NLS-1$
		else
			this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		if (message == null)
			this.message = ""; //$NON-NLS-1$
		else
			this.message = message;
	}
}
