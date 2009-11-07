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

import java.util.Collection;

/**
 * Interface representing a function scope. A function is a block of code
 * (function/method) which may have parameters and lexical blocks.
 */
public interface IFunctionScope extends IScope {

	/**
	 * Get function parameters
	 * 
	 * @return unmodifiable list of parameters which may be empty
	 */
	Collection<IVariable> getParameters();

	/**
	 * Get the location provider for the frame base.
	 * 
	 * @return the location provider, or null if none
	 */
	ILocationProvider getFrameBaseLocation();
}
