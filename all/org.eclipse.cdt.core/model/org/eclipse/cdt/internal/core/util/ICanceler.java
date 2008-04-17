/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.util;

/**
 * An interface for objects accepting an instance of {@link ICancelable}.
 * 
 * @since 5.0
 */
public interface ICanceler {

	/**
	 * Set the cancelable object.
	 * 
	 * @param cancelable  the cancelable object
	 */
	void setCancelable(ICancelable cancelable);
	
}
