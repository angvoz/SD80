/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * An Enumeration type.
 */
public interface IEnumeration extends IParent, IDeclaration {

	/**
	 * Return "enum"
	 * @throws CModelException 
	 * @deprecated
	 */
	String getTypeName() throws CModelException;
}
