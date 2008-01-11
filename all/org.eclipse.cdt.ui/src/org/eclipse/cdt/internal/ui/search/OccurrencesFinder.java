/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

import org.eclipse.cdt.internal.ui.util.Messages;

public class OccurrencesFinder implements IOccurrencesFinder {
	
	public static final String ID= "OccurrencesFinder"; //$NON-NLS-1$
	
	private IASTTranslationUnit fRoot;
	private IASTName fSelectedNode;
	private IBinding fTarget;

	private List/*<OccurrenceLocation>*/fResult;
	private String fDescription;
	
	public OccurrencesFinder() {
		super();
	}
	
	public String initialize(IASTTranslationUnit root, IASTNode node) {
		if (!(node instanceof IASTName))
			return CSearchMessages.OccurrencesFinder_no_element; 
		fRoot= root;
		fSelectedNode= (IASTName)node;
		fTarget= fSelectedNode.resolveBinding();
		if (fTarget == null)
			return CSearchMessages.OccurrencesFinder_no_binding; 
		
		fDescription= Messages.format(CSearchMessages.OccurrencesFinder_occurrence_description, fTarget.getName());
		return null;
	}
	
	private void performSearch() {
		if (fResult == null) {
			fResult= new ArrayList/*<OccurrenceLocation>*/();
			IASTName[] names= fRoot.getDeclarationsInAST(fTarget);
			for (int i= 0; i < names.length; i++) {
				IASTName candidate= names[i];
				if (candidate.isPartOfTranslationUnitFile()) {
					addUsage(candidate, candidate.resolveBinding());
				}
			}
			names= fRoot.getReferences(fTarget);
			for (int i= 0; i < names.length; i++) {
				IASTName candidate= names[i];
				if (candidate.isPartOfTranslationUnitFile()) {
					addUsage(candidate, candidate.resolveBinding());
				}
			}
		}
	}
	
	public OccurrenceLocation[] getOccurrences() {
		performSearch();
		if (fResult.isEmpty())
			return null;
		return (OccurrenceLocation[]) fResult.toArray(new OccurrenceLocation[fResult.size()]);
	}

	public IASTTranslationUnit getASTRoot() {
		return fRoot;
	}
		
	/*
	 * @see org.eclipse.cdt.internal.ui.search.IOccurrencesFinder#getJobLabel()
	 */
	public String getJobLabel() {
		return CSearchMessages.OccurrencesFinder_searchfor ; 
	}
	
	public String getElementName() {
		if (fSelectedNode != null) {
			return new String(fSelectedNode.toCharArray());
		}
		return null;
	}
	
	public String getUnformattedPluralLabel() {
		return CSearchMessages.OccurrencesFinder_label_plural;
	}
	
	public String getUnformattedSingularLabel() {
		return CSearchMessages.OccurrencesFinder_label_singular;
	}
	
	private boolean addUsage(IASTName node, IBinding binding) {
		if (binding != null /* && Bindings.equals(binding, fTarget) */) {
			int flag= 0;
			String description= fDescription;
			IASTNodeLocation nodeLocation= node.getImageLocation();
			if (nodeLocation == null) {
				nodeLocation= node.getFileLocation();
			}
			if (nodeLocation != null) {
				final int offset= nodeLocation.getNodeOffset();
				final int length= nodeLocation.getNodeLength();
				if (offset >= 0 && length > 0) {
					fResult.add(new OccurrenceLocation(offset, length, flag, description));
				}
			}
			return true;
		}
		return false;
	}

	public int getSearchKind() {
		return K_OCCURRENCE;
	}
	
	public String getID() {
		return ID;
	}
}
