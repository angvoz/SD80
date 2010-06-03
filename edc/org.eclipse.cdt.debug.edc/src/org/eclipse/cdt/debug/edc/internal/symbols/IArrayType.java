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

import org.eclipse.cdt.debug.edc.symbols.IType;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IArrayType extends IType, IAggregate {

	/**
	 * get the type that this is an array of
	 */
	public IType getType();

	public int getBoundsCount();

	public void addBound(IArrayBoundType bound);

	public IArrayBoundType[] getBounds();

	// get the Nth bound. E.g., for "a[X][Y]", getBound(0) returns info for
	// "[X]"
	public IArrayBoundType getBound(int index);

}
