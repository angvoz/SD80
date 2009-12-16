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

import java.util.Map;

public interface IInheritance extends IType {

	/**
	 * Get offset to inherited fields
	 * 
	 * @return offset within inherited type to the fields inherited 
	 */
	public long getFieldsOffset();

	/**
	 * Get properties
	 * 
	 * @return general map of type properties
	 */
	public Map<Object, Object> getProperties();

	/**
	 * Get type inherited from
	 * 
	 * @return type
	 */
	public IType getType();

	/**
	 * Set type inherited from
	 * 
	 * @param type
	 *           type inherited from
	 */
	public void setType(IType type);

}
