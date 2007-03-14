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

import java.util.List;
import java.util.Vector;

import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class ASTPreprocessorFunctionStyleMacroDefinition extends ASTPreprocessorObjectStyleMacroDefinition
		implements IASTPreprocessorFunctionStyleMacroDefinition {

	
	private final List parameters = new Vector();
	
	
	public void addParameter(IASTFunctionStyleMacroParameter parm) {
		parameters.add(parm);
	}

	public IASTFunctionStyleMacroParameter[] getParameters() {
		return (IASTFunctionStyleMacroParameter[]) parameters.toArray(new IASTFunctionStyleMacroParameter[]{});
	}
}
