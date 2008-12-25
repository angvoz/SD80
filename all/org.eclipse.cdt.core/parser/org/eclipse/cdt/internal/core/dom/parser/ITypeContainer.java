/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Internal interface representing types that contain other types
 * @author aniefer
 */
public interface ITypeContainer extends IType {
    /**
     * get the type this contains
     * @throws DOMException
     */
    IType getType() throws DOMException;

    /**
     * set the type this contains
     * @param type
     */
    void setType(IType type);
}
