/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author aniefer
 */
public class CPPArrayType implements IArrayType, ITypeContainer {
    private IType type = null;
    
    public CPPArrayType( IType type ){
        this.type = type;
    }
    
    public IType getType(){
        return type;
    }
    
    public boolean equals(Object obj) {
        if( obj instanceof IArrayType ){
            return ((IArrayType) obj).getType().equals( type );
        }
    	return false;
    }
}
