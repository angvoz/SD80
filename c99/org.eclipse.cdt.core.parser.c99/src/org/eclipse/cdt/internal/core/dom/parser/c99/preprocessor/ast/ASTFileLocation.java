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

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;


public class ASTFileLocation extends ASTNodeLocation implements IASTFileLocation {

	
	private int endingLineNumber;
	private int startingLineNumber;
	private String fileName;
	
	
	public ASTFileLocation(String fileName, int nodeOffset, int nodeLength, int startingLineNumber, int endingLineNumber) {
		super(nodeOffset, nodeLength);
		this.fileName = fileName;
		this.startingLineNumber = startingLineNumber;
		this.endingLineNumber = endingLineNumber;
	}
	
	
	public ASTFileLocation(String fileName, IToken firstToken, IToken lastToken) {
		super(firstToken.getStartOffset(), lastToken.getEndOffset() - firstToken.getStartOffset());
		this.fileName = fileName;
		this.startingLineNumber = firstToken.getLine();
		this.endingLineNumber = lastToken.getLine();
	}
	
	public int getEndingLineNumber() {
		return endingLineNumber;
	}

	public String getFileName() {
		return fileName;
	}

	public int getStartingLineNumber() {
		return startingLineNumber;
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

}
