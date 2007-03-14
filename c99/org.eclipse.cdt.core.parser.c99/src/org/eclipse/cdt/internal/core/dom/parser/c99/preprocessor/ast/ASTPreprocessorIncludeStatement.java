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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class ASTPreprocessorIncludeStatement extends ASTNode implements
		IASTPreprocessorIncludeStatement {

	private IASTName name;
	private String path;
	private boolean isActive;
	private boolean isResolved;
	private boolean isSystemInclude;
	
	public IASTName getName() {
		return name;
	}
	
	public void setName(IASTName name) {
		this.name = name;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public boolean isResolved() {
		return isResolved;
	}
	
	public void setResolved(boolean isResolved) {
		this.isResolved = isResolved;
	}
	
	public boolean isSystemInclude() {
		return isSystemInclude;
	}
	
	public void setSystemInclude(boolean isSystemInclude) {
		this.isSystemInclude = isSystemInclude;
	}

}
