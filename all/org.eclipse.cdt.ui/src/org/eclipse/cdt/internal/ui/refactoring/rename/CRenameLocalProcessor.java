/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;

import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;


/**
 * Rename processor, setting up input page for a local rename.
 */
public class CRenameLocalProcessor extends CRenameProcessorDelegate {
    private IScope fScope;
    public CRenameLocalProcessor(CRenameProcessor input, String kind, IScope scope) {
        super(input, kind);
        fScope= scope;
        setAvailableOptions(0);
        setOptionsForcingPreview(0);
    }
    
    // overrider
    @Override
	protected int getAcceptedLocations(int selectedOptions) {
        return CRefactory.OPTION_IN_CODE | CRefactory.OPTION_IN_MACRO_DEFINITION | selectedOptions;
    }
    
    // overrider
    @Override
	protected int getSearchScope() {
        return TextSearchWrapper.SCOPE_FILE;
    }
    
    @Override
	protected void analyzeTextMatches(ArrayList<CRefactoringMatch> matches, IProgressMonitor monitor, 
            RefactoringStatus status) {
    	
    	super.analyzeTextMatches(matches, monitor, status);
        if (fScope != null) {
            CRefactoringArgument argument = getArgument();
            ASTManager r = getAstManager();
            int[] result= new int[] {0, Integer.MAX_VALUE};
            IScope scope= argument.getScope();
            IASTNode node= null;
            try {
                node = ASTInternal.getPhysicalNodeOfScope(scope);
                if (argument.getBinding() instanceof IParameter) {
                    node= node.getParent();
                }
            } catch (DOMException e) {
                r.handleDOMException(argument.getTranslationUnit(), e, status);
            }
            if (node != null) {
                IASTFileLocation loc= ASTManager.getLocationInTranslationUnit(node);
                if (loc != null) {
                    result[0]= loc.getNodeOffset();
                    result[1]= result[0] + loc.getNodeLength();
                }
            }
            int[] range= result;
            for (Iterator<CRefactoringMatch> iter = matches.iterator(); iter.hasNext();) {
                CRefactoringMatch m = iter.next();
                if (m.getAstInformation() != CRefactoringMatch.AST_REFERENCE) {
                	int off= m.getOffset();
                	if (off < range[0] || off > range[1]) {
                		iter.remove();
                	}
                }
            }
        }
    }
}
