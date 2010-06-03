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

import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IType;

public interface IEnumeration extends IType {

	public int enumeratorCount();

	public void addEnumerator(IEnumerator enumerator);

	public IEnumerator getEnumeratorByName(String name);

	public IEnumerator getEnumeratorByValue(long value);

	/**
	 * returns an array of the IEnumerators declared in this enumeration
	 */
	IEnumerator[] getEnumerators();

}
