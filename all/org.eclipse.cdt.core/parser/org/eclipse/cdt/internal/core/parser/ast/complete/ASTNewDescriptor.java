/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ASTNewDescriptor implements IASTNewExpressionDescriptor {
	
	List newPlacementExpressions;
	List newTypeIdExpressions;	
	List newInitializerExpressions;
	public ASTNewDescriptor(List newPlacementExpressions, List newTypeIdExpressions, List newInitializerExpressions) {
		super();
		this.newPlacementExpressions = newPlacementExpressions;
		this.newTypeIdExpressions = newTypeIdExpressions;
		this.newInitializerExpressions = newInitializerExpressions;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getExpressions()
	 */
	public Iterator getNewPlacementExpressions() {
		return newPlacementExpressions.iterator();
	}
	public Iterator getNewTypeIdExpressions() {
		return newTypeIdExpressions.iterator();
	}
	public Iterator getNewInitializerExpressions() {
		return newInitializerExpressions.iterator();
	}


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	accept( requestor, getNewPlacementExpressions() );
    	accept( requestor, getNewTypeIdExpressions() );
		accept( requestor, getNewInitializerExpressions() );
    }


    /**
     * @param requestor
     * @param iterator
     */
    protected void accept(ISourceElementRequestor requestor, Iterator iterator)
    {
        while( iterator.hasNext() )
        	((IASTExpression)iterator.next()).acceptElement(requestor);
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }

}
