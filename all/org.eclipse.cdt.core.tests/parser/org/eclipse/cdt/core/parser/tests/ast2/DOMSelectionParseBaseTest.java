/**********************************************************************
 * Copyright (c) 2002-2005 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.core.resources.IFile;

/**
 * @author johnc
 *
 */
public class DOMSelectionParseBaseTest extends DOMFileBasePluginTest {

	public DOMSelectionParseBaseTest(String name, Class className) {
		super(name, className);
	}

	protected IASTNode parse(String code, int offset1, int offset2) throws Exception {
		return parse( code, offset1, offset2, true );
	}

	/**
	 * @param code
	 * @param offset1
	 * @param offset2
	 * @param b
	 * @return
	 */
	protected IASTNode parse(String code, int offset1, int offset2, boolean expectedToPass) throws Exception {
		IFile file = importFile("temp.cpp", code); //$NON-NLS-1$
		IASTName[] names = DOMSearchUtil.getSelectedNamesFrom(file, offset1, offset2 - offset1);
		
		if (!expectedToPass) return null;
		
		if (names.length == 0) {
			assertFalse(true);
		} else {
			return names[0];
		}
		
		return null;
	}
	
	protected IASTNode parse(IFile file, int offset1, int offset2, boolean expectedToPass) throws Exception {
		IASTName[] names = DOMSearchUtil.getSelectedNamesFrom(file, offset1, offset2 - offset1);
		
		if (!expectedToPass) return null;
		
		if (names.length == 0) {
			assertFalse(true);
		} else {
			return names[0];
		}		
		
		return null;
	}
	
	protected IASTName[] getDeclarationOffTU(IASTName name) {
		return name.getTranslationUnit().getDeclarations(name.resolveBinding());
	}
    
    protected IASTName[] getReferencesOffTU(IASTName name) {
        return name.getTranslationUnit().getReferences(name.resolveBinding());
    }
}
