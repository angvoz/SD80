/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorSelectionResult;

/**
 * This class is used to wrap possible results from the ILocationResolver (when retrieving
 * nodes from the preprocessor tree.
 * @author dsteffle
 */
public class ASTPreprocessorSelectionResult implements IASTPreprocessorSelectionResult {
	IASTNode selectedNode = null;
	int globalOffset = 0;
	
	public ASTPreprocessorSelectionResult(IASTNode node, int offset) {
		this.selectedNode = node;
		this.globalOffset = offset;
	}
	
	public IASTNode getSelectedNode() {
		return selectedNode;
	}
	
	public void setSelectedNode(IASTNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public int getGlobalOffset() {
		return globalOffset;
	}
	

	public void setGlobalOffset(int globalOffset) {
		this.globalOffset = globalOffset;
	}
	
}
