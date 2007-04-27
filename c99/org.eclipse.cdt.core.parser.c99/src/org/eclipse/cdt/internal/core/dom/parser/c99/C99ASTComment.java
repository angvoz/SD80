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
package org.eclipse.cdt.internal.core.dom.parser.c99;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Represents a comment in the source.
 * 
 * @author Mike
 */
public class C99ASTComment extends ASTNode implements IASTComment {

	private char[] comment;
	private boolean isBlockComment = false;
	
	
	public char[] getComment() {
		return comment;
	}

	public boolean isBlockComment() {
		return isBlockComment;
	}

	public void setBlockComment(boolean isBlockComment) {
		this.isBlockComment = isBlockComment;
	}
	
	public void setComment(char[] comment) {
		this.comment = comment;
	}

	public boolean accept(ASTVisitor visitor) {
		if(visitor.shouldVisitComments) {
            switch(visitor.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT:
	                return false;
	            case ASTVisitor.PROCESS_SKIP:
	                return true;
            }
        }
        return true;
	}
	
}
