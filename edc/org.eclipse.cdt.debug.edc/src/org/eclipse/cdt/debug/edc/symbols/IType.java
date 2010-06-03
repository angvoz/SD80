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
package org.eclipse.cdt.debug.edc.symbols;

import java.util.Map;


/**
 * Interface for types.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IType {

	/**
	 * Get type name
	 * 
	 * @return type name
	 */
	public String getName();

	/**
	 * Get type scope
	 * 
	 * @return scope
	 */
	public IScope getScope();

	/**
	 * Get size of data type in bytes
	 * 
	 * @return size in bytes of the effective type (e.g. skipping qualifiers, typedefs, etc.)
	 */
	public int getByteSize();

	/**
	 * Get properties
	 * 
	 * @return general map of type properties
	 */
	public Map<Object, Object> getProperties();

	/**
	 * Get type pointed to, accessed, qualified, etc. by this type
	 */
	public IType getType();

	/**
	 * Set type pointed to, accessed, qualified, etc. by this type
	 * 
	 * @param type
	 *           type pointed to, accessed, qualified, etc.
	 */
	public void setType(IType type);

	public void dispose();
}
