/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;

public interface ICPPASTForStatement extends IASTForStatement {
    
    public static final ASTNodeProperty CONDITION_DECLARATION = new ASTNodeProperty( "org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement"); //$NON-NLS-1$
    public void setConditionDeclaration( IASTDeclaration d );
    public IASTDeclaration getConditionDeclaration();

}
