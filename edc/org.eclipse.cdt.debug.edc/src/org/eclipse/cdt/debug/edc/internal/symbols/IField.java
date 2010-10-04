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
public interface IField extends IType {

	public static final IField[] EMPTY_FIELD_ARRAY = new IField[0];

	public long getFieldOffset();

	public int getBitSize();

	public int getBitOffset();
	
	public int getAccessibility();

	// member offset may need to be computed
	public void setFieldOffset(long offset);

	/**
	 * Returns the composite type that owns the field.
	 */
	ICompositeType getCompositeTypeOwner();

}
