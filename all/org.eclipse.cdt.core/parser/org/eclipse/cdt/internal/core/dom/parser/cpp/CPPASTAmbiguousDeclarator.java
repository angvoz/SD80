/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.Assert;

/**
 * Handles ambiguities when parsing declarators.
 * <br>
 * Example: void f(int (D));  // is D a type?
 */
public class CPPASTAmbiguousDeclarator extends ASTAmbiguousNode implements IASTAmbiguousDeclarator {

    private IASTDeclarator[] dtors = new IASTDeclarator[2];
    private int dtorPos=-1;
	private IASTInitializer fInitializer;

    
    public CPPASTAmbiguousDeclarator(IASTDeclarator... decls) {
		for(IASTDeclarator d : decls) {
			if (d != null) {
				addDeclarator(d);
			}
		}
	}

	@Override
	protected void beforeResolution() {
		// populate containing scope, so that it will not be affected by the alternative branches.
		IScope scope= CPPVisitor.getContainingNonTemplateScope(this);
		if (scope instanceof ICPPASTInternalScope) {
			((ICPPASTInternalScope) scope).populateCache();
		}
	}
	
    @Override
	protected void afterResolution(ASTVisitor resolver, IASTNode best) {
    	// if we have an initializer it needs to be added to the chosen alternative.
    	// we also need to resolve ambiguities in the initializer.
    	if (fInitializer != null) {
    		((IASTDeclarator) best).setInitializer(fInitializer);
    		fInitializer.accept(resolver);
    	}
	}

	public IASTDeclarator copy() {
		throw new UnsupportedOperationException();
	}
    
	public void addDeclarator(IASTDeclarator d) {
        assertNotFrozen();
    	if (d != null) {
    		dtors = (IASTDeclarator[]) ArrayUtil.append(IASTDeclarator.class, dtors, ++dtorPos, d);
    		d.setParent(this);
			d.setPropertyInParent(SUBDECLARATOR);
    	}
    }

    public IASTDeclarator[] getDeclarators() {
    	dtors = (IASTDeclarator[]) ArrayUtil.removeNullsAfter(IASTDeclarator.class, dtors, dtorPos ); 
        return dtors;
    }

    @Override
	public IASTNode[] getNodes() {
        return getDeclarators();
    }

	public IASTInitializer getInitializer() {
		return fInitializer;
	}

	public IASTName getName() {
		return dtors[0].getName();
	}

	public IASTDeclarator getNestedDeclarator() {
		return dtors[0].getNestedDeclarator();
	}

	public IASTPointerOperator[] getPointerOperators() {
		return dtors[0].getPointerOperators();
	}
	
	public int getRoleForName(IASTName name) {
		return dtors[0].getRoleForName(name);
	}

	public void addPointerOperator(IASTPointerOperator operator) {
        assertNotFrozen();
		Assert.isLegal(false);
	}

	public void setInitializer(IASTInitializer initializer) {
		// store the initializer until the ambiguity is resolved
		fInitializer= initializer;
	}

	public void setName(IASTName name) {
        assertNotFrozen();
		Assert.isLegal(false);
	}

	public void setNestedDeclarator(IASTDeclarator nested) {
        assertNotFrozen();
		Assert.isLegal(false);
	}
}
