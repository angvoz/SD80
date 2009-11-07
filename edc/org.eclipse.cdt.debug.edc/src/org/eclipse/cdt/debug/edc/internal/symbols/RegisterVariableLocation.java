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

public class RegisterVariableLocation implements IRegisterVariableLocation {

	protected String name;
	protected int id;

	public RegisterVariableLocation(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getRegisterName() {
		return name;
	}

	public int getRegisterID() {
		return id;
	}

}
