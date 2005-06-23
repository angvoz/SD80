/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Doug Schaefer
 */
public interface IType extends Cloneable {
	public static final IType [] EMPTY_TYPE_ARRAY = new IType[0];
    public Object clone();

    /**
     * is the given type the same as this type?
     * @param type
     * @return
     */
    public boolean isSameType( IType type );
}
