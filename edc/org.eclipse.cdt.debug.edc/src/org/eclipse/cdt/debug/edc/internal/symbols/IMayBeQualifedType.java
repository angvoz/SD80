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

public interface IMayBeQualifedType extends IType {

	/**
	 * Whether this is a const type
	 * 
	 * @return true only if this is a const type
	 */
	public boolean isConst();

	/**
	 * Whether this is a volatile type
	 * 
	 * @return true only if this is a volatile type
	 */
	public boolean isVolatile();

}
