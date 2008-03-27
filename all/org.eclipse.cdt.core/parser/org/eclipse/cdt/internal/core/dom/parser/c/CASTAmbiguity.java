/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public abstract class CASTAmbiguity extends CASTNode  {

    protected static class CASTNameCollector extends CASTVisitor
    {
        private IASTName[] names = new IASTName[ 2 ];
    	private int namesPos=-1;

        {
            shouldVisitNames = true;
        }
        
        @Override
		public int visit(IASTName name) {
        	if (name != null) {
        		namesPos++;
        		names = (IASTName[]) ArrayUtil.append( IASTName.class, names, name );
        	}
            return PROCESS_CONTINUE;
        }
        
        public IASTName [] getNames()
        {
            names = (IASTName[]) ArrayUtil.removeNullsAfter( IASTName.class, names, namesPos );
        	return names;
        }
    }
    
    
    protected abstract IASTNode [] getNodes();
    
    @Override
	public boolean accept(ASTVisitor visitor) {
        IASTNode [] nodez = getNodes();
        int [] issues = new int[ nodez.length ];
        for( int i = 0; i < nodez.length; ++i )
        {
            IASTNode s = nodez[i];
            s.accept(visitor);
            CASTNameCollector resolver = new CASTNameCollector();
            s.accept( resolver );
            IASTName [] names  = resolver.getNames();
            for( int j = 0; j < names.length; ++j )
            {
                try
                {
                    IBinding b = names[j].resolveBinding();
                    if( b == null || b instanceof IProblemBinding )
                        ++issues[i];
                    IScope scope = CVisitor.getContainingScope( names[j] );
                    if( scope != null )
                    {
                        try {
                            ASTInternal.flushCache(scope);
                        } catch (DOMException e) {
                        }
                    }
                }
                catch( Exception t )
                {
                    ++issues[i];
                }
            }
        }
        int bestIndex = 0;
        int bestValue = issues[0];
        for( int i = 1; i < issues.length; ++i )
        {
            if( issues[i] < bestValue )
            {
                bestIndex = i;
                bestValue = issues[i];
            }
        }

        IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
        owner.replace(this, nodez[bestIndex]);
        return true;
    }

}
