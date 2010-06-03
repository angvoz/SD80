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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IScope;

public class Enumeration extends MayBeQualifiedType implements IEnumeration {

	ArrayList<IEnumerator> enumerators = new ArrayList<IEnumerator>();
	HashMap<Long, IEnumerator> enumeratorsByConstant = new HashMap<Long, IEnumerator>();

	public Enumeration(String name, IScope scope, int byteSize, Map<Object, Object> properties) {
		super(name, scope, byteSize, properties);
		name = "enum"; //$NON-NLS-1$
	}
	
	public int enumeratorCount() {
		return enumerators.size();
	}

	public void addEnumerator(IEnumerator enumerator) {
		enumerators.add(enumerator);
		enumeratorsByConstant.put(enumerator.getValue(), enumerator);
	}

	public IEnumerator getEnumeratorByName(String name) {
		for (int i = 0; i < enumerators.size(); i++) {
			if (enumerators.get(i).getName().equals(name))
				return enumerators.get(i);
		}
		return null;
	}

	public IEnumerator getEnumeratorByValue(long value) {
		return this.enumeratorsByConstant.get(new Long(value));
	}

	public IEnumerator[] getEnumerators() {
		IEnumerator[] enumeratorArray = new IEnumerator[enumerators.size()];
		for (int i = 0; i < enumerators.size(); i++) {
			enumeratorArray[i] = enumerators.get(i);
		}
		return enumeratorArray;
	}

}
