/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTInitializerList extends CASTNode implements
        IASTInitializerList {

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclarators()
     */
    public IASTInitializer[] getInitializers() {
        if( initializers == null ) return IASTInitializer.EMPTY_INITIALIZER_ARRAY;
        return (IASTInitializer[]) ArrayUtil.removeNulls( IASTInitializer.class, initializers );
    }
    
    public void addInitializer( IASTInitializer d )
    {
        initializers = (IASTInitializer[]) ArrayUtil.append( IASTInitializer.class, initializers, d );
    }
    
    private IASTInitializer [] initializers = null;

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitInitializers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTInitializer [] list = getInitializers();
        for ( int i = 0; i < list.length; i++ ) {
            if( !list[i].accept( action ) ) return false;
        }
        return true;
    }

}
