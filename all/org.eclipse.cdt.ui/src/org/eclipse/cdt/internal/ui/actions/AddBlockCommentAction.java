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

package org.eclipse.cdt.internal.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.ui.text.ICPartitions;

/**
 * Action that encloses the editor's current selection with Java block comment terminators
 * (<code>&#47;&#42;</code> and <code>&#42;&#47;</code>).
 * 
 * @since 3.0
 */
public class AddBlockCommentAction extends BlockCommentAction {
	/**
	 * Creates a new instance.
	 * 
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 */
	public AddBlockCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/*
	 * @see org.eclipse.jdt.internal.ui.actions.BlockCommentAction#runInternal(org.eclipse.jface.text.ITextSelection, org.eclipse.jface.text.IDocumentExtension3, org.eclipse.jdt.internal.ui.actions.BlockCommentAction.Edit.EditFactory)
	 */
	@Override
	protected void runInternal(ITextSelection selection, IDocumentExtension3 docExtension, Edit.EditFactory factory) throws BadLocationException, BadPartitioningException {
		int selectionOffset= selection.getOffset();
		int selectionEndOffset= selectionOffset + selection.getLength();
		List<Edit> edits= new LinkedList<Edit>();
		ITypedRegion partition= docExtension.getPartition(ICPartitions.C_PARTITIONING, selectionOffset, false);

		handleFirstPartition(partition, edits, factory, selectionOffset);

		while (partition.getOffset() + partition.getLength() < selectionEndOffset) {
			partition= handleInteriorPartition(partition, edits, factory, docExtension);
		}
		
		handleLastPartition(partition, edits, factory, selectionEndOffset);
		
		executeEdits(edits);
	}

	/**
	 * Handle the first partition of the selected text.
	 * 
	 * @param partition
	 * @param edits
	 * @param factory
	 * @param offset
	 */
	private void handleFirstPartition(ITypedRegion partition, List<Edit> edits, Edit.EditFactory factory, int offset) throws BadLocationException {
		
		int partOffset= partition.getOffset();
		String partType= partition.getType();
		
		Assert.isTrue(partOffset <= offset, "illegal partition"); //$NON-NLS-1$
		
		// first partition: mark start of comment
		if (partType == IDocument.DEFAULT_CONTENT_TYPE) {
			// Java code: right where selection starts
			edits.add(factory.createEdit(offset, 0, getCommentStart()));
		} else if (isSpecialPartition(partType)) {
			// special types: include the entire partition
			edits.add(factory.createEdit(partOffset, 0, getCommentStart()));
		}	// javadoc: no mark, will only start after comment
		
	}

	/**
	 * Handles the end of the given partition and the start of the next partition, which is returned.
	 * 
	 * @param partition
	 * @param edits
	 * @param factory
	 * @param docExtension
	 * @throws BadLocationException
	 * @throws BadPartitioningException
	 * @return the region
	 */
	private ITypedRegion handleInteriorPartition(ITypedRegion partition, List<Edit> edits, Edit.EditFactory factory, IDocumentExtension3 docExtension) throws BadPartitioningException, BadLocationException {

		// end of previous partition
		String partType= partition.getType();
		int partEndOffset= partition.getOffset() + partition.getLength();
		int tokenLength= getCommentStart().length();
		
		if (partType == ICPartitions.C_MULTI_LINE_COMMENT) {	
			// already in a comment - remove ending mark
			edits.add(factory.createEdit(partEndOffset - tokenLength, tokenLength, "")); //$NON-NLS-1$	
		}

		// advance to next partition
		partition= docExtension.getPartition(ICPartitions.C_PARTITIONING, partEndOffset, false);
		partType= partition.getType();

		// start of next partition		
		if (partType == ICPartitions.C_MULTI_LINE_COMMENT) {
			// already in a comment - remove startToken
			edits.add(factory.createEdit(partition.getOffset(), getCommentStart().length(), "")); //$NON-NLS-1$
		}
		return partition;
	}

	/**
	 * Handles the end of the last partition.
	 * 
	 * @param partition
	 * @param edits
	 * @param factory
	 * @param endOffset
	 */
	private void handleLastPartition(ITypedRegion partition, List<Edit> edits, Edit.EditFactory factory, int endOffset) throws BadLocationException {

		String partType= partition.getType();
		
		if (partType == IDocument.DEFAULT_CONTENT_TYPE) {
			// normal java: end comment where selection ends
			edits.add(factory.createEdit(endOffset, 0, getCommentEnd()));
		} else if (isSpecialPartition(partType)) {
			// special types: consume entire partition
			edits.add(factory.createEdit(partition.getOffset() + partition.getLength(), 0, getCommentEnd()));
		}
		
	}

	/**
	 * Returns whether <code>partType</code> is special, i.e. a Java <code>String</code>,
	 * <code>Character</code>, or <code>Line End Comment</code> partition.
	 * 
	 * @param partType the partition type to check
	 * @return <code>true</code> if <code>partType</code> is special, <code>false</code> otherwise
	 */
	private boolean isSpecialPartition(String partType) {
		return partType == ICPartitions.C_CHARACTER
				|| partType == ICPartitions.C_STRING
				|| partType == ICPartitions.C_SINGLE_LINE_COMMENT
				|| partType == ICPartitions.C_PREPROCESSOR;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.actions.BlockCommentAction#validSelection(org.eclipse.jface.text.ITextSelection)
	 */
	@Override
	protected boolean isValidSelection(ITextSelection selection) {
		return selection != null && !selection.isEmpty() && selection.getLength() > 0;
	}


}
