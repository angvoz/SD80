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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *
 */
public class ASTBaseSpecifier implements IASTBaseSpecifier {

	private final int offset;
    private final ASTAccessVisibility visibility; 
	private final boolean isVirtual; 
	private final String parentClassName;
	
	public ASTBaseSpecifier( String className, boolean v, ASTAccessVisibility a, int o )
	{
		parentClassName = className; 
		isVirtual = v; 
		visibility = a;
		offset = o;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#getAccess()
	 */
	public ASTAccessVisibility getAccess() {
		return visibility;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#isVirtual()
	 */
	public boolean isVirtual() {
		return isVirtual;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#getParent()
	 */
	public String getParentClassName() {
		return parentClassName;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#getParentClassSpecifier()
     */
    public IASTTypeSpecifier getParentClassSpecifier() throws ASTNotImplementedException
    {
        throw new ASTNotImplementedException();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#getNameOffset()
     */
    public int getNameOffset()
    {
        return offset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#addReference(org.eclipse.cdt.core.parser.ast.IASTReference)
     */
    public void addReference(IASTReference reference) throws ASTNotImplementedException
    {
		throw new ASTNotImplementedException();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#processReferences()
     */
    public void processReferences(ISourceElementRequestor requestor)throws ASTNotImplementedException
    {
		throw new ASTNotImplementedException();        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	// no references to process
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }

}
