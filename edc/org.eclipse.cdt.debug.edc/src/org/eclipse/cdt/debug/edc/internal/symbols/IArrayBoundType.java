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

public interface IArrayBoundType {

	/** bound of this array dimension. E.g., for "int a[7][8]", this would be
	 * either 7 or 8.
	 */
	public long getBoundCount();

	/**  number of array elements associated with each index of this array
	 *  dimension.
	 * 	E.g., for "int a[7][8]", "a[1]" comprises 8 elements, but "a[1][2]"
	 * comprises 1 element.
	 */
	public long getElementCount();

	/** array dimension ordinal. E.g., for "int a[7][8]", "[7]" is index 1 and
	 * "[8]" is index 0;
	 */
	public long getDimensionIndex();

	public void multiplyElementCount(long multiply);

	public void incDimensionIndex();

}
