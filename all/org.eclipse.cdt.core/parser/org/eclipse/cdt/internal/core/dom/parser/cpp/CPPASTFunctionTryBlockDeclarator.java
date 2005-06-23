/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTFunctionTryBlockDeclarator extends CPPASTFunctionDeclarator
        implements ICPPASTFunctionTryBlockDeclarator {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#addCatchHandler(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void addCatchHandler(ICPPASTCatchHandler statement) {
    	if (statement != null) {
    		catchHandlersPos++;
    		catchHandlers = (ICPPASTCatchHandler[]) ArrayUtil.append( ICPPASTCatchHandler.class, catchHandlers, statement );	
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#getCatchHandlers()
     */
    public ICPPASTCatchHandler [] getCatchHandlers() {
        if( catchHandlers == null ) return ICPPASTCatchHandler.EMPTY_CATCHHANDLER_ARRAY;
        catchHandlers = (ICPPASTCatchHandler[]) ArrayUtil.removeNullsAfter( ICPPASTCatchHandler.class, catchHandlers, catchHandlersPos );
        return catchHandlers;
    }


    private ICPPASTCatchHandler [] catchHandlers = null;
    private int catchHandlersPos=-1;
    protected boolean postAccept( ASTVisitor action ){
        if( !super.postAccept( action ) ) return false;
        
        ICPPASTCatchHandler [] handlers = getCatchHandlers();
        for ( int i = 0; i < handlers.length; i++ ) {
            if( !handlers[i].accept( action ) ) return false;
        }
        return true;
    }
    
}
