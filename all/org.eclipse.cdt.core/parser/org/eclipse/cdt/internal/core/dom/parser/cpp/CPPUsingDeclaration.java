/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Mar 16, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class CPPUsingDeclaration extends PlatformObject implements ICPPUsingDeclaration, ICPPInternalBinding{
    private IASTName name;
    private ICPPDelegate [] delegates;
    
    public CPPUsingDeclaration( IASTName name, IBinding [] bindings ) {
    	if( name instanceof ICPPASTQualifiedName ){
    		IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
    		name = ns[ ns.length - 1 ];
    	}
        this.name = name;
        this.delegates = createDelegates( bindings );
    }
    
    private ICPPDelegate [] createDelegates( IBinding [] bindings ){
        ICPPDelegate [] result = null;
        for( int i = 0; i < bindings.length; i++ ){
            if( bindings[i] instanceof ICPPDelegateCreator){
                ICPPDelegate delegate = ((ICPPDelegateCreator)bindings[i]).createDelegate( this );
                result = (ICPPDelegate[]) ArrayUtil.append( ICPPDelegate.class, result, delegate );
            } 
        }
        return (ICPPDelegate[]) ArrayUtil.trim( ICPPDelegate.class, result );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration#getDelegates()
     */
    public ICPPDelegate[] getDelegates() {
        return delegates;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() {
    	return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
    	return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
    	return name.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
    	return name.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CPPVisitor.getContainingScope( name.getParent() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        IASTNode n = name.getParent();
        if( n instanceof ICPPASTTemplateId )
            n = n.getParent();
            
        return n;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( ICPPUsingDeclaration usingDecl ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void addDefinition( IASTNode node ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void addDeclaration( IASTNode node ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#removeDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void removeDeclaration( IASTNode node ) {
    }

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
}
