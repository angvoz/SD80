/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Stack;

import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTScopedElement;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;

/**
 * @author jcamelon
 *
 */
public class ASTQualifiedNamedElement implements IASTQualifiedNameElement
{
    /**
     * @param scope
     */
    public ASTQualifiedNamedElement(IASTScope scope, char[] name )
    {
        Stack names = new Stack();
		IASTScope parent = scope;
        
		names.push( name ); // push on our own name
		while (parent != null)
		{
			if (parent instanceof IASTNamespaceDefinition
				|| parent instanceof IASTClassSpecifier )
			{
				names.push(((IASTOffsetableNamedElement)parent).getNameCharArray());
				if( parent instanceof IASTScopedElement  )
					parent = ((IASTScopedElement)parent).getOwnerScope();				
			}
			else if( parent instanceof IASTTemplateDeclaration )
			{
				if( parent instanceof IASTScopedElement  )
					parent = ((IASTScopedElement)parent).getOwnerScope();
				continue;
			}
			else 
				break;
		}
		if (names.size() != 0)
		{
			qualifiedNames = new char[names.size()][];
			int counter = 0;
			while (!names.empty())
				qualifiedNames[counter++] = (char[])names.pop();
		}
		else 
			qualifiedNames = null;

    }
    
	public String[] getFullyQualifiedName()
	{
	    String[] result = new String[qualifiedNames.length ];
	    for( int i = 0; i < qualifiedNames.length; i++ ){
	        result[i] = String.valueOf(qualifiedNames[i]);
	    }
		return result;
	}

    private final char[][] qualifiedNames;

}
