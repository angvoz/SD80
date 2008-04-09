/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * 
 * Base class for node writers. This class contains methods and string constants
 * used by multiple node writers.
 * 
 * @author Emanuel Graf IFS
 * 
 */
public class NodeWriter {

	protected Scribe scribe;
	protected CPPASTVisitor visitor;
	protected NodeCommentMap commentMap;
	protected static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	protected static final String EQUALS = " = "; //$NON-NLS-1$
	protected static final String RESTRICT = "restrict "; //$NON-NLS-1$
	protected static final String TYPENAME = "typename "; //$NON-NLS-1$
	protected static final String PUBLIC = "public"; //$NON-NLS-1$
	protected static final String PRIVATE = "private"; //$NON-NLS-1$
	protected static final String PROTECTED = "protected"; //$NON-NLS-1$
	protected static final String CONST = "const"; //$NON-NLS-1$
	protected static final String VOLATILE = "volatile"; //$NON-NLS-1$
	protected static final String INLINE = "inline "; //$NON-NLS-1$
	protected static final String EXTERN = "extern "; //$NON-NLS-1$
	protected static final String STATIC = "static "; //$NON-NLS-1$
	protected static final String THROW = "throw "; //$NON-NLS-1$
	protected static final String SPACE_COLON_SPACE = " : "; //$NON-NLS-1$
	protected static final String TEMPLATE = "template "; //$NON-NLS-1$
	protected static final String DOUBLE = "double"; //$NON-NLS-1$
	protected static final String FLOAT = "float"; //$NON-NLS-1$
	protected static final String INT = "int"; //$NON-NLS-1$
	protected static final String CHAR = "char"; //$NON-NLS-1$
	protected static final String VOID = "void"; //$NON-NLS-1$
	protected static final String WCHAR_T = "wchar_t"; //$NON-NLS-1$
	protected static final String CPP_BOOL = "bool"; //$NON-NLS-1$
	protected static final String LONG = "long"; //$NON-NLS-1$
	protected static final String SHORT = "short"; //$NON-NLS-1$
	protected static final String UNSIGNED = "unsigned"; //$NON-NLS-1$
	protected static final String SIGNED = "signed"; //$NON-NLS-1$
	protected static final String CLASS_SPACE = "class "; //$NON-NLS-1$
	protected static final String VAR_ARGS = "..."; //$NON-NLS-1$
	protected static final String COLON_COLON = "::"; //$NON-NLS-1$

	public NodeWriter(Scribe scribe, CPPASTVisitor visitor, NodeCommentMap commentMap) {
		super();
		this.scribe = scribe;
		this.visitor = visitor;
		this.commentMap = commentMap;
	}

	protected void writeNodeList(IASTNode[] nodes) {
		for(int i = 0; i < nodes.length; ++i) {
			nodes[i].accept(visitor);
			if(i + 1 < nodes.length) {
				scribe.print(COMMA_SPACE);
			}
		}
	}
	
	protected void visitNodeIfNotNull(IASTNode node){
		if(node != null){
			node.accept(visitor);
		}
	}


	protected void writeTrailingComments(IASTNode node) {
		//default write newLine
		writeTrailingComments(node, true);
	}
	
	protected boolean hasTrailingComments(IASTNode node){
		if(commentMap.getTrailingCommentsForNode(node).size()>0) {
			return true;
		}
		return false;
	}
	
	protected void writeTrailingComments(IASTNode node, boolean newLine) {
		for(IASTComment comment : commentMap.getTrailingCommentsForNode(node)) {
			scribe.printSpace();
			scribe.print(comment.getComment());
			if(newLine) {
				scribe.newLine();
			}
		}
	}

	protected boolean hasFreestandingComments(IASTNode node){
		if(commentMap.getFreestandingCommentsForNode(node).size()>0) {
			return true;
		}
		return false;
	}

	protected void writeFreeStandingComments(IASTNode node) {
		for(IASTComment comment : commentMap.getFreestandingCommentsForNode(node)) {
			scribe.print(comment.getComment());
			scribe.newLine();
		}
	}
}
