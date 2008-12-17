/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;

/**
 * Internal interface for c- or c++ enumeration specifiers.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTInternalEnumerationSpecifier extends IASTEnumerationSpecifier {
	/**
	 * Notifies that the value computation for the enumeration is started. Returns whether this is the
	 * first attempt to do so.
	 */
	boolean startValueComputation();
	
	/**
	 * @since 5.1
	 */
	public IASTInternalEnumerationSpecifier copy();
}
