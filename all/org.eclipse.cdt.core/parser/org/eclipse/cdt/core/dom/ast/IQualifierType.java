/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 8, 2004
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IQualifierType extends IType {
    /**
     * is this a const type
     * @return
     * @throws DOMException
     */
    public boolean isConst() throws DOMException;
    
    /** 
     * is this a volatile type
     * @return
     * @throws DOMException
     */
    public boolean isVolatile() throws DOMException;
    
    /** 
     * get the type that this is qualifying
     * @return
     * @throws DOMException
     */
    public IType getType() throws DOMException;
}
