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
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *
 */
public class ASTAbstractTypeSpecifierDeclaration
    extends ASTDeclaration
    implements IASTAbstractTypeSpecifierDeclaration
{
	private final IASTTemplate ownerTemplate;
    private final IASTTypeSpecifier typeSpecifier;
    private final boolean isFriendDeclaration;
    /**
     * @param scope
     * @param typeSpecifier
     */
    public ASTAbstractTypeSpecifierDeclaration(IASTScope scope, IASTTypeSpecifier typeSpecifier, IASTTemplate ownerTemplate, int startingOffset, int endingOffset, int startingLine, int endingLine, boolean isFriend)
    {
        super( ownerTemplate != null ? null : scope  ); 
        this.typeSpecifier = typeSpecifier;
        this.ownerTemplate = ownerTemplate;
        this.isFriendDeclaration = isFriend;
        if( ownerTemplate != null )
        	ownerTemplate.setOwnedDeclaration( this );
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getTypeSpecifier()
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return typeSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplatedDeclaration#getOwnerTemplateDeclaration()
     */
    public IASTTemplate getOwnerTemplateDeclaration()
    {
        return ownerTemplate;
    }
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	try
        {
    		if( isFriendDeclaration() )
    			requestor.acceptFriendDeclaration( this );
    		else
    			requestor.acceptAbstractTypeSpecDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration#isFriendDeclaration()
	 */
	public boolean isFriendDeclaration() {
		return isFriendDeclaration;
	}
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public final int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public final int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public final void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public final void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public final int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public final int getEndingOffset()
    {
        return endingOffset;
    }
    
}
