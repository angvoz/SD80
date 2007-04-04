/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.ast;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class ASTPreprocessorObjectStyleMacroDefinition extends ASTNode
		implements IASTPreprocessorObjectStyleMacroDefinition {

	private IASTName name;
	private String expansion;
	

	public String getExpansion() {
		return expansion;
	}

	public IASTName getName() {
		return name;
	}

	public void setExpansion(String expansion) {
		this.expansion = expansion;

	}

	public void setName(IASTName name) {
		this.name = name;

	}

	public int getRoleForName(IASTName n) {
        if (name == n)
            return r_definition;
        return r_unclear;
    }

}
