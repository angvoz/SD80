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

import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;

public abstract class ASTNodeLocation implements IASTNodeLocation {

	private int nodeLength;
	private int nodeOffset;
	

	public ASTNodeLocation(int nodeOffset, int nodeLength) {
		this.nodeOffset = nodeOffset;
		this.nodeLength = nodeLength;
	}
	
	public int getNodeLength() {
		return nodeLength;
	}

	public void setNodeLength(int nodeLength) {
		this.nodeLength = nodeLength;
	}

	public int getNodeOffset() {
		return nodeOffset;
	}

	public void setNodeOffset(int nodeOffset) {
		this.nodeOffset = nodeOffset;
	}

	

}
