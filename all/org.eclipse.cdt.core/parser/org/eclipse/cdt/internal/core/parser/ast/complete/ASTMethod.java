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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableError;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.TypeFilter;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author jcamelon
 *
 */
public class ASTMethod extends ASTFunction implements IASTMethod
{
    private final List constructorChain;
    private final boolean isConstructor;
    private final boolean isPureVirtual;
    private final ASTAccessVisibility visibility;
    private final boolean isDestructor;
    /**
     * @param symbol
     * @param parameters
     * @param returnType
     * @param exception
     * @param startOffset
     * @param nameOffset
     * @param ownerTemplate
     * @param references
     */
    public ASTMethod(IParameterizedSymbol symbol, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, int startOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, IASTTemplate ownerTemplate, 
	List references, boolean previouslyDeclared, boolean isConstructor, boolean isDestructor, boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean hasFunctionTryBlock, boolean isFriend )
    {
        super(
            symbol,
            nameEndOffset,
            parameters,
            returnType,
            exception,
            startOffset,
            startLine,
            nameOffset,
            nameLine, ownerTemplate, references, previouslyDeclared, hasFunctionTryBlock, isFriend );
        this.visibility = visibility; 
        this.isConstructor = isConstructor;
        this.isDestructor = isDestructor;
        this.isPureVirtual = isPureVirtual; 
        this.constructorChain = constructorChain;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVirtual()
     */
    public boolean isVirtual()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isVirtual );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isExplicit()
     */
    public boolean isExplicit()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isExplicit);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConstructor()
     */
    public boolean isConstructor()
    {
        return isConstructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isDestructor()
     */
    public boolean isDestructor()
    {
        return isDestructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConst()
     */
    public boolean isConst()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isConst);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVolatile()
     */
    public boolean isVolatile()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isVolatile );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isPureVirtual()
     */
    public boolean isPureVirtual()
    {
        return isPureVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getVisiblity()
     */
    public ASTAccessVisibility getVisiblity()
    {
        return visibility;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        try
        {
        	if( isFriend() )
        		requestor.acceptFriendDeclaration( this );
        	else
        		requestor.acceptMethodDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        methodCallbacks(requestor, manager);
    }
    protected void methodCallbacks(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        functionCallbacks(requestor, manager);
        processConstructorChain(requestor, manager);
    }
    
    protected void processConstructorChain(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        if( constructorChain != null )
        {
        	Iterator i = getConstructorChainInitializers(); 
        	while( i.hasNext() )
        	{
        		IASTConstructorMemberInitializer c = (IASTConstructorMemberInitializer)i.next();
        		c.acceptElement(requestor, manager);
        	}
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
		try
        {
            requestor.enterMethodBody(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
		methodCallbacks( requestor, manager );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        try
        {
            requestor.exitMethodBody( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#getConstructorChainInitializers()
     */
    public Iterator getConstructorChainInitializers()
    {
		if( constructorChain == null )
			return EmptyIterator.EMPTY_ITERATOR; 
        return constructorChain.iterator();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTMethod#getOwnerClassSpecifier()
	 */
	public IASTClassSpecifier getOwnerClassSpecifier() {
		if( getOwnerScope() instanceof IASTTemplateDeclaration )
			return (IASTClassSpecifier) ((IASTTemplateDeclaration)getOwnerScope()).getOwnerScope();
		
		return (IASTClassSpecifier) getOwnerScope();
	}
	
	/**
	 * @param prefix
	 * @param thisContainer
	 * @param qualification
	 * @param filter
	 * @param lookInThis
	 * @param lookupResults
	 * @return
	 * @throws LookupError
	 */
	protected List performPrefixLookup(String prefix, IContainerSymbol thisContainer, IContainerSymbol qualification, TypeFilter filter, List paramList) throws LookupError {
		if( filter.isLookingInThis() ){
			try{
				ISymbol thisPointer = thisContainer.lookup( ParserSymbolTable.THIS );
				ISymbol thisClass = ( thisPointer != null ) ? thisPointer.getTypeSymbol() : null; 
				if( thisClass != null && thisClass instanceof IContainerSymbol ){
					return ((IContainerSymbol) thisClass).prefixLookup( filter, prefix, true, paramList );
				}	
			} catch (ParserSymbolTableException e) {
				throw new LookupError();
			} catch (ParserSymbolTableError e ){
				throw new LookupError();
			}
		} else {
			return super.performPrefixLookup( prefix, thisContainer, qualification, filter, paramList );
		}
		
		return null;
	}
}
