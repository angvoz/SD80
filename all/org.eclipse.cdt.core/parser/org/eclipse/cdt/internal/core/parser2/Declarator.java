/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

public class Declarator implements IParameterCollection, IDeclaratorOwner, IDeclarator
{
	private static final int DEFAULT_ARRAYLIST_SIZE = 4;
	private static final char[] EMPTY_STRING = new char[0];  //$NON-NLS-1$
	
	private final IDeclaratorOwner owner;
    private ITokenDuple pointerOperatorNameDuple = null;
    private ITokenDuple namedDuple = null;
    private Object constructorExpression = null;
    private Declarator ownedDeclarator = null;
	private Object initializerClause = null;
	private Object exceptionSpecification = null;
	private Object bitFieldExpression = null;

	private int flag = 0;
	protected void setBit(boolean b, int mask){
		if( b ){
			flag = flag | mask; 
		} else {
			flag = flag & ~mask; 
		} 
	}
	
	protected boolean checkBit(int mask){
		return (flag & mask) != 0;
	}	
	
	protected static final int IS_FUNCTION       =  0x000020;
	protected static final int HAS_TRY_BLOCK 	 =  0x000040;
	protected static final int HAS_FUNCTION_BODY =  0x000080;
	protected static final int IS_PURE_VIRTUAL   =  0x000100;
	protected static final int IS_VAR_ARGS       =  0x000200;
	protected static final int IS_VOLATILE       =  0x000400;
	protected static final int IS_CONST          =  0x000800;
	
	private List ptrOps = Collections.EMPTY_LIST;
	private List parameters = Collections.EMPTY_LIST;
	private List arrayModifiers = Collections.EMPTY_LIST;
	private List constructorMemberInitializers = Collections.EMPTY_LIST;
	


    public Declarator( IDeclaratorOwner owner )
	{
		this.owner = owner; 
	}
	
    /**
     * @return
     */
    public char[] getName()
    {
    	if( namedDuple == null ) return EMPTY_STRING;
        return namedDuple.toCharArray();
    }

    /**
     * @return
     */
    public int getNameEndOffset()
    {
    	if( namedDuple == null ) return -1;
        return namedDuple.getEndOffset();
    }

    public int getNameLine()
	{
    	if( namedDuple == null ) return -1;
    	return namedDuple.getLineNumber();
    }
    
    /**
     * @return
     */
    public int getNameStartOffset()
    {
    	if( namedDuple == null ) return -1;
        return namedDuple.getStartOffset();
    }

    /**
     * @return
     */
    public IDeclaratorOwner getOwner()
    {
        return owner;
    }

 
    /**
     * @return
     */
    public List getPointerOperators()
    {
        return ptrOps;
    }

	public void addPointerOperator( Object ptrOp )
	{
    	if( ptrOps == Collections.EMPTY_LIST )
    		ptrOps = new ArrayList( DEFAULT_ARRAYLIST_SIZE );

		ptrOps.add( ptrOp ); 
	}
    /**
     * @return
     */
    public List getParameters()
    {
        return parameters;
    }

	public void addParameter( DeclarationWrapper param )
	{
    	if( parameters == Collections.EMPTY_LIST )
    		parameters = new ArrayList( DEFAULT_ARRAYLIST_SIZE );

		parameters.add( param );
	}
    /**
     * @return
     */
    public Object getInitializerClause()
    {
        return initializerClause;
    }

    /**
     * @param clause
     */
    public void setInitializerClause(Object clause)
    {
        initializerClause = clause;
    }

    /**
     * @return
     */
    public Declarator getOwnedDeclarator()
    {
        return ownedDeclarator;
    }

    /**
     * @param declarator
     */
    public void setOwnedDeclarator(Declarator declarator)
    {
        ownedDeclarator = declarator;
    }
    
    public void setName( ITokenDuple duple )
    {
		namedDuple = duple;
    }

    /**
     * @return
     */
    public Object getExceptionSpecification()
    {
        return exceptionSpecification;
    }

    /**
     * @return
     */
    public boolean isConst()
    {
        return checkBit(IS_CONST);
    }

    /**
     * @return
     */
    public boolean isVolatile()
    {
        return checkBit( IS_VOLATILE);
    }

    /**
     * @param specification
     */
    public void setExceptionSpecification(Object specification)
    {
        exceptionSpecification = specification;
    }

    /**
     * @param b
     */
    public void setConst(boolean b)
    {
        setBit(b, IS_CONST );
    }

    /**
     * @param b
     */
    public void setVolatile(boolean b)
    {
    	setBit( b, IS_VOLATILE );
    }

    /**
     * @param b
     */
    public void setPureVirtual(boolean b)
    {
    	setBit( b, IS_PURE_VIRTUAL );
    }

    /**
     * @return
     */
    public boolean isPureVirtual()
    {
        return checkBit( IS_PURE_VIRTUAL );
    }

    /**
     * @param arrayMod
     */
    public void addArrayModifier(Object arrayMod)
    {
    	if( arrayModifiers == Collections.EMPTY_LIST )
    		arrayModifiers = new ArrayList( DEFAULT_ARRAYLIST_SIZE );
		arrayModifiers.add( arrayMod );        
    }

    /**
     * @return
     */
    public List getArrayModifiers()
    {
        return arrayModifiers;
    }

    /**
     * @return
     */
    public Object getBitFieldExpression()
    {
        return bitFieldExpression;
    }

    /**
     * @param exp
     */
    public void setBitFieldExpression(Object exp)
    {
        bitFieldExpression = exp;
    }

    /**
     * @param astExpression
     */
    public void setConstructorExpression(Object astExpression)
    {
        constructorExpression = astExpression;
    }

    /**
     * @return
     */
    public Object getConstructorExpression()
    {
        return constructorExpression;
    }

    /**
     * @param initializer
     */
    public void addConstructorMemberInitializer(Object initializer)
    {
    	if( constructorMemberInitializers == Collections.EMPTY_LIST )
    		constructorMemberInitializers = new ArrayList( DEFAULT_ARRAYLIST_SIZE );
        constructorMemberInitializers.add( initializer );
    }

    /**
     * @return
     */
    public List getConstructorMemberInitializers()
    {
        return constructorMemberInitializers;
    }


    /**
     * @return
     */
    public boolean isFunction()
    {
        return checkBit( IS_FUNCTION );
    }

    /**
     * @param b
     */
    public void setIsFunction(boolean b)
    {
        setBit( b, IS_FUNCTION );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclaratorOwner#getDeclarators()
     */
    public Iterator getDeclarators()
    {
		if( ownedDeclarator == null )
			return EmptyIterator.EMPTY_ITERATOR;
		
		List l = new ArrayList(1);
		l.add( ownedDeclarator );
		return l.iterator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclaratorOwner#getDeclarationWrapper()
     */
    public DeclarationWrapper getDeclarationWrapper()
    {
    	Declarator d = this;
    	while( d.getOwner() instanceof Declarator )
    		d = (Declarator)d.getOwner();
    	return (DeclarationWrapper)d.getOwner(); 
    }

	
    /**
     * @return
     */
    public ITokenDuple getNameDuple()
    {
        return namedDuple;
    }

    /**
     * @param nameDuple
     */
    public void setPointerOperatorName(ITokenDuple nameDuple)
    {
        pointerOperatorNameDuple = nameDuple; 
    }

    /**
     * @return
     */
    public ITokenDuple getPointerOperatorNameDuple()
    {
        return pointerOperatorNameDuple;
    }

    /**
     * @return
     */
    public boolean hasFunctionBody()
    {
        return checkBit( HAS_FUNCTION_BODY );
    }

    /**
     * @param b
     */
    public void setHasFunctionBody(boolean b)
    {
    	setBit( b, HAS_FUNCTION_BODY );
    }

    /**
     * @param b
     */
    public void setFunctionTryBlock(boolean b)
    {
        setBit( b, HAS_TRY_BLOCK );
    }

    /**
     * @return
     */
    public boolean hasFunctionTryBlock()
    {
        return checkBit( HAS_TRY_BLOCK );
    }

	/**
	 * @param b
	 */
	public void setIsVarArgs(boolean b) {
		setBit( b, IS_VAR_ARGS );
	}

	/**
	 * @return Returns the varArgs.
	 */
	public boolean isVarArgs() {
		return checkBit( IS_VAR_ARGS );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IDeclarator#getScope()
	 */
	public Object getScope() {
		return getDeclarationWrapper().getScope();
	}

}
